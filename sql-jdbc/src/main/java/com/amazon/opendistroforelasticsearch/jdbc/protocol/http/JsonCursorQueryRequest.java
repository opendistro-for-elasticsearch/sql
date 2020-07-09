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

import com.amazon.opendistroforelasticsearch.jdbc.protocol.Parameter;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.QueryRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Definition of json cursor request
 *
 *  @author abbas hussain
 *  @since 07.05.20
 **/
public class JsonCursorQueryRequest implements QueryRequest  {

    private final String cursor;

    public JsonCursorQueryRequest(QueryRequest queryRequest) {
        this.cursor = queryRequest.getQuery();
    }

    @JsonProperty("cursor")
    @Override
    public String getQuery() {
        return cursor;
    }

    @JsonIgnore
    @Override
    public List<? extends Parameter> getParameters() {
        return null;
    }

    @JsonIgnore
    @Override
    public int getFetchSize() {
        return 0;
    }
}
