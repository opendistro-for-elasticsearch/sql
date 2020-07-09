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

public class ShortType extends NumberType<Short> {

    public static final ShortType INSTANCE = new ShortType();

    private ShortType() {

    }

    @Override
    public Short fromValue(Object value, Map<String, Object> conversionParams) throws SQLException {
        if (value == null) {
            return (short) 0;
        }
        if (value instanceof Short) {
            return (Short) value;
        } else if (value instanceof String) {
            return asShort((String) value);
        } else if (value instanceof Number) {
            return asShort((Number) value);
        } else {
            throw objectConversionException(value);
        }
    }

    private Short asShort(String value) throws SQLException {
        try {
            return asShort(Double.valueOf(value));
        } catch (NumberFormatException nfe) {
            throw stringConversionException(value, nfe);
        }
    }

    private Short asShort(Number value) throws SQLException {
        return (short) getDoubleValueWithinBounds(value, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    @Override
    public String getTypeName() {
        return "Short";
    }

    public static void main(String[] args) {
        Short.valueOf("45.50");
        System.out.println(Math.round(100.45D));
        System.out.println(Math.round(100.95f));
    }
}
