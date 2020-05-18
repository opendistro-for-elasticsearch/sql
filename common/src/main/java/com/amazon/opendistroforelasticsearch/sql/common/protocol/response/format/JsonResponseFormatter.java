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

package com.amazon.opendistroforelasticsearch.sql.common.protocol.response.format;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

/**
 * Abstract class for all JSON formatter.
 * @param <Response>
 */
@RequiredArgsConstructor
public abstract class JsonResponseFormatter<Response> implements ResponseFormatter<Response> {

    /**
     * Should response be formatted in pretty json string.
     */
    private final boolean isPretty;

    @Override
    public String format(Response response) {
        JSONObject json = new JSONObject(buildJsonObject(response));
        return isPretty ? json.toString(2) : json.toString();
    }

    /**
     * Build JSON object to generate json string.
     * @param response  response
     * @return          json object
     */
    protected abstract Object buildJsonObject(Response response);

}
