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

package com.amazon.opendistroforelasticsearch.sql.correctness.testset;

import java.util.ArrayList;
import java.util.List;

import static com.amazon.opendistroforelasticsearch.sql.utils.StringUtils.unquoteSingleField;

/**
 * Test data set
 */
public class TestDataSet {

    private final String tableName;
    private final String schema;
    private final List<String[]> dataRows;

    public TestDataSet(String tableName, String schemaFileContent, String dataFileContent) {
        this.tableName = tableName;
        this.schema = schemaFileContent;
        this.dataRows = splitBy(dataFileContent, ',');
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

    /** Split by separator and ignore escaped separator(s) in quoted string. */
    private List<String[]> splitBy(String content, char separator) {

        List<String[]> result = new ArrayList<>();
        for (String line : content.split("\\r?\\n")) {

            List<String> columns = new ArrayList<>();
            boolean isQuoted = false;
            int start = 0;
            for (int i = 0; i < line.length(); i++) {

                char c = line.charAt(i);
                if (c == separator) {
                    if (isQuoted) { // Ignore comma(s) in quoted string
                        continue;
                    }

                    String column = line.substring(start, i);
                    columns.add(unquoteSingleField(column, "\""));
                    start = i + 1;

                } else if (c == '\"') {
                    isQuoted = !isQuoted;
                }
            }

            columns.add(unquoteSingleField(line.substring(start), "\""));
            result.add(columns.toArray(new String[0]));
        }
        return result;
    }

}
