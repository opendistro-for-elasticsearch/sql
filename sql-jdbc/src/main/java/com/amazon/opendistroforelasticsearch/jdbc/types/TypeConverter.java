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

public interface TypeConverter {

    /**
     * This method allows retrieving a column value as an instance of
     * a different class than the default Java class that the column's
     * JDBCType maps to.
     * <p>
     * This implements the aspect of the JDBC spec that specifies
     * multiple JDBCTypes on which a ResultSet getter method may be called.
     *
     * @param <T> Type of the Java Class
     * @param value Column value
     * @param clazz Instance of the Class to which the value needs to be
     *         converted
     * @param conversionParams Optional conversion parameters to use in
     *         the conversion
     *
     * @return Column value as an instance of type T
     *
     * @throws SQLException if the conversion is not supported or the
     *         conversion operation fails.
     */
    <T> T convert(Object value, Class<T> clazz, Map<String, Object> conversionParams) throws SQLException;

    default SQLDataException objectConversionException(Object value, Class clazz) {
        return new SQLDataException(String.format(
                "Can not convert object '%s' of type '%s' to type '%s'",
                value, value.getClass().getName(), clazz.getName()));
    }
}
