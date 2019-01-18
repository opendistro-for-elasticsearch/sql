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

package com.amazon.opendistro.sql.intgtest;

import com.amazon.opendistro.sql.SearchDao;
import com.amazon.opendistro.sql.exception.SqlParseException;
import com.amazon.opendistro.sql.rewriter.matchtoterm.VerificationException;
import com.amazon.opendistro.sql.query.QueryAction;
import com.amazon.opendistro.sql.query.SqlElasticRequestBuilder;
import com.google.common.io.Files;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.rest.RestStatus;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLFeatureNotSupportedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class TermQueryExplainTest {


    @Test
    public void testNonExistingIndex() throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        try {
            String result = explain("SELECT firstname, lastname " +
                "FROM elasticsearch_sql_test_fake_index " +
                "WHERE firstname = 'Leo'");
        } catch (IndexNotFoundException e) {
            assertThat(e.getMessage(), containsString("no such index"));
        }
    }

    @Test
    public void testNonResolvingIndexPattern()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        try {
            String result = explain("SELECT * " +
                "FROM elasticsearch_sql_test_blah_blah* " +
                "WHERE firstname = 'Leo'");
        } catch (VerificationException e) {
            assertThat(e.status(), equalTo(RestStatus.BAD_REQUEST));
            assertThat(e.getMessage(), containsString("Unknown index"));
        }
    }

    @Test
    public void testNonResolvingIndexPatternWithExistingIndex()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException  {
        String result = explain(String.format("SELECT * " +
            "FROM elasticsearch_sql_test_blah_blah*, %s " +
            "WHERE state = 'DC'", TestsConstants.TEST_INDEX_BANK));
        assertThat(result, containsString("\"term\":{\"state.keyword\""));
    }

    @Test
    public void testNonResolvingIndexPatternWithNonExistingIndex()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        try {
            String result = explain("SELECT firstname, lastname " +
                "FROM elasticsearch_sql_test_blah_blah*, another_fake_index " +
                "WHERE firstname = 'Leo'");
        } catch (IndexNotFoundException e) {
            assertThat(e.getMessage(), containsString("no such index"));
        }
    }

    @Test
    public void testNonIdenticalMappings()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        try {
            String result = explain(String.format("SELECT firstname, birthdate FROM %s, %s WHERE firstname = 'Leo' OR male = 'true'",
                TestsConstants.TEST_INDEX_BANK,
                TestsConstants.TEST_INDEX_ONLINE));
        } catch (VerificationException e) {
            assertThat(e.status(), equalTo(RestStatus.BAD_REQUEST));
            assertThat(e.getMessage(), containsString("When using multiple indices, the mappings must be identical."));
        }
    }

    @Test
    public void testIdenticalMappings ()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        String result = explain(String.format("SELECT firstname, birthdate, state FROM %s, %s WHERE state = 'WA' OR male = 'true'",
            TestsConstants.TEST_INDEX_BANK,
            TestsConstants.TEST_INDEX_BANK_TWO));
        assertThat(result, containsString("term"));
        assertThat(result, containsString("state.keyword"));
        assertThat(result, containsString("_source"));
    }

    @Test
    public void testIdenticalMappingsWithTypes()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        String result = explain(String.format("SELECT firstname, birthdate, state FROM %s, %s WHERE state = 'WA' OR male = 'true'",
            TestsConstants.TEST_INDEX_BANK + "/account",
            TestsConstants.TEST_INDEX_BANK_TWO + "/account_two"));
        assertThat(result, containsString("term"));
        assertThat(result, containsString("state.keyword"));
        assertThat(result, containsString("_source"));
    }


    @Test
    public void testIdenticalMappingsWithPartialType()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        String result = explain(String.format("SELECT firstname, birthdate, state FROM %s, %s WHERE state = 'WA' OR male = 'true'",
            TestsConstants.TEST_INDEX_BANK + "/account",
            TestsConstants.TEST_INDEX_BANK_TWO));
        assertThat(result, containsString("term"));
        assertThat(result, containsString("state.keyword"));
        assertThat(result, containsString("_source"));
    }

    @Test
    public void testTextFieldOnly()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        String result = explain(String.format("SELECT firstname, birthdate, state FROM %s WHERE firstname = 'Abbas'", TestsConstants.TEST_INDEX_BANK));
        assertThat(result, containsString("term"));
        assertThat(result, not(containsString("firstname.")));
    }


    @Test
    public void testTextAndKeywordAppendsKeywordAlias()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        String result = explain(String.format("SELECT firstname, birthdate, state FROM %s WHERE state = 'WA' OR lastname = 'Chen'", TestsConstants.TEST_INDEX_BANK));
        assertThat(result, containsString("term"));
        assertThat(result, containsString("state.keyword"));
        assertThat(result, not(containsString("lastname.")));
    }

    @Test
    public void testBooleanFieldNoKeywordAlias()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        String result = explain(String.format("SELECT * FROM %s WHERE male = 'false'", TestsConstants.TEST_INDEX_BANK));
        assertThat(result, containsString("term"));
        assertThat(result, not(containsString("male.")));
    }

    @Test
    public void testDateFieldNoKeywordAlias()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        String result = explain(String.format("SELECT * FROM %s WHERE birthdate = '2018-08-19'", TestsConstants.TEST_INDEX_BANK));
        assertThat(result, containsString("term"));
        assertThat(result, not(containsString("birthdate.")));
    }

    @Test
    public void testNumberNoKeywordAlias()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        String result = explain(String.format("SELECT * FROM %s WHERE age = 32", TestsConstants.TEST_INDEX_BANK));
        assertThat(result, containsString("term"));
        assertThat(result, not(containsString("age.")));
    }

    @Test
    public void inTestInWhere()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        String result = explain(String.format("select * from %s where state IN ('WA' , 'PA' , 'TN')", TestsConstants.TEST_INDEX_BANK));
        assertThat(result, containsString("term"));
        assertThat(result, containsString("state.keyword"));
    }

    @Test
    public void inTestInWhereSubquery()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        String result = explain(String.format("select * from %s where state IN (select state from %s where city = 'Nicholson')",
            TestsConstants.TEST_INDEX_BANK + "/account",
            TestsConstants.TEST_INDEX_BANK));
        assertThat(result, containsString("term"));
        assertThat(result, containsString("state.keyword"));
    }

    @Test
    public void testKeywordAliasGroupBy()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        String result = explain(String.format("SELECT firstname, state FROM %s GROUP BY firstname, state", TestsConstants.TEST_INDEX_BANK + "/account"));
        assertThat(result, containsString("term"));
        assertThat(result, containsString("state.keyword"));
        assertThat(result, not(containsString("lastname.")));
    }

    @Test
    public void testKeywordAliasOrderBy()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        String result = explain(String.format("SELECT * FROM %s ORDER BY state , lastname ", TestsConstants.TEST_INDEX_BANK));
        assertThat(result, containsString("\"state.keyword\":{\"order\":\"asc\""));
        assertThat(result, containsString("\"lastname\":{\"order\":\"asc\"}"));
    }


    @Ignore
    @Test
    public void testJoinWhere()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        String expectedOutput = Files.toString(new File("src/test/resources/expectedOutput/term_join_where"), StandardCharsets.UTF_8).replaceAll("\r","");
        String result = explain(String.format("SELECT a.firstname, a.lastname , b.city FROM %s a " +
            "JOIN %s b ON a.city = b.city where a.city IN ('Nicholson', 'Yardville')", TestsConstants.TEST_INDEX_ACCOUNT, TestsConstants.TEST_INDEX_ACCOUNT ));

        assertThat(result.replaceAll("\\s+",""), equalTo(expectedOutput.replaceAll("\\s+","")));
    }


    @Test
    public void testJoinAliasMissing()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        try {
            String result = explain(String.format("SELECT a.firstname, a.lastname , b.city FROM %s a " +
                " JOIN %s b ON a.city = b.city where city IN ('Nicholson', 'Yardville')", TestsConstants.TEST_INDEX_ACCOUNT, TestsConstants.TEST_INDEX_ACCOUNT));
        } catch (VerificationException e) {
            assertThat(e.status(), equalTo(RestStatus.BAD_REQUEST));
            assertThat(e.getMessage(), containsString("table alias or field name missing"));
        }

    }

    @Test
    public void testMultiQuery()
        throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        String expectedOutput = Files.toString(new File("src/test/resources/expectedOutput/term_union_where"), StandardCharsets.UTF_8).replaceAll("\r","");
        String result = explain(String.format("SELECT firstname FROM %s/account WHERE firstname = 'Amber' " +
            "UNION ALL SELECT dog_name as firstname FROM %s/dog WHERE holdersName = 'Hattie' OR dog_name = 'rex'",
            TestsConstants.TEST_INDEX_ACCOUNT,
            TestsConstants.TEST_INDEX_DOG));
        assertThat(result.replaceAll("\\s+",""), equalTo(expectedOutput.replaceAll("\\s+","")));
    }


    private String explain (String sql) throws IOException, SQLFeatureNotSupportedException, SqlParseException {
        SearchDao searchDao = MainTestSuite.getSearchDao();
        QueryAction queryAction = searchDao.explain(sql);
        SqlElasticRequestBuilder requestBuilder = queryAction.explain();
        return requestBuilder.explain();
    }
}