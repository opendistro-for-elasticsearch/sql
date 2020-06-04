/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.request;

import org.json.JSONObject;

import java.util.List;

public class PreparedStatementRequest extends SqlRequest {

    private List<PreparedStatementParameter> parameters;
    private String sqlTemplate;

    public PreparedStatementRequest(String sql, JSONObject payloadJson, List<PreparedStatementParameter> parameters) {
        super(null, payloadJson);
        this.sqlTemplate = sql;
        this.parameters = parameters;
        this.sql = this.substituteParameters();
    }

    public PreparedStatementRequest(String sql, final Integer fetchSize,
                                    JSONObject payloadJson, List<PreparedStatementParameter> parameters) {
        this(sql, payloadJson, parameters);
        this.fetchSize = fetchSize;
    }

    public List<PreparedStatementParameter> getParameters() {
        return this.parameters;
    }

    @Override
    public String getSql() {
        return this.sql;
    }

    public String getPreparedStatement() {
        return this.sqlTemplate;
    }

    private String substituteParameters() {
        if (this.sqlTemplate == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int paramIndex = 0;
        int i = 0;
        while (i < this.sqlTemplate.length()) {
            char c = this.sqlTemplate.charAt(i);
            if (c == '\'') {
                // found string starting quote character, skip the string
                sb.append(c);
                i++;
                while (i < this.sqlTemplate.length()) {
                    char s = this.sqlTemplate.charAt(i);
                    sb.append(s);
                    if (s == '\'') {
                        if (this.sqlTemplate.charAt(i - 1) == '\\') {
                            // this is an escaped single quote (\') still in the string
                            i++;
                        } else if ((i + 1) < this.sqlTemplate.length() && this.sqlTemplate.charAt(i + 1) == '\'') {
                            // found 2 single quote {''} in a string, which is escaped single quote {'}
                            // move to next character
                            sb.append('\'');
                            i += 2;
                        } else {
                            // found the string ending single quote char
                            break;
                        }
                    } else {
                        // not single quote character, move on
                        i++;
                    }
                }
            } else if (c == '?') {
                // question mark "?" not in a string
                if (paramIndex >= this.parameters.size()) {
                    throw new IllegalStateException("Placeholder count is greater than parameter number "
                            + parameters.size() + " . Cannot convert PreparedStatement to sql query");
                }
                sb.append(this.parameters.get(paramIndex).getSqlSubstitutionValue());
                paramIndex++;
            } else {
                // other character, simply append
                sb.append(c);
            }
            i++;
        }

        return sb.toString();
    }

    //////////////////////////////////////////////////
    // Parameter related types below
    //////////////////////////////////////////////////
    public enum ParameterType {
        BYTE,
        SHORT,
        INTEGER,
        LONG,
        FLOAT,
        DOUBLE,
        BOOLEAN,
        STRING,
        KEYWORD,
        DATE,
        NULL
    }

    public static class PreparedStatementParameter<T> {
        protected T value;

        public PreparedStatementParameter(T value) {
            this.value = value;
        }

        public String getSqlSubstitutionValue() {
            return String.valueOf(this.value);
        }

        public T getValue() {
            return this.value;
        }
    }

    public static class StringParameter extends PreparedStatementParameter<String> {

        public StringParameter(String value) {
            super(value);
        }

        @Override
        public String getSqlSubstitutionValue() {
            // TODO: investigate other injection prevention
            if (this.value == null) {
                return "null";
            }
            StringBuilder sb = new StringBuilder();
            sb.append('\''); // starting quote
            for (int i = 0; i < this.value.length(); i++) {
                char c = this.value.charAt(i);
                switch (c) {
                    case 0:
                        sb.append('\\').append(0);
                        break;
                    case '\n':
                        sb.append('\\').append('n');
                        break;
                    case '\r':
                        sb.append('\\').append('r');
                        break;
                    case '\\':
                        sb.append('\\').append('\\');
                        break;
                    case '\'':
                        sb.append('\\').append('\'');
                        break;
                    case '\"':
                        sb.append('\\').append('\"');
                        break;
                    default:
                        sb.append(c);
                }
            }
            sb.append('\''); // ending quote
            return sb.toString();
        }
    }

    public static class NullParameter extends PreparedStatementParameter {

        public NullParameter() {
            super(null);
        }

        @Override
        public String getSqlSubstitutionValue() {
            return "null";
        }
    }
}
