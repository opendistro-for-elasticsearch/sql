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

package com.amazon.opendistroforelasticsearch.sql.correctness.testfile;

import java.util.List;

/**
 * Test data set
 */
public class TestDataSet implements TestFile {

    private final String tableName;
    private final String schema;
    private final List<String[]> dataRows;

    public TestDataSet(String tableName, String schemaFilePath, String dataFilePath) {
        this.tableName = tableName;
        this.schema = content(schemaFilePath);
        this.dataRows = splitBy(content(dataFilePath), ",");
    }

    public String getTableName() {
        return tableName;
    }

    public String getSchema() {
        return schema;
    }

    public List<String[]> getDataRows() {
        return dataRows;
    }

}
