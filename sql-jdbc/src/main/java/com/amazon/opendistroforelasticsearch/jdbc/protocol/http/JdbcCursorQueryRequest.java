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

import com.amazon.opendistroforelasticsearch.jdbc.protocol.JdbcQueryParam;;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.QueryRequest;


import java.util.List;
import java.util.Objects;

/**
 * Bean to encapsulate cursor ID
 *
 *  @author abbas hussain
 *  @since 07.05.20
 **/
public class JdbcCursorQueryRequest implements QueryRequest {

    String cursor;

    public JdbcCursorQueryRequest(String cursor) {
        this.cursor = cursor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JdbcCursorQueryRequest)) return false;
        JdbcCursorQueryRequest that = (JdbcCursorQueryRequest) o;
        return Objects.equals(cursor, that.cursor) &&
                Objects.equals(getParameters(), that.getParameters());
    }

    @Override
    public int hashCode() {
        return Objects.hash(cursor, getParameters());
    }

    @Override
    public String getQuery() {
        return cursor;
    }

    @Override
    public List<JdbcQueryParam> getParameters() {
        return null;
    }

    @Override
    public int getFetchSize() {
        return 0;
    }

    @Override
    public String toString() {
        return "JdbcQueryRequest{" +
                "cursor='" + cursor + '\'' +
                '}';
    }
}
