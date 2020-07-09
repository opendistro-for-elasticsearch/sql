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

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Map;

/**
 * Supports returning a java.sql.Date from a String (starting with yyyy-mm-dd)
 * or a Number value indicating epoch time in millis.
 */
public class DateType implements TypeHelper<Date> {

    public static final DateType INSTANCE = new DateType();

    private DateType() {

    }

    @Override
    public Date fromValue(Object value, Map<String, Object> conversionParams) throws SQLException {
        if (value == null) {
            return null;
        }
        Calendar calendar = conversionParams != null ? (Calendar) conversionParams.get("calendar") : null;
        if (value instanceof Date) {
            return asDate((Date) value, calendar);
        } else if (value instanceof String) {
            return asDate((String) value, calendar);
        } else if (value instanceof Number) {
            return asDate((Number) value);
        } else {
            throw objectConversionException(value);
        }
    }

    public java.sql.Date asDate(Date value, Calendar calendar) throws SQLException {
        if (calendar == null) {
            return value;
        } else {
            return localDatetoSqlDate(value.toLocalDate(), calendar);
        }
    }

    public java.sql.Date asDate(String value, Calendar calendar) throws SQLException {
        try {
            if (calendar == null) {
                return java.sql.Date.valueOf(toLocalDate(value));
            } else {
                return localDatetoSqlDate(toLocalDate(value), calendar);
            }
        } catch (DateTimeParseException dpe) {
            throw stringConversionException(value, dpe);
        }
    }

    private Date localDatetoSqlDate(LocalDate localDate, Calendar calendar) {
        calendar.set(localDate.getYear(),
                localDate.getMonthValue() - 1,
                localDate.getDayOfMonth(), 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new java.sql.Date(calendar.getTimeInMillis());
    }

    public java.sql.Date asDate(Number value) {
        return new java.sql.Date(value.longValue());
    }

    private LocalDate toLocalDate(String value) throws SQLException {
        if (value == null || value.length() < 10)
            throw stringConversionException(value, null);
        return LocalDate.parse(value.substring(0, 10));
    }

    @Override
    public String getTypeName() {
        return "Date";
    }

}
