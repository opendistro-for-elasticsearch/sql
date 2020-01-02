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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Test query set
 */
public class TestQuerySet implements Iterable<String> {

    private List<String> queries;

    public TestQuerySet(String queryFileContent) {
        queries = lines(queryFileContent);
    }

    public TestQuerySet(String[] queries) {
        this.queries = Arrays.asList(queries);
    }

    @Override
    public Iterator<String> iterator() {
        return queries.iterator();
    }

    private List<String> lines(String content) {
        return Arrays.asList(content.split("\\r?\\n"));
    }

}
