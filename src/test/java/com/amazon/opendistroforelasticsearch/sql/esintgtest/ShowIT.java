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

package com.amazon.opendistroforelasticsearch.sql.esintgtest;

import org.elasticsearch.client.AdminClient;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

public class ShowIT extends SQLIntegTestCase {

    @Override
    protected void setupSuiteScopeCluster() {

        AdminClient adminClient = this.admin();

        // Note: not using the existing TEST_INDEX_* indices, since underscore in the names causes issues
        TestUtils.createTestIndex(adminClient, "abcdefg", "doc", null);
        TestUtils.createTestIndex(adminClient, "abcdefghijk", "doc", null);
        TestUtils.createTestIndex(adminClient, "abcdijk", "doc", null);
    }

    @Test
    public void showAll_matchAll() throws IOException {

        showIndexTest("%", 3, false);
    }

    @Test
    public void showIndex_matchPrefix() throws IOException {

        showIndexTest("abcdefg" + "%", 2, true);
    }

    @Test
    public void showIndex_matchSuffix() throws IOException {

        showIndexTest("%ijk", 2, true);
    }

    @Test
    public void showIndex_matchExact() throws IOException {

        showIndexTest("abcdefg", 1, true);
    }

    private void showIndexTest(String querySuffix, int expectedMatches, boolean exactMatch) throws IOException {

        final String query = "SHOW TABLES LIKE " + querySuffix;
        JSONObject result = executeQuery(query);

        if (exactMatch) {
            Assert.assertThat(result.length(), equalTo(expectedMatches));
        } else {
            Assert.assertThat(result.length(), greaterThanOrEqualTo(expectedMatches));
        }
        for (String indexName : result.keySet()) {
            Assert.assertTrue(result.getJSONObject(indexName).has("mappings"));
        }
    }
}
