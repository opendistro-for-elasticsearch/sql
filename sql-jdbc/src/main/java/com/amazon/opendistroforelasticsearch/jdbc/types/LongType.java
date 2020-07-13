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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Map;

public class LongType extends NumberType<Long> {

    public static final LongType INSTANCE = new LongType();

    private LongType() {

    }

    @Override
    public Long fromValue(Object value, Map<String, Object> conversionParams) throws SQLException {
        if (value == null) {
            return (long) 0;
        }
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof String) {
            return asLong((String) value);
        } else if (value instanceof Number) {
            return asLong((Number) value);
        } else {
            throw objectConversionException(value);
        }
    }

    private Long asLong(String value) throws SQLException {
        try {
            if (value.length() > 14) {
                // more expensive conversion but
                // needed to preserve precision for such large numbers
                BigDecimal bd = new BigDecimal(value);
                bd = bd.setScale(0, RoundingMode.HALF_UP);
                return bd.longValueExact();
            } else {
                return asLong(Double.valueOf(value));
            }

        } catch (ArithmeticException | NumberFormatException ex) {
            throw stringConversionException(value, ex);
        }
    }

    private Long asLong(Number value) throws SQLException {
        return (long) getDoubleValueWithinBounds(value, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    @Override
    public String getTypeName() {
        return "Long";
    }
}
