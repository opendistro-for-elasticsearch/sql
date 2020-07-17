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

public class FloatType extends NumberType<Float> {

    public static final FloatType INSTANCE = new FloatType();

    private FloatType() {

    }

    @Override
    public Float fromValue(Object value, Map<String, Object> conversionParams) throws SQLException {
        if (value == null) {
            return (float) 0;
        }
        if (value instanceof Float) {
            return (Float) value;
        } else if (value instanceof String) {
            return asFloat((String) value);
        } else if (value instanceof Number) {
            return asFloat((Number) value);
        } else {
            throw objectConversionException(value);
        }
    }

    private Float asFloat(String value) throws SQLException {
        try {
            return asFloat(Double.valueOf(value));
        } catch (NumberFormatException nfe) {
            throw stringConversionException(value, nfe);
        }
    }

    private Float asFloat(Number value) throws SQLException {
        return (float) getDoubleValueWithinBounds(value, -Float.MAX_VALUE, Float.MAX_VALUE);
    }

    @Override
    public String getTypeName() {
        return "Float";
    }

    @Override
    public boolean roundOffValue() {
        return false;
    }
}
