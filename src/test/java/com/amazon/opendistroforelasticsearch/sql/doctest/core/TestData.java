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

import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.createIndexByRestClient;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getResourceFilePath;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.loadDataByRestClient;

/**
 * Test data for document generation
 */
public class TestData {

    public static final String MAPPINGS_FOLDER_ROOT = "src/test/resources/doctest/mappings/";
    public static final String TEST_DATA_FOLDER_ROOT = "src/test/resources/doctest/testdata/";

    private final String[] testFilePaths;

    public TestData(String[] testFilePaths) {
        this.testFilePaths = testFilePaths;
    }

    /**
     * Load test data in file to Elaticsearch cluster via client.
     * @param test  current test instance
     */
    public void loadToES(DocTest test) {
        for (String filePath : testFilePaths) {
            String indexName = indexName(filePath);
            try {
                createIndexByRestClient(test.restClient(), indexName, getIndexMapping(filePath));
                loadDataByRestClient(test.restClient(), indexName, TEST_DATA_FOLDER_ROOT + filePath);
            } catch (Exception e) {
                throw new IllegalStateException(StringUtils.format(
                    "Failed to load mapping and test filePath from %s", filePath), e);
            }
            test.ensureGreen(indexName);
        }
    }

    /**
     * Use file name (without file extension) as index name implicitly.
     * For example, for 'testdata/accounts.json', 'accounts' will be used.
     */
    private String indexName(String filePath) {
        return filePath.substring(
            filePath.lastIndexOf(File.separatorChar) + 1,
            filePath.lastIndexOf('.')
        );
    }

    private String getIndexMapping(String filePath) throws IOException {
        Path path = Paths.get(getResourceFilePath(MAPPINGS_FOLDER_ROOT + filePath));
        if (Files.notExists(path)) {
            return "";
        }
        return new String(Files.readAllBytes(path));
    }

}
