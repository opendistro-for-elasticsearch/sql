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

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static org.hamcrest.Matchers.equalTo;

/**
 * Tests to cover requests with "?format=csv" parameter
 */
public class GetEndpointQueryIT extends SQLIntegTestCase {

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
    }

    @Test
    public void unicodeTermInQuery() throws IOException  {

        // NOTE: There are unicode characters in name, not just whitespace.
        final String name = "盛虹";
        final String query = String.format(Locale.ROOT, "SELECT _id, firstname FROM %s " +
                "WHERE firstname=matchQuery('%s') LIMIT 2", TEST_INDEX_ACCOUNT, name);

        final JSONObject result = executeQueryWithGetRequest(query);
        final JSONArray hits = getHits(result);
        Assert.assertThat(hits.length(), equalTo(1));
        Assert.assertThat(hits.query("/0/_id"), equalTo("919"));
        Assert.assertThat(hits.query("/0/_source/firstname"), equalTo(name));
    }
}
