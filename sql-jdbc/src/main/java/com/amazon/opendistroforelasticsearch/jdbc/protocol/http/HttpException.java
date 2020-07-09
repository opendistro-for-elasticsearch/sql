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

import com.amazon.opendistroforelasticsearch.jdbc.protocol.exceptions.ResponseException;

/**
 * Exception thrown when an unexpected HTTP response code is
 * received from the server.
 */
public class HttpException extends ResponseException {

    private int statusCode;

    /**
     * @param statusCode HTTP Status code due to which this exception is raised.
     * @param message Message associated with the exception - can be the HTTP
     *         reason phrase corresponding to the status code.
     */
    public HttpException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpException(int statusCode, String message, Throwable cause, String responsePayload) {
        super(message, cause, responsePayload);
        this.statusCode = statusCode;
    }

    /**
     * Returns the HTTP response status code that resulted in
     * this exception.
     *
     * @return HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getLocalizedMessage() {
        String message = getMessage();
        String localizedMessage = "HTTP Code: " + statusCode +
                ". Message: " + (message == null ? "None" : message) + ".";

        if (this.getResponsePayload() != null) {
            localizedMessage += " Raw response received: " + getResponsePayload();
        }

        return localizedMessage;
    }
}
