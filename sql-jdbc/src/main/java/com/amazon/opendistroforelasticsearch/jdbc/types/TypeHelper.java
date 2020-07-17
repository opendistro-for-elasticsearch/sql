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

import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Provides conversion of Object instances to Java type T where possible.
 * <p>
 * Used by {@link TypeConverter} instances to perform object cross conversions.
 *
 * @param <T> The Java type to which conversion is provided.
 */
public interface TypeHelper<T> {
    default SQLDataException stringConversionException(String value, Throwable cause) {
        if (cause != null)
            return new SQLDataException(String.format("Can not parse %s as a %s", value, getTypeName()), cause);
        else
            return new SQLDataException(String.format("Can not parse %s as a %s", value, getTypeName()));
    }

    default SQLDataException objectConversionException(Object value) {
        if (value == null) {
            return new SQLDataException(
                    String.format("Can not return null value as a %s", getTypeName()));
        } else {
            return new SQLDataException(
                    String.format("Can not return value of type %s as a %s",
                            value.getClass().getName(), getTypeName()));
        }
    }

    default SQLDataException valueOutOfRangeException(Object value) {
        return new SQLDataException(
                String.format("Object value %s out of range for type %s", value, getTypeName()));

    }

    /**
     * Returns an Object as an equivalent instance of type T
     *
     * @param value Object instance to convert
     * @param conversionParams Optional parameters to use for conversion
     *
     * @return instance of type T
     *
     * @throws SQLException if there is a problem in carrying out the conversion
     */
    T fromValue(Object value, Map<String, Object> conversionParams) throws SQLException;

    /**
     * Indicative name of the type T
     *
     * @return
     */
    String getTypeName();
}
