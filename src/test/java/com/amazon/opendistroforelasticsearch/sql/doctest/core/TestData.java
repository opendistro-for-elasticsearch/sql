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

package com.amazon.opendistroforelasticsearch.sql.doctest.core;

import com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;

import java.io.File;

/**
 * Test data for document generation
 */
public class TestData {

    public static final String TEST_DATA_FOLDER_ROOT = "src/test/resources/doctest/testdata/";

    private final String[] testFilePaths;

    public TestData(String[] testFilePaths) {
        this.testFilePaths = testFilePaths;
    }

    public void loadToES(DocTest test) {
        for (String filePath : testFilePaths) {
            try {
                TestUtils.loadBulk(test.client(), TEST_DATA_FOLDER_ROOT + filePath, indexName(filePath));
            } catch (Exception e) {
                throw new IllegalStateException(StringUtils.format(
                    "Failed to load test filePath from %s", filePath), e);
            }
            test.ensureGreen(indexName(filePath));
        }
    }

    private String indexName(String filePath) {
        return filePath.substring(
            filePath.lastIndexOf(File.separatorChar) + 1,
            filePath.lastIndexOf('.')
        );
    }

}
