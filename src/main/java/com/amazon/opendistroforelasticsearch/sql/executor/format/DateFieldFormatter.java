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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Formatter to transform date fields into a consistent format for consumption by clients.
 */
public class DateFieldFormatter {
    private static final Logger LOG = LogManager.getLogger(DateFieldFormatter.class);
    private static final String FORMAT_JDBC = "yyyy-MM-dd HH:mm:ss.SSS";

    private static final String FORMAT_DOT_DATE_AND_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String FORMAT_DOT_KIBANA_SAMPLE_DATA_LOGS_EXCEPTION = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String FORMAT_DOT_KIBANA_SAMPLE_DATA_FLIGHTS_EXCEPTION = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String FORMAT_DOT_KIBANA_SAMPLE_DATA_ECOMMERCE_EXCEPTION = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private static final String FORMAT_DOT_DATE = "yyyy-MM-dd";

    private final Map<String, String> dateFieldFormatMap;
    private final Map<String, String> fieldAliasMap;
    private Set<String> dateColumns;

    public DateFieldFormatter(String indexName, List<Schema.Column> columns, Map<String, String> fieldAliasMap) {
        this.dateFieldFormatMap = getDateFieldFormatMap(indexName);
        this.dateColumns = getDateColumns(columns);
        this.fieldAliasMap = fieldAliasMap;
    }

    @VisibleForTesting
    protected DateFieldFormatter(Map<String, String> dateFieldFormatMap,
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

            String columnFormat = getFormatForColumn(columnName);
            if (columnFormat == null) {
                LOG.warn("Could not determine date format for column {}; returning original value", columnName);
                continue;
            }
            DateFormat format = DateFormat.valueOf(columnFormat.toUpperCase());

            Date date = parseDateString(format, columnOriginalDate.toString());
            if (date != null) {
                rowSource.put(columnName, DateFormat.getFormattedDate(date, FORMAT_JDBC));
            } else {
                LOG.warn("Could not parse date value; returning original value");
            }
        }
    }

    private String getFormatForColumn(String columnName) {
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

    private Map<String, String> getDateFieldFormatMap(String indexName) {
        LocalClusterState state = LocalClusterState.state();
        Map<String, String> formatMap = new HashMap<>();

        String[] indices = indexName.split("\\|");
        Collection<TypeMappings> typeProperties = state.getFieldMappings(indices)
            .allMappings();

        for (TypeMappings mappings: typeProperties) {
            FieldMappings fieldMappings = mappings.firstMapping();
            for (Map.Entry<String, Map<String, Object>> field : fieldMappings.data().entrySet()) {
                String fieldName = field.getKey();
                Map<String, Object> properties = field.getValue();

                if (properties.containsKey("format")) {
                    formatMap.put(fieldName, properties.get("format").toString());
                } else {
                    // Give all field types a format, since operations such as casts
                    // can change the output type for a field to `date`.
                    formatMap.put(fieldName, "date_optional_time");
                }
            }
        }

        return formatMap;
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
