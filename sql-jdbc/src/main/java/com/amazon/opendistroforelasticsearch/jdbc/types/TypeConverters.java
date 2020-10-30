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
import java.sql.JDBCType;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * For columns that are mapped to a certain JDBCType, the
 * TypeConverter instances here provide utility functions
 * to retrieve the values in these columns as instances
 * of specified Java classes.
 *
 * These conversions are needed for example when ResultSet.getString()
 * is invoked on a column that is internally a JDBCType.FLOAT or
 * ResultSet.getFloat() is invoked on a column that is internally a
 * JDBCType.VARCHAR
 */
public class TypeConverters {

    private static Map<JDBCType, TypeConverter> tcMap = new HashMap<>();

    static {
        // TODO - JDBCType.VARBINARY - byte[] -> Try ES data type
        tcMap.put(JDBCType.TIMESTAMP, new TimestampTypeConverter());
        tcMap.put(JDBCType.DATE, new DateTypeConverter());
        tcMap.put(JDBCType.TIME, new TimeTypeConverter());

        tcMap.put(JDBCType.FLOAT, new FloatTypeConverter());
        tcMap.put(JDBCType.REAL, new RealTypeConverter());
        tcMap.put(JDBCType.DOUBLE, new DoubleTypeConverter());

        tcMap.put(JDBCType.VARCHAR, new VarcharTypeConverter());

        tcMap.put(JDBCType.BOOLEAN, new BooleanTypeConverter());

        tcMap.put(JDBCType.TINYINT, new TinyIntTypeConverter());
        tcMap.put(JDBCType.SMALLINT, new SmallIntTypeConverter());
        tcMap.put(JDBCType.INTEGER, new IntegerTypeConverter());
        tcMap.put(JDBCType.BIGINT, new BigIntTypeConverter());

        tcMap.put(JDBCType.BINARY, new BinaryTypeConverter());
    }

    public static TypeConverter getInstance(JDBCType jdbcType) {
        return tcMap.get(jdbcType);
    }

    public static class TimestampTypeConverter extends BaseTypeConverter {

        private static final Set<Class> supportedJavaClasses = Collections.unmodifiableSet(
                new HashSet<>(Arrays.asList(
                        String.class, Timestamp.class
                )));

        private TimestampTypeConverter() {

        }

        @Override
        public Class getDefaultJavaClass() {
            return Timestamp.class;
        }

        @Override
        public Set<Class> getSupportedJavaClasses() {
            return supportedJavaClasses;
        }

    }

    public static class DateTypeConverter extends BaseTypeConverter {

        private static final Set<Class> supportedJavaClasses = Collections.unmodifiableSet(
                new HashSet<>(Arrays.asList(
                        String.class, Date.class
                )));

        private DateTypeConverter() {

        }

        @Override
        public Class getDefaultJavaClass() {
            return Date.class;
        }

        @Override
        public Set<Class> getSupportedJavaClasses() {
            return supportedJavaClasses;
        }

    }

    public static class TimeTypeConverter extends BaseTypeConverter {

        private static final Set<Class> supportedJavaClasses = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                String.class, Time.class
            )));

        private TimeTypeConverter() {

        }

        @Override
        public Class getDefaultJavaClass() {
            return Time.class;
        }

        @Override
        public Set<Class> getSupportedJavaClasses() {
            return supportedJavaClasses;
        }
    }

    public static class VarcharTypeConverter extends BaseTypeConverter {

        private static final Set<Class> supportedJavaClasses = Collections.unmodifiableSet(
                new HashSet<>(Arrays.asList(
                        String.class, Timestamp.class, java.sql.Date.class,
                        Byte.class, Short.class, Integer.class, Long.class,
                        Boolean.class
                )));

        VarcharTypeConverter() {

        }

        @Override
        public Class getDefaultJavaClass() {
            return String.class;
        }

        @Override
        public Set<Class> getSupportedJavaClasses() {
            return supportedJavaClasses;
        }
    }

    public static class DoubleTypeConverter extends BaseTypeConverter {

        private static final Set<Class> supportedJavaClasses = Collections.unmodifiableSet(
                new HashSet<>(Arrays.asList(
                        String.class, Float.class, Double.class,
                        Byte.class, Short.class, Integer.class, Long.class
                )));

        private DoubleTypeConverter() {

        }

        @Override
        public Class getDefaultJavaClass() {
            return Double.class;
        }

        @Override
        public Set<Class> getSupportedJavaClasses() {
            return supportedJavaClasses;
        }
    }

    public static class RealTypeConverter extends DoubleTypeConverter {

        RealTypeConverter() {

        }

        @Override
        public Class getDefaultJavaClass() {
            return Float.class;
        }
    }

    public static class FloatTypeConverter extends DoubleTypeConverter {

        FloatTypeConverter() {

        }
    }

    public static class BooleanTypeConverter extends BaseTypeConverter {

        private static final Set<Class> supportedJavaClasses = Collections.unmodifiableSet(
                new HashSet<>(Arrays.asList(
                        Boolean.class, String.class
                )));

        BooleanTypeConverter() {

        }

        @Override
        public Class getDefaultJavaClass() {
            return Boolean.class;
        }

        @Override
        public Set<Class> getSupportedJavaClasses() {
            return supportedJavaClasses;
        }
    }

    public static class BinaryTypeConverter extends BaseTypeConverter {

        private static final Set<Class> supportedJavaClasses = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                String.class
            )));

        @Override
        public Class getDefaultJavaClass() {
            return String.class;
        }

        @Override
        public Set<Class> getSupportedJavaClasses() {
            return supportedJavaClasses;
        }
    }

    public static class IntegerTypeConverter extends BaseTypeConverter {

        private static final Set<Class> supportedJavaClasses = Collections.unmodifiableSet(
                new HashSet<>(Arrays.asList(
                        Float.class, Double.class,
                        Byte.class, Short.class, Integer.class, Long.class,
                        String.class
                )));

        IntegerTypeConverter() {

        }

        @Override
        public Class getDefaultJavaClass() {
            return Integer.class;
        }

        @Override
        public Set<Class> getSupportedJavaClasses() {
            return supportedJavaClasses;
        }
    }

    public static class BigIntTypeConverter extends IntegerTypeConverter {

        BigIntTypeConverter() {

        }

        @Override
        public Class getDefaultJavaClass() {
            return Long.class;
        }
    }

    public static class TinyIntTypeConverter extends IntegerTypeConverter {

        TinyIntTypeConverter() {

        }

        @Override
        public Class getDefaultJavaClass() {
            return Byte.class;
        }
    }

    public static class SmallIntTypeConverter extends IntegerTypeConverter {

        SmallIntTypeConverter() {

        }

        @Override
        public Class getDefaultJavaClass() {
            return Short.class;
        }
    }
}
