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

package com.amazon.opendistroforelasticsearch.jdbc.types;

import java.sql.SQLException;
import java.util.Map;

public class DoubleType implements TypeHelper<Double> {

    public static final DoubleType INSTANCE = new DoubleType();

    private DoubleType() {

    }

    @Override
    public Double fromValue(Object value, Map<String, Object> conversionParams) throws SQLException {
        if (value == null) {
            return (double) 0;
        }
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof String) {
            return asDouble((String) value);
        } else if (value instanceof Number) {
            return asDouble((Number) value);
        } else {
            throw objectConversionException(value);
        }
    }

    private Double asDouble(String value) throws SQLException {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException nfe) {
            throw stringConversionException(value, nfe);
        }
    }

    private Double asDouble(Number value) throws SQLException {
        return value.doubleValue();
    }

    @Override
    public String getTypeName() {
        return "Double";
    }
}
