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

import org.elasticsearch.client.ResponseException;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import org.junit.rules.ExpectedException;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;

/**
 * Tests to cover requests with "?format=csv" parameter
 */
public class GetEndpointQueryIT extends SQLIntegTestCase {

    @Rule
    public ExpectedException rule = ExpectedException.none();

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
    }

    @Test
    public void getEndPointShouldBeInvalid() throws IOException {
        rule.expect(ResponseException.class);
        rule.expectMessage("Incorrect HTTP method");
        String query = "select name from " + TEST_INDEX_ACCOUNT;
        executeQueryWithGetRequest(query);
    }
}
