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

package com.amazon.opendistroforelasticsearch.sql.request;

import org.elasticsearch.rest.RestRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SqlRequestFactory {

    private static final String SQL_URL_PARAM_KEY = "sql";
    private static final String SQL_FIELD_NAME = "query";
    private static final String PARAM_FIELD_NAME = "parameters";

    private static final String PARAM_TYPE_FIELD_NAME = "type";
    private static final String PARAM_VALUE_FIELD_NAME = "value";

    public static SqlRequest getSqlRequest(final RestRequest request) {
        switch (request.method()) {
            case GET:
                return parseSqlRequestFromUrl(request);
            case POST:
                return parseSqlRequestFromPayload(request);
            default:
                throw new IllegalArgumentException(String.format(Locale.ROOT, "ES SQL does not support HTTP %s method",
                        request.method().name()));
        }
    }

    private static SqlRequest parseSqlRequestFromUrl(final RestRequest restRequest) {

        final String sql = restRequest.param(SQL_URL_PARAM_KEY);

        if (sql == null) {
            throw new IllegalArgumentException("Cannot find sql parameter from the URL");
        }
        return new SqlRequest(sql, null);
    }

    private static SqlRequest parseSqlRequestFromPayload(final RestRequest restRequest) {

        final String content = restRequest.content().utf8ToString();

        JSONObject jsonContent;
        try {
            jsonContent = new JSONObject(content);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Failed to parse request payload", e);
        }

        final String sql = jsonContent.getString(SQL_FIELD_NAME);
        if (jsonContent.has(PARAM_FIELD_NAME)) { // is a PreparedStatement
            JSONArray paramArray = jsonContent.getJSONArray(PARAM_FIELD_NAME);
            List<PreparedStatementRequest.PreparedStatementParameter> parameters = parseParameters(paramArray);
            return new PreparedStatementRequest(sql, jsonContent, parameters);
        }

        return new SqlRequest(sql, jsonContent);
    }

    private static List<PreparedStatementRequest.PreparedStatementParameter> parseParameters(
            final JSONArray paramsJsonArray) {

        List<PreparedStatementRequest.PreparedStatementParameter> parameters = new ArrayList<>();
        for (int i = 0; i < paramsJsonArray.length(); i++) {

            final JSONObject paramJson = paramsJsonArray.getJSONObject(i);
            final String typeString = paramJson.getString(PARAM_TYPE_FIELD_NAME);
            if (typeString == null) {
                throw new IllegalArgumentException("Parameter type cannot be null. parameter json: " +
                        paramJson.toString());
            }

            final PreparedStatementRequest.ParameterType type;
            try {
                type = PreparedStatementRequest.ParameterType.valueOf(typeString.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unsupported parameter type " + typeString, e);
            }

            parameters.add(buildPreparedStatementParameterFromJson(paramJson, type));
        }
        return parameters;
    }

    private static PreparedStatementRequest.PreparedStatementParameter buildPreparedStatementParameterFromJson(
            final JSONObject paramJson, final PreparedStatementRequest.ParameterType parameterType) {

        final PreparedStatementRequest.PreparedStatementParameter parameter;
        try {
            switch (parameterType) {
                case BOOLEAN:
                    parameter = new PreparedStatementRequest.PreparedStatementParameter<>(
                            paramJson.getBoolean(PARAM_VALUE_FIELD_NAME));
                    break;
                case KEYWORD:
                case STRING:
                case DATE:
                    parameter = new PreparedStatementRequest.StringParameter(
                            paramJson.getString(PARAM_VALUE_FIELD_NAME));
                    break;
                case BYTE:
                case SHORT:
                case INTEGER:
                case LONG:
                    parameter = new PreparedStatementRequest.PreparedStatementParameter<>(
                            paramJson.getLong(PARAM_VALUE_FIELD_NAME));
                    break;
                case FLOAT:
                case DOUBLE:
                    parameter = new PreparedStatementRequest.PreparedStatementParameter<>(
                            paramJson.getDouble(PARAM_VALUE_FIELD_NAME));
                    break;
                case NULL:
                    parameter = new PreparedStatementRequest.NullParameter();
                    break;
                default:
                    throw new IllegalArgumentException("Failed to handle parameter type " + parameterType.name());
            }
        } catch (final JSONException e) {
            throw new IllegalArgumentException("Failed to parse PreparedStatement parameters", e);
        }

        return parameter;
    }
}
