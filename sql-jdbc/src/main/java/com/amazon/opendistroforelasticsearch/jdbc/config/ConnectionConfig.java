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

package com.amazon.opendistroforelasticsearch.jdbc.config;

import com.amazon.opendistroforelasticsearch.jdbc.auth.AuthenticationType;
import com.amazon.opendistroforelasticsearch.jdbc.logging.LogLevel;
import com.amazon.opendistroforelasticsearch.jdbc.internal.util.AwsHostNameUtil;
import com.amazon.opendistroforelasticsearch.jdbc.internal.util.UrlParser;
import com.amazonaws.auth.AWSCredentialsProvider;

import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConnectionConfig {

    private String url;
    private String host;
    private int port;
    private int fetchSize;
    private String path;
    private boolean useSSL;
    private int loginTimeout;
    private String logOutput;
    private PrintWriter logWriter;
    private String user;
    private String password;
    private boolean requestCompression;
    private AuthenticationType authenticationType;
    private AWSCredentialsProvider awsCredentialsProvider;
    private String region;
    private LogLevel logLevel;

    private String keyStoreLocation;
    private String keyStorePassword;
    private String keyStoreType;
    private String trustStoreLocation;
    private String trustStorePassword;
    private String trustStoreType;
    private boolean trustSelfSigned;
    private boolean hostnameVerification;

    private ConnectionConfig(Builder builder) {
        this.url = builder.getUrl();
        this.host = builder.getHostProperty().getValue();
        this.port = builder.getPortProperty().getValue();
        this.fetchSize = builder.getFetchSizeProperty().getValue();
        this.path = builder.getPathProperty().getValue();
        this.useSSL = builder.getUseSSLProperty().getValue();

        this.logOutput = builder.getLogOutputProperty().getValue();
        this.logLevel = builder.getLogLevelConnectionProperty().getValue();
        this.logWriter = builder.getLogWriter();

        this.loginTimeout = builder.getLoginTimeoutProperty().getValue();

        this.user = builder.getUserProperty().getValue();
        this.password = builder.getPasswordProperty().getValue();

        this.requestCompression = builder.getRequestCompressionProperty().getValue();
        this.authenticationType = builder.getAuthConnectionProperty().getValue();
        this.awsCredentialsProvider = builder.getAwsCredentialProvider().getValue();
        this.region = builder.getRegionConnectionProperty().getValue();

        this.keyStoreLocation = builder.getKeyStoreLocationConnectionProperty().getValue();
        this.keyStorePassword = builder.getKeyStorePasswordConnectionProperty().getValue();
        this.keyStoreType = builder.getKeyStoreTypeConnectionProperty().getValue();

        this.trustStoreLocation = builder.getTrustStoreLocationConnectionProperty().getValue();
        this.trustStorePassword = builder.getTrustStorePasswordConnectionProperty().getValue();
        this.trustStoreType = builder.getTrustStoreTypeConnectionProperty().getValue();

        this.trustSelfSigned = builder.getTrustSelfSignedConnectionProperty().getValue();

        this.hostnameVerification = builder.getHostnameVerificationConnectionProperty().getValue();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getUrl() {
        return url;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public String getPath() {
        return path;
    }

    public boolean isUseSSL() {
        return useSSL;
    }

    public boolean requestCompression() {
        return requestCompression;
    }

    public int getLoginTimeout() {
        return loginTimeout;
    }

    public String getLogOutput() {
        return logOutput;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public AWSCredentialsProvider getAwsCredentialsProvider() {
        return awsCredentialsProvider;
    }

    public String getRegion() {
        return region;
    }

    public PrintWriter getLogWriter() {
        return logWriter;
    }

    public String getKeyStoreLocation() {
        return keyStoreLocation;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public String getTrustStoreLocation() {
        return trustStoreLocation;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }

    public boolean trustSelfSigned() {
        return trustSelfSigned;
    }

    public boolean hostnameVerification() {
        return hostnameVerification;
    }

    @Override
    public String toString() {
        return "ConnectionConfig{" +
                "url='" + url + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", fetchSize=" + fetchSize +
                ", path='" + path + '\'' +
                ", useSSL=" + useSSL +
                ", loginTimeout=" + loginTimeout +
                ", logOutput='" + logOutput + '\'' +
                ", logWriter=" + logWriter +
                ", user='" + user + '\'' +
                ", password='" + mask(password) + '\'' +
                ", requestCompression=" + requestCompression +
                ", authenticationType=" + authenticationType +
                ", awsCredentialsProvider=" + awsCredentialsProvider +
                ", region='" + region + '\'' +
                ", logLevel=" + logLevel +
                ", keyStoreLocation='" + keyStoreLocation + '\'' +
                ", keyStorePassword='" + mask(keyStorePassword) + '\'' +
                ", keyStoreType='" + keyStoreType + '\'' +
                ", trustStoreLocation='" + trustStoreLocation + '\'' +
                ", trustStorePassword='" + mask(trustStorePassword) + '\'' +
                ", trustStoreType='" + trustStoreType + '\'' +
                ", trustSelfSigned='" + trustSelfSigned + '\'' +
                ", hostnameVerification='" + hostnameVerification + '\'' +
                '}';
    }

    private String mask(String string) {
        return string == null || string.length() == 0 ? "<not set>" : "<set>";
    }

    public static class Builder {

        private HostConnectionProperty hostProperty = new HostConnectionProperty();
        private PortConnectionProperty portProperty = new PortConnectionProperty();
        private FetchSizeProperty fetchSizeProperty = new FetchSizeProperty();
        private LoginTimeoutConnectionProperty loginTimeoutProperty = new LoginTimeoutConnectionProperty();
        private UseSSLConnectionProperty useSSLProperty = new UseSSLConnectionProperty();
        private PathConnectionProperty pathProperty = new PathConnectionProperty();
        private LogOutputConnectionProperty logOutputProperty = new LogOutputConnectionProperty();
        private UserConnectionProperty userProperty = new UserConnectionProperty();
        private PasswordConnectionProperty passwordProperty = new PasswordConnectionProperty();
        private RequestCompressionConnectionProperty requestCompressionProperty =
                new RequestCompressionConnectionProperty();
        private AuthConnectionProperty authConnectionProperty = new AuthConnectionProperty();
        private RegionConnectionProperty regionConnectionProperty = new RegionConnectionProperty();
        private LogLevelConnectionProperty logLevelConnectionProperty = new LogLevelConnectionProperty();

        private KeyStoreLocationConnectionProperty keyStoreLocationConnectionProperty
                = new KeyStoreLocationConnectionProperty();
        private KeyStorePasswordConnectionProperty keyStorePasswordConnectionProperty
                = new KeyStorePasswordConnectionProperty();
        private KeyStoreTypeConnectionProperty keyStoreTypeConnectionProperty
                = new KeyStoreTypeConnectionProperty();

        private TrustStoreLocationConnectionProperty trustStoreLocationConnectionProperty
                = new TrustStoreLocationConnectionProperty();
        private TrustStorePasswordConnectionProperty trustStorePasswordConnectionProperty
                = new TrustStorePasswordConnectionProperty();
        private TrustStoreTypeConnectionProperty trustStoreTypeConnectionProperty
                = new TrustStoreTypeConnectionProperty();

        private TrustSelfSignedConnectionProperty trustSelfSignedConnectionProperty
                = new TrustSelfSignedConnectionProperty();

        private AwsCredentialsProviderProperty awsCredentialsProviderProperty
                = new AwsCredentialsProviderProperty();

        private HostnameVerificationConnectionProperty hostnameVerificationConnectionProperty
                = new HostnameVerificationConnectionProperty();

        ConnectionProperty[] connectionProperties = new ConnectionProperty[]{
                hostProperty,
                portProperty,
                fetchSizeProperty,
                loginTimeoutProperty,
                useSSLProperty,
                pathProperty,
                logOutputProperty,
                logLevelConnectionProperty,
                userProperty,
                passwordProperty,
                requestCompressionProperty,
                authConnectionProperty,
                awsCredentialsProviderProperty,
                regionConnectionProperty,
                keyStoreLocationConnectionProperty,
                keyStorePasswordConnectionProperty,
                keyStoreTypeConnectionProperty,
                trustStoreLocationConnectionProperty,
                trustStorePasswordConnectionProperty,
                trustStoreTypeConnectionProperty,
                trustSelfSignedConnectionProperty,
                hostnameVerificationConnectionProperty
        };

        private String url = null;
        private PrintWriter logWriter = null;
        private Map<String, Object> propertyMap;
        private Map<String, Object> overrideMap;
        private Properties urlProperties;
        private Properties properties;


        public ConnectionProperty[] getConnectionProperties() {
            return connectionProperties;
        }

        public HostConnectionProperty getHostProperty() {
            return hostProperty;
        }

        public PortConnectionProperty getPortProperty() {
            return portProperty;
        }

        public FetchSizeProperty getFetchSizeProperty() {
            return fetchSizeProperty;
        }

        public LoginTimeoutConnectionProperty getLoginTimeoutProperty() {
            return loginTimeoutProperty;
        }

        public UseSSLConnectionProperty getUseSSLProperty() {
            return useSSLProperty;
        }

        public PathConnectionProperty getPathProperty() {
            return pathProperty;
        }

        public LogOutputConnectionProperty getLogOutputProperty() {
            return logOutputProperty;
        }

        public UserConnectionProperty getUserProperty() {
            return userProperty;
        }

        public PasswordConnectionProperty getPasswordProperty() {
            return passwordProperty;
        }

        public RequestCompressionConnectionProperty getRequestCompressionProperty() {
            return requestCompressionProperty;
        }

        public AuthConnectionProperty getAuthConnectionProperty() {
            return authConnectionProperty;
        }

        public AwsCredentialsProviderProperty getAwsCredentialProvider() {
            return awsCredentialsProviderProperty;
        }

        public RegionConnectionProperty getRegionConnectionProperty() {
            return regionConnectionProperty;
        }

        public LogLevelConnectionProperty getLogLevelConnectionProperty() {
            return logLevelConnectionProperty;
        }

        public PrintWriter getLogWriter() {
            return logWriter;
        }

        public KeyStoreLocationConnectionProperty getKeyStoreLocationConnectionProperty() {
            return keyStoreLocationConnectionProperty;
        }

        public KeyStorePasswordConnectionProperty getKeyStorePasswordConnectionProperty() {
            return keyStorePasswordConnectionProperty;
        }

        public KeyStoreTypeConnectionProperty getKeyStoreTypeConnectionProperty() {
            return keyStoreTypeConnectionProperty;
        }

        public TrustStoreLocationConnectionProperty getTrustStoreLocationConnectionProperty() {
            return trustStoreLocationConnectionProperty;
        }

        public TrustStorePasswordConnectionProperty getTrustStorePasswordConnectionProperty() {
            return trustStorePasswordConnectionProperty;
        }

        public TrustStoreTypeConnectionProperty getTrustStoreTypeConnectionProperty() {
            return trustStoreTypeConnectionProperty;
        }

        public TrustSelfSignedConnectionProperty getTrustSelfSignedConnectionProperty() {
            return trustSelfSignedConnectionProperty;
        }

        public HostnameVerificationConnectionProperty getHostnameVerificationConnectionProperty() {
            return hostnameVerificationConnectionProperty;
        }

        public Builder setLogWriter(PrintWriter printWriter) {
            this.logWriter = printWriter;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setPropertyMap(Map<String, Object> map) {
            if (map != null) {
                propertyMap = new HashMap<>();
                propertyMap.putAll(map);
            }
            return this;
        }

        /**
         * Accumulates property values to override. Successive calls
         * are applied in the order they are made i.e. a property value
         * supplied in the most recent invocation overrides any value
         * supplied in a prior invocations.
         *
         * @param map map containing property key-value pairs
         *
         * @return
         */
        public Builder overrideProperties(Map<String, Object> map) {
            if (map != null) {
                if (overrideMap == null) {
                    overrideMap = new HashMap<>();
                }
                this.overrideMap.putAll(map);
            }
            return this;
        }

        public Builder setProperties(Properties properties) {
            if (properties != null) {
                this.properties = new Properties();

                Enumeration<?> enumeration = properties.propertyNames();

                while (enumeration.hasMoreElements()) {
                    String key = (String) enumeration.nextElement();
                    this.properties.setProperty(key, properties.getProperty(key));
                }
            }
            return this;
        }

        public ConnectionConfig build() throws ConnectionPropertyException {
            if (url != null) {
                try {
                    urlProperties = UrlParser.parseProperties(url);
                } catch (URISyntaxException e) {
                    throw new ConnectionPropertyException("Invalid connection URL", e);
                }
            }

            for (ConnectionProperty connectionProperty : connectionProperties) {
                setRawValue(connectionProperty);
            }

            validateConfig();

            return new ConnectionConfig(this);
        }

//        public DriverPropertyInfo[] buildDriverPropertyInfo() throws ConnectionPropertyException {
//            // Return connection properties that need more
//            try {
//                build();
//            } catch (ConnectionPropertyException cpe) {
//
//            }
//            validateConfig();
//
//            return new ConnectionConfig(this);
//        }

        private void setRawValue(ConnectionProperty connectionProperty) throws ConnectionPropertyException {
            Object value = getPropertyValueToSet(connectionProperty.getKey());
            connectionProperty.setRawValue(value);
        }

        /**
         * Validate the overall configuration to be applied.
         *
         * @throws ConnectionPropertyException if the configuration attempted
         *         fails validation checks
         */
        private void validateConfig() throws ConnectionPropertyException {
            AuthenticationType authenticationType = authConnectionProperty.getValue();

            if (authenticationType == AuthenticationType.NONE) {

                // Use Basic auth if it a username is provided but an
                // explicit auth type is not set

                if (userProperty.getValue() != null) {
                    authConnectionProperty.setRawValue(AuthenticationType.BASIC.name());
                }

            } else if (authenticationType == AuthenticationType.BASIC &&
                    userProperty.getValue() == null) {

                throw new ConnectionPropertyException(authConnectionProperty.getKey(),
                        "Basic authentication requires a valid username but none was provided.");

            } else if (authenticationType == AuthenticationType.AWS_SIGV4 &&
                    regionConnectionProperty.getValue() == null) {

                // aws sdk auto-detection does not work for AWS ES endpoints
                String region = AwsHostNameUtil.parseRegion(hostProperty.getValue());

                if (region == null) {
                    throw new ConnectionPropertyException(authConnectionProperty.getKey(),
                            String.format("AWS Signature V4 authentication requires a region to be used, but " +
                                    "a valid value could not be determined from the specified hostname. " +
                                    "Provide an explicit region value (e.g. us-east-1) " +
                                    "via the \"%s\" connection property.", regionConnectionProperty.getKey()));
                } else {
                    regionConnectionProperty.setRawValue(region);
                }
            }

            if (portProperty.getRawValue() == null && useSSLProperty.getValue() == true) {
                // port is not explicitly specified, but SSL is enabled
                // change the default port to use to 443
                portProperty.setRawValue(443);
            }

            if (fetchSizeProperty.getValue() < 0) {
                throw new ConnectionPropertyException(fetchSizeProperty.getKey(),
                        "Cursor fetch size value should be greater or equal to zero");
            }
        }

        /**
         * Computes the effective value for a connection property
         * as per the necessary precedence order.
         * <p>
         * Properties specified via overrideProperties have the
         * highest precedence, followed by properties specified via
         * setPropertyMap, followed by properties specified via
         * setProperties and finally property values specified via
         * the connection URL.
         *
         * @param key name of the property
         *
         * @return effective value
         */
        private Object getPropertyValueToSet(String key) {
            if (overrideMap != null && overrideMap.containsKey(key)) {
                return overrideMap.get(key);
            }

            if (propertyMap != null && propertyMap.containsKey(key)) {
                return propertyMap.get(key);
            }

            if (properties != null) {
                Object value = properties.getProperty(key);
                if (value != null)
                    return value;
            }

            if (urlProperties != null) {
                Object value = urlProperties.getProperty(key);
                if (value != null)
                    return value;
            }

            return null;
        }
    }
}
