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

package com.amazon.opendistroforelasticsearch.jdbc.internal.results;

import com.amazon.opendistroforelasticsearch.jdbc.types.ElasticsearchType;

import java.util.List;

/**
 * Represents the schema for a query result
 */
public class Schema {
    private final List<ColumnMetaData> columnMetaDataList;
    private final int numberOfColumns;

    public Schema(List<ColumnMetaData> columnMetaDataList) {
        this.columnMetaDataList = columnMetaDataList;
        this.numberOfColumns = columnMetaDataList != null ? columnMetaDataList.size() : 0;
    }

    /**
     * @return Number of columns in result
     */
    public int getNumberOfColumns() {
        return this.numberOfColumns;
    }

    /**
     * Returns {@link ColumnMetaData} for a specific column in the result
     *
     * @param index the index of the column to return metadata for
     *
     * @return {@link ColumnMetaData} for the specified column
     */
    public ColumnMetaData getColumnMetaData(int index) {
        return columnMetaDataList.get(index);
    }

    /**
     * Returns the {@link ElasticsearchType} corresponding to a specific
     * column in the result.
     *
     * @param index the index of the column to return the type for
     *
     * @return {@link ElasticsearchType} for the specified column
     */
    public ElasticsearchType getElasticsearchType(int index) {
        return columnMetaDataList.get(index).getEsType();
    }
}
