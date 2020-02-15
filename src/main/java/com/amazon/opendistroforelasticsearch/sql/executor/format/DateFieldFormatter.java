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

package com.amazon.opendistroforelasticsearch.sql.executor.format;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DateFieldFormatter {
    private static final Logger LOG = LogManager.getLogger(DateFieldFormatter.class);
    private static final String FORMAT_JDBC = "yyyy-MM-dd HH:mm:ss.SSS";

    private static final String FORMAT_DOT_DATE_AND_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String FORMAT_DOT_KIBANA_SAMPLE_DATA_LOGS_EXCEPTION = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String FORMAT_DOT_KIBANA_SAMPLE_DATA_FLIGHTS_EXCEPTION = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String FORMAT_DOT_KIBANA_SAMPLE_DATA_ECOMMERCE_EXCEPTION = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private static final String FORMAT_DOT_DATE = "yyyy-MM-dd";

    private final Map<String, String> dateFieldFormatMap;
    private List<Schema.Column> columns;

    public DateFieldFormatter(Map<String, String> dateFieldFormatMap, List<Schema.Column> columns) {
        this.dateFieldFormatMap = dateFieldFormatMap;
        this.columns = columns;
    }

    public void applyJDBCDateFormat(Map<String, Object> rowSource) {
        for (Schema.Column column : columns) {
            String columnType = column.getType();
            String columnName = column.getName();

            if (columnType.equals(Schema.Type.DATE.nameLowerCase())) {
                String columnFormat = dateFieldFormatMap.get(columnName);
                DateFormat format = DateFormat.valueOf(columnFormat.toUpperCase());

                Object columnOriginalDate = rowSource.get(columnName);
                if (columnOriginalDate == null) {
                    // Don't try to parse null date values
                    continue;
                }

                Date date = parseDateString(format, columnOriginalDate.toString());
                if (date != null) {
                    rowSource.put(columnName, DateFormat.getFormattedDate(date, FORMAT_JDBC));
                } else {
                    LOG.warn("Could not parse date value; returning original value");
                }
            }
        }
    }

    private Date parseDateString(DateFormat format, String columnOriginalDate) {
        try {
            switch (format) {
                case DATE_OPTIONAL_TIME:
                    return DateUtils.parseDate(
                        columnOriginalDate,
                        FORMAT_DOT_KIBANA_SAMPLE_DATA_LOGS_EXCEPTION,
                        FORMAT_DOT_KIBANA_SAMPLE_DATA_FLIGHTS_EXCEPTION,
                        FORMAT_DOT_KIBANA_SAMPLE_DATA_ECOMMERCE_EXCEPTION,
                        FORMAT_DOT_DATE_AND_TIME,
                        FORMAT_DOT_DATE);
                case EPOCH_MILLIS:
                    return new Date(Long.parseLong(columnOriginalDate));
                case EPOCH_SECOND:
                    return new Date(Long.parseLong(columnOriginalDate) * 1000);
                default:
                    return DateUtils.parseDate(columnOriginalDate, format.getFormatString());
            }
        } catch (ParseException e) {
            LOG.error(
                String.format("Error parsing date string %s as %s", columnOriginalDate, format.nameLowerCase()),
                e);
        }
        return null;
    }

}
