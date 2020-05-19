/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.protocol.response.format;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import static com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter.Style.PRETTY;

/**
 * Abstract class for all JSON formatter.
 * @param <Response>
 */
@RequiredArgsConstructor
public abstract class JsonResponseFormatter<Response> implements ResponseFormatter<Response> {

    /**
     * JSON format styles
     */
    public enum Style {
        PRETTY, COMPACT
    }

    /**
     * Should response be formatted in pretty json string.
     */
    private final Style style;


    @Override
    public String format(Response response) {
        return jsonify(buildJsonObject(response));
    }

    @Override
    public String format(Throwable t) {
        return jsonify(buildJsonObject(t));
    }

    /**
     * Build JSON object to generate response json string.
     * @param response  response
     * @return          json object for response
     */
    protected abstract Object buildJsonObject(Response response);

    /**
     * Build JSON object to generate error json string.
     * @param t     exception
     * @return      json object for error
     */
    protected abstract Object buildJsonObject(Throwable t);


    private String jsonify(Object jsonObject) {
        JSONObject json = new JSONObject(jsonObject);
        return (style == PRETTY) ? json.toString(2) : json.toString();
    }
}
