/*
 * Copyright <2019> Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.jdbc.transport.http;

import com.amazon.opendistroforelasticsearch.jdbc.auth.AuthenticationType;
import com.amazon.opendistroforelasticsearch.jdbc.config.ConnectionConfig;
import com.amazon.opendistroforelasticsearch.jdbc.logging.Logger;
import com.amazon.opendistroforelasticsearch.jdbc.logging.LoggingSource;
import com.amazon.opendistroforelasticsearch.jdbc.transport.TransportException;
import com.amazon.opendistroforelasticsearch.jdbc.transport.http.auth.aws.AWSRequestSigningApacheInterceptor;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import org.apache.http.Header;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class ApacheHttpTransport implements HttpTransport, LoggingSource {
    String scheme;
    String host;
    int port;
    String path;
    int readTimeout;

    private RequestConfig requestConfig;
    private CloseableHttpClient httpClient;

    public ApacheHttpTransport(ConnectionConfig connectionConfig, Logger log, String userAgent) throws TransportException {
        this.host = connectionConfig.getHost();
        this.port = connectionConfig.getPort();
        this.scheme = connectionConfig.isUseSSL() ? "https" : "http";
        this.path = connectionConfig.getPath();

        updateRequestConfig();

        ConnectionSocketFactory sslConnectionSocketFactory = null;

        try {
            sslConnectionSocketFactory = getSslConnectionSocketFactory(connectionConfig);
        } catch (Exception e) {
            throw new TransportException("Exception building SSL/TLS socket factory " + e, e);
        }

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslConnectionSocketFactory)
                .build();

        ApacheHttpClientConnectionFactory connectionFactory =
                new ApacheHttpClientConnectionFactory(new JclLoggerAdapter(log, getSource()));

        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setConnectionManager(
                        new BasicHttpClientConnectionManager(socketFactoryRegistry, connectionFactory))
                .setDefaultSocketConfig(buildDefaultSocketConfig())
                .setDefaultRequestConfig(getRequestConfig())
                .setUserAgent(userAgent);

        // request compression
        if (!connectionConfig.requestCompression())
            httpClientBuilder.disableContentCompression();

        // setup authentication
        if (connectionConfig.getAuthenticationType() == AuthenticationType.BASIC) {
            CredentialsProvider basicCredsProvider = new BasicCredentialsProvider();
            basicCredsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(connectionConfig.getUser(), connectionConfig.getPassword()));
            httpClientBuilder.setDefaultCredentialsProvider(basicCredsProvider);

        } else if (connectionConfig.getAuthenticationType() == AuthenticationType.AWS_SIGV4) {
            AWS4Signer signer = new AWS4Signer();
            signer.setServiceName("es");
            signer.setRegionName(connectionConfig.getRegion());

            AWSCredentialsProvider provider = connectionConfig.getAwsCredentialsProvider() != null ?
                    connectionConfig.getAwsCredentialsProvider() : new DefaultAWSCredentialsProviderChain();
            httpClientBuilder.addInterceptorLast(
                    new AWSRequestSigningApacheInterceptor(
                            "es",
                            signer,
                            provider));
        }

        // TODO - can apply settings retry & backoff
        this.httpClient = httpClientBuilder.build();
    }

    @Override
    public CloseableHttpResponse doGet(String path, Header[] headers, HttpParam[] params, int timeout) throws TransportException {
        return doGet(buildRequestURI(path, params), headers, timeout);
    }

    @Override
    public CloseableHttpResponse doPost(String path, Header[] headers, HttpParam[] params, String body, int timeout) throws TransportException {
        return doPost(buildRequestURI(path, params), headers, body, timeout);
    }

    @Override
    public void close() throws TransportException {
        try {
            this.httpClient.close();
        } catch (IOException e) {
            throw new TransportException(e);
        }
    }

    private ConnectionSocketFactory getSslConnectionSocketFactory(ConnectionConfig connectionConfig)
            throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
            UnrecoverableKeyException, KeyManagementException {

        TrustStrategy trustStrategy = connectionConfig.trustSelfSigned() ? new TrustSelfSignedStrategy() : null;

        SSLContextBuilder builder = SSLContexts.custom();

        if (connectionConfig.getKeyStoreLocation() != null || connectionConfig.getTrustStoreLocation() != null) {
            // trust material
            if (connectionConfig.getTrustStoreLocation() != null) {
                String trustStorePassword = connectionConfig.getTrustStorePassword();
                char[] password = trustStorePassword == null ? "".toCharArray() : trustStorePassword.toCharArray();

                builder.loadTrustMaterial(
                        new File(connectionConfig.getTrustStoreLocation()),
                        password, trustStrategy);
            }

            // key material
            if (connectionConfig.getKeyStoreLocation() != null) {
                String keyStorePassword = connectionConfig.getKeyStorePassword();
                char[] password = keyStorePassword == null ? "".toCharArray() : keyStorePassword.toCharArray();

                // TODO - can add alias selection strategy
                // TODO - can add support for a separate property for key password
                builder.loadKeyMaterial(new File(connectionConfig.getKeyStoreLocation()), password, password).build();
            }

        } else {

            builder.loadTrustMaterial(null, trustStrategy);
        }

        HostnameVerifier hostnameVerifier = connectionConfig.hostnameVerification() ?
                SSLConnectionSocketFactory.getDefaultHostnameVerifier() : new NoopHostnameVerifier();

        SSLContext sslContext = builder.build();
        return new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
    }

    private SocketConfig buildDefaultSocketConfig() {
        return SocketConfig.custom()
                .setSoKeepAlive(true)
                .setSoTimeout(this.readTimeout)
                .build();
    }

    private void updateRequestConfig() {
        this.requestConfig = RequestConfig.custom()
                .setSocketTimeout(this.readTimeout)
                .build();
    }

    private RequestConfig getRequestConfig() {
        return this.requestConfig;
    }

    public void setReadTimeout(int readTimeout) {
        if (readTimeout != this.readTimeout) {
            this.readTimeout = readTimeout;
            updateRequestConfig();
        }
    }

    private URIBuilder getUriBuilder(String path) {
        return new URIBuilder()
                .setScheme(this.scheme)
                .setHost(this.host)
                .setPort(this.port)
                .setPath(this.path + path);
    }


    private URI buildRequestURI(String path, HttpParam... params) throws TransportException {
        try {
            URIBuilder uriBuilder = getUriBuilder(path);

            if (params != null) {
                for (HttpParam param : params)
                    uriBuilder.setParameter(param.getName(), param.getValue());
            }
            return uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new TransportException(e);
        }
    }

    private CloseableHttpResponse doGet(URI uri, Header[] headers, int readTimeout) throws TransportException {
        try {
            setReadTimeout(readTimeout);
            HttpGet request = new HttpGet(uri);
            request.setHeaders(headers);
            request.setConfig(getRequestConfig());
            return httpClient.execute(request);
        } catch (IOException e) {
            throw new TransportException(e);
        }
    }

    private CloseableHttpResponse doPost(URI uri, Header[] headers, String body, int readTimeout) throws TransportException {
        try {
            setReadTimeout(readTimeout);
            HttpPost request = new HttpPost(uri);
            request.setHeaders(headers);
            request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
            request.setConfig(getRequestConfig());
            return httpClient.execute(request);
        } catch (IOException e) {
            throw new TransportException(e);
        }
    }

}
