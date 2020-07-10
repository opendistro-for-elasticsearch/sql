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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class JsonQueryRequest implements QueryRequest {

    private String query;
    private int fetchSize;
    private List<? extends Parameter> parameters;

    public JsonQueryRequest(QueryRequest queryRequest) {
        this.query = queryRequest.getQuery();
        this.parameters = queryRequest.getParameters();
        this.fetchSize = queryRequest.getFetchSize();

    }

    @Override
    public String getQuery() {
        return query;
    }

    @JsonInclude(Include.NON_NULL)
    @Override
    public List<? extends Parameter> getParameters() {
        return parameters;
    }

    @JsonProperty("fetch_size")
    @Override
    public int getFetchSize() {
        return fetchSize;
    }
}
