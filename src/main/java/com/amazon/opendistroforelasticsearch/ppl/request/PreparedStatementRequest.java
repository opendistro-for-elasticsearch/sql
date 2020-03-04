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

package com.amazon.opendistroforelasticsearch.ppl.request;

import java.util.List;
import org.json.JSONObject;

public class PreparedStatementRequest extends PPLRequest {

    private List<PreparedStatementParameter> parameters;
    private String pplTemplate;

    public PreparedStatementRequest(String ppl, JSONObject payloadJson, List<PreparedStatementParameter> parameters) {
        super(null, payloadJson);
        this.pplTemplate = ppl;
        this.parameters = parameters;
        this.ppl = this.substituteParameters();
    }

    public List<PreparedStatementParameter> getParameters() {
        return this.parameters;
    }

    @Override
    public String getPpl() {
        return this.ppl;
    }

    public String getPreparedStatement() {
        return this.pplTemplate;
    }

    private String substituteParameters() {
        if (this.pplTemplate == null) {
            return null;
        }

        //TODO
        return "";
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
}
