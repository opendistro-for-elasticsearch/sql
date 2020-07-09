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

package com.amazon.opendistroforelasticsearch.jdbc.protocol.http;

import com.amazon.opendistroforelasticsearch.jdbc.protocol.exceptions.MalformedResponseException;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.exceptions.ResponseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class JsonHttpResponseHandler {

    protected JsonHttpProtocol protocol;

    public static final Set<Integer> DEFAULT_ACCEPTABLE_HTTP_CODES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(HttpStatus.SC_OK)));

    public JsonHttpResponseHandler(JsonHttpProtocol protocol) {
        this.protocol = protocol;
    }

    public <R> R handleResponse(HttpResponse response, JsonContentParser<InputStream, R> contentParser) throws ResponseException {
        return handleResponse(response, null, true, contentParser);
    }

    public <R> R handleResponse(HttpResponse response, Set<Integer> acceptableHttpStatusCodes,
                                boolean expectResponseBody, JsonContentParser<InputStream, R> contentParser) throws ResponseException {

        try {
            checkResponseForErrors(response, acceptableHttpStatusCodes, expectResponseBody);

            try (InputStream responseContent = response.getEntity().getContent()) {
                return contentParser.apply(responseContent);
            }

        } catch (JsonProcessingException jpe) {
            throw new MalformedResponseException("Could not process server response", jpe);
        } catch (IOException ioe) {
            throw new ResponseException("Error reading server response", ioe);
        }
    }

    /**
     * Checks if an HttpResponse meets the requirements to be accepted.
     *
     * @param response HttpResponse to check
     *
     * @throws ResponseException if the HttpResponse fails any of the checks
     * @throws IOException if there is an I/O exception when reading the
     *         HttpResponse
     */
    protected void checkResponseForErrors(HttpResponse response, Set<Integer> acceptableHttpStatusCodes,
                                          boolean expectResponseBodyContent) throws ResponseException, IOException {
        int statusCode = response.getStatusLine().getStatusCode();

        if (!isHttpStatusCodeAcceptable(statusCode, acceptableHttpStatusCodes)) {

            String responseBody = extractResponseBody(response);

            HttpException httpException = new HttpException(statusCode,
                    response.getStatusLine().getReasonPhrase(), null, responseBody);

            if (statusCode == HttpStatus.SC_METHOD_NOT_ALLOWED) {
                throw new ResponseException("Could not submit SQL request to the target server. " +
                        "Make sure the SQL plugin is installed on the server and responding on the " +
                        "\"/" + protocol.getSqlContextPath() + "\" context path.", httpException);
            } else {
                throw httpException;
            }
        }

        if (expectResponseBodyContent &&
                (response.getEntity() == null || response.getEntity().getContent() == null)) {
            throw new MalformedResponseException("Empty response.");
        }
    }


    /**
     * Reads and returns the entire response body content present in an
     * HttpResponse as a String.
     * <p>
     * This is meant to be used only in cases where a request to the
     * server has failed. In such cases we check the response
     * body contains any text or json content which could potentially
     * be helpful for a user to understand why the request failed.
     * <p>
     * We expect such content to only contain error messages of a limited
     * size, so it's ok to read in the entire response body as a String.
     *
     * @param response HttpResponse to extract the response body from
     *
     * @return the response body as a String or null if no response body
     *         is present
     */
    protected String extractResponseBody(HttpResponse response) {
        // TODO - limit the amount read from response.getEntity() ?
        String responseBody = null;
        try {
            if (response.getEntity() != null && response.getEntity().getContent() != null &&
                    response.getEntity().getContentType() != null &&
                    response.getEntity().getContentType().getValue() != null &&
                    (response.getEntity().getContentType().getValue().contains("application/json") ||
                            response.getEntity().getContentType().getValue().contains("text/plain"))) {
                responseBody = EntityUtils.toString(response.getEntity());
            }

        } catch (IOException ioe) {
            // ignore
        }
        return responseBody;
    }

    /**
     * HTTP Status codes that indicate success for this response
     * handler
     *
     * @return Set of HTTP Status codes that indicate successful requests.
     */
    protected Set<Integer> getAcceptableHttpStatusCodes() {
        return DEFAULT_ACCEPTABLE_HTTP_CODES;
    }

    private boolean isHttpStatusCodeAcceptable(int statusCode, Set<Integer> acceptableHttpStatusCodes) {

        if (acceptableHttpStatusCodes == null)
            acceptableHttpStatusCodes = DEFAULT_ACCEPTABLE_HTTP_CODES;

        return acceptableHttpStatusCodes.contains(statusCode);
    }

    @FunctionalInterface
    public interface JsonContentParser<T, R> {
        R apply(T t) throws IOException;
    }
}
