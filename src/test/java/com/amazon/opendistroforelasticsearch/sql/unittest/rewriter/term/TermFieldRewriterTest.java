/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.unittest.rewriter.term;


import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.rewriter.matchtoterm.TermFieldRewriter;
import com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils;
import com.amazon.opendistroforelasticsearch.sql.util.SqlParserUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.net.URL;

import static com.amazon.opendistroforelasticsearch.sql.util.CheckScriptContents.mockLocalClusterState;
import static org.hamcrest.MatcherAssert.assertThat;

public class TermFieldRewriterTest {
    private static final String TEST_MAPPING_FILE = "mappings/semantics.json";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() throws IOException {
        URL url = Resources.getResource(TEST_MAPPING_FILE);
        String mappings = Resources.toString(url, Charsets.UTF_8);
        LocalClusterState.state(null);
        mockLocalClusterState(mappings);
    }

    @Test
    public void testFromSubqueryShouldPass() {
        String sql = "SELECT t.age as a FROM (SELECT age FROM semantics WHERE employer = 'david') t";
        String expected = "SELECT t.age as a FROM (SELECT age FROM semantics WHERE employer.keyword = 'david') t";

        assertThat(rewriteTerm(sql),
                MatcherUtils.IsEqualIgnoreCaseAndWhiteSpace.equalToIgnoreCaseAndWhiteSpace(expected));
    }

    @Test
    public void testFromSubqueryWithoutTermShouldPass() {
        String sql = "SELECT t.age as a FROM (SELECT age FROM semantics WHERE age = 10) t";
        String expected = sql;

        assertThat(rewriteTerm(sql),
                MatcherUtils.IsEqualIgnoreCaseAndWhiteSpace.equalToIgnoreCaseAndWhiteSpace(expected));
    }

    private String rewriteTerm(String sql) {
        SQLQueryExpr sqlQueryExpr = SqlParserUtils.parse(sql);
        sqlQueryExpr.accept(new TermFieldRewriter());
        return SQLUtils.toMySqlString(sqlQueryExpr)
                .replaceAll("[\\n\\t]+", " ")
                .replaceAll("^\\(", " ")
                .replaceAll("\\)$", " ")
                .trim();
    }
}