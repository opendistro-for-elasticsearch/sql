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

package com.amazon.opendistroforelasticsearch.jdbc.protocol;

import java.util.List;
import java.util.Objects;

public class JdbcQueryRequest implements QueryRequest {

    private String statement;
    private int fetchSize;
    List<JdbcQueryParam> parameters;

    public JdbcQueryRequest(String sql) {
        this.statement = sql;
    }

    public JdbcQueryRequest(String sql, int fetchSize) {
        this.statement = sql;
        this.fetchSize = fetchSize;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JdbcQueryRequest)) return false;
        JdbcQueryRequest that = (JdbcQueryRequest) o;
        return Objects.equals(statement, that.statement) &&
                Objects.equals(getParameters(), that.getParameters());
    }

    @Override
    public int hashCode() {
        return Objects.hash(statement, getParameters());
    }

    @Override
    public String getQuery() {
        return statement;
    }

    @Override
    public List<JdbcQueryParam> getParameters() {
        return parameters;
    }

    public void setParameters(List<JdbcQueryParam> parameters) {
        this.parameters = parameters;
    }

    @Override
    public int getFetchSize() {
        return fetchSize;
    }

    @Override
    public String toString() {
        return "JdbcQueryRequest{" +
                "statement='" + statement + '\'' +
                ", fetchSize='" + fetchSize + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
