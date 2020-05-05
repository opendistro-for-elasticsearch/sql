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

import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.esdomain.mapping.FieldMappings;
import com.amazon.opendistroforelasticsearch.sql.esdomain.mapping.TypeMappings;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * Formatter to transform date fields into a consistent format for consumption by clients.
 */
public class DateFieldFormatter {
    private static final Logger LOG = LogManager.getLogger(DateFieldFormatter.class);
    private static final String FORMAT_JDBC = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String FORMAT_DELIMITER = "\\|\\|";

    private static final String FORMAT_DOT_DATE_AND_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String FORMAT_DOT_KIBANA_SAMPLE_DATA_LOGS_EXCEPTION = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String FORMAT_DOT_KIBANA_SAMPLE_DATA_FLIGHTS_EXCEPTION = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String FORMAT_DOT_KIBANA_SAMPLE_DATA_FLIGHTS_EXCEPTION_NO_TIME = "yyyy-MM-dd'T'";
    private static final String FORMAT_DOT_KIBANA_SAMPLE_DATA_ECOMMERCE_EXCEPTION = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private static final String FORMAT_DOT_DATE = DateFormat.getFormatString("date");

    private final Map<String, List<String>> dateFieldFormatMap;
    private final Map<String, String> fieldAliasMap;
    private Set<String> dateColumns;

    public DateFieldFormatter(String indexName, List<Schema.Column> columns, Map<String, String> fieldAliasMap) {
        this.dateFieldFormatMap = getDateFieldFormatMap(indexName);
        this.dateColumns = getDateColumns(columns);
        this.fieldAliasMap = fieldAliasMap;
    }

    @VisibleForTesting
    protected DateFieldFormatter(Map<String, List<String>> dateFieldFormatMap,
                                 List<Schema.Column> columns,
                                 Map<String, String> fieldAliasMap) {
        this.dateFieldFormatMap = dateFieldFormatMap;
        this.dateColumns = getDateColumns(columns);
        this.fieldAliasMap = fieldAliasMap;
    }

    /**
     * Apply the JDBC date format ({@code yyyy-MM-dd HH:mm:ss.SSS}) to date values in the current row.
     *
     * @param rowSource The row in which to format the date values.
     */
    public void applyJDBCDateFormat(Map<String, Object> rowSource) {
        for (String columnName : dateColumns) {
            Object columnOriginalDate = rowSource.get(columnName);
            if (columnOriginalDate == null) {
                // Don't try to parse null date values
                continue;
            }

            List<String> formats = getFormatsForColumn(columnName);
            if (formats == null) {
                LOG.warn("Could not determine date formats for column {}; returning original value", columnName);
                continue;
            }

            Date date = parseDateString(formats, columnOriginalDate.toString());
            if (date != null) {
                rowSource.put(columnName, DateFormat.getFormattedDate(date, FORMAT_JDBC));
                break;
            } else {
                LOG.warn("Could not parse date value; returning original value");
            }
        }
    }

    private List<String> getFormatsForColumn(String columnName) {
        // Handle special cases for column names
        if (fieldAliasMap.get(columnName) != null) {
            // Column was aliased, and we need to find the base name for the column
            columnName = fieldAliasMap.get(columnName);
        } else if (columnName.split("\\.").length == 2) {
            // Column is part of a join, and is qualified by the table alias
            columnName = columnName.split("\\.")[1];
        }
        return dateFieldFormatMap.get(columnName);
    }

    private Set<String> getDateColumns(List<Schema.Column> columns) {
        return columns.stream()
            .filter(column -> column.getType().equals(Schema.Type.DATE.nameLowerCase()))
            .map(Schema.Column::getName)
            .collect(Collectors.toSet());
    }

    private Map<String, List<String>> getDateFieldFormatMap(String indexName) {
        LocalClusterState state = LocalClusterState.state();
        Map<String, List<String>> formatMap = new HashMap<>();

        String[] indices = indexName.split("\\|");
        Collection<TypeMappings> typeProperties = state.getFieldMappings(indices)
            .allMappings();

        for (TypeMappings mappings: typeProperties) {
            FieldMappings fieldMappings = mappings.firstMapping();
            for (Map.Entry<String, Map<String, Object>> field : fieldMappings.data().entrySet()) {
                String fieldName = field.getKey();
                Map<String, Object> properties = field.getValue();

                if (properties.containsKey("format")) {
                    formatMap.put(fieldName, getFormatsFromProperties(properties.get("format").toString()));
                } else {
                    // Give all field types a format, since operations such as casts
                    // can change the output type for a field to `date`.
                    formatMap.put(fieldName, getFormatsFromProperties("date_optional_time"));
                }
            }
        }

        return formatMap;
    }

    private List<String> getFormatsFromProperties(String formatProperty) {
        String[] formats = formatProperty.split(FORMAT_DELIMITER);
        return Arrays.asList(formats);
    }

    private Date parseDateString(List<String> formats, String columnOriginalDate) {
        TimeZone originalDefaultTimeZone = TimeZone.getDefault();
        Date parsedDate = null;

        // Apache Commons DateUtils uses the default TimeZone for the JVM when parsing.
        // However, since all dates on Elasticsearch are stored as UTC, we need to
        // parse these values using the UTC timezone.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        for (String columnFormat : formats) {
            try {
                switch (columnFormat) {
                    case "date_optional_time":
                    case "strict_date_optional_time":
                        parsedDate = DateUtils.parseDate(
                            columnOriginalDate,
                            FORMAT_DOT_KIBANA_SAMPLE_DATA_LOGS_EXCEPTION,
                            FORMAT_DOT_KIBANA_SAMPLE_DATA_FLIGHTS_EXCEPTION,
                            FORMAT_DOT_KIBANA_SAMPLE_DATA_FLIGHTS_EXCEPTION_NO_TIME,
                            FORMAT_DOT_KIBANA_SAMPLE_DATA_ECOMMERCE_EXCEPTION,
                            FORMAT_DOT_DATE_AND_TIME,
                            FORMAT_DOT_DATE);
                        break;
                    case "epoch_millis":
                        parsedDate = new Date(Long.parseLong(columnOriginalDate));
                        break;
                    case "epoch_second":
                        parsedDate = new Date(Long.parseLong(columnOriginalDate) * 1000);
                        break;
                    default:
                        String formatString = DateFormat.getFormatString(columnFormat);
                        if (formatString == null) {
                            // Custom format; take as-is
                            formatString = columnFormat;
                        }
                        parsedDate = DateUtils.parseDate(columnOriginalDate, formatString);
                }
            } catch (ParseException | NumberFormatException e) {
                LOG.warn(String.format("Could not parse date string %s as %s", columnOriginalDate, columnFormat));
            }
        }
        // Reset default timezone after parsing
        TimeZone.setDefault(originalDefaultTimeZone);

        return parsedDate;
    }
}
