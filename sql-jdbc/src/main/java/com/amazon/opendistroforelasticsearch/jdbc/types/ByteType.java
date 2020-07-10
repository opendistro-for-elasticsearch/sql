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

public class ByteType extends NumberType<Byte> {
    public static final ByteType INSTANCE = new ByteType();

    private ByteType() {

    }

    public Byte fromValue(Object value, Map<String, Object> conversionParams) throws SQLException {
        if (value == null) {
            return (byte) 0;
        }
        if (value instanceof Byte) {
            return (Byte) value;
        } else if (value instanceof String) {
            return asByte((String) value);
        } else if (value instanceof Number) {
            return asByte((Number) value);
        } else {
            throw objectConversionException(value);
        }
    }

    private Byte asByte(String value) throws SQLException {
        try {
            return asByte(Double.valueOf(value));
        } catch (NumberFormatException nfe) {
            throw stringConversionException(value, nfe);
        }
    }

    private Byte asByte(Number value) throws SQLException {
        return (byte) getDoubleValueWithinBounds(value, Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    @Override
    public String getTypeName() {
        return "Byte";
    }
}
