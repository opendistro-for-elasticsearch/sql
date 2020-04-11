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
import com.amazon.opendistroforelasticsearch.sql.util.SqlParserUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
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
                IsEqualIgnoreCaseAndWhiteSpace.equalToIgnoreCaseAndWhiteSpace(expected));
    }

    @Test
    public void testFromSubqueryWithoutTermShouldPass() {
        String sql = "SELECT t.age as a FROM (SELECT age FROM semantics WHERE age = 10) t";
        String expected = sql;

        assertThat(rewriteTerm(sql),
                IsEqualIgnoreCaseAndWhiteSpace.equalToIgnoreCaseAndWhiteSpace(expected));
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

    /**
     * Tests if a string is equal to another string, ignore the case and whitespace.
     */
    public static class IsEqualIgnoreCaseAndWhiteSpace extends TypeSafeMatcher<String> {
        private final String string;

        public IsEqualIgnoreCaseAndWhiteSpace(String string) {
            if (string == null) {
                throw new IllegalArgumentException("Non-null value required");
            }
            this.string = string;
        }

        @Override
        public boolean matchesSafely(String item) {
            return ignoreCase(ignoreSpaces(string)).equals(ignoreCase(ignoreSpaces(item)));
        }

        @Override
        public void describeMismatchSafely(String item, Description mismatchDescription) {
            mismatchDescription.appendText("was ").appendValue(item);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a string equal to ")
                    .appendValue(string)
                    .appendText(" ignore case and white space");
        }

        public String ignoreSpaces(String toBeStripped) {
            return toBeStripped.replaceAll("\\s+", "").trim();
        }

        public String ignoreCase(String toBeLower) {
            return toBeLower.toLowerCase();
        }

        public static Matcher<String> equalToIgnoreCaseAndWhiteSpace(String expectedString) {
            return new IsEqualIgnoreCaseAndWhiteSpace(expectedString);
        }
    }
}