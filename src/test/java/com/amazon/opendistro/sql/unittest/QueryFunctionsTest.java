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

package com.amazon.opendistro.sql.unittest;

import com.alibaba.druid.sql.parser.ParserException;
import com.amazon.opendistro.sql.exception.SqlParseException;
import com.amazon.opendistro.sql.intgtest.TestsConstants;
import com.amazon.opendistro.sql.query.ESActionFactory;
import com.amazon.opendistro.sql.query.QueryAction;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.Mockito;
import com.amazon.opendistro.sql.util.CheckScriptContents;

import java.sql.SQLFeatureNotSupportedException;

import static org.elasticsearch.index.query.QueryBuilders.constantScoreQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;
import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class QueryFunctionsTest {

    private static final String SELECT_ALL = "SELECT *";
    private static final String FROM_ACCOUNTS = "FROM " + TestsConstants.TEST_INDEX_ACCOUNT + "/account";
    private static final String FROM_NESTED = "FROM " + TestsConstants.TEST_INDEX_NESTED_TYPE + "/nestedType";
    private static final String FROM_PHRASE = "FROM " + TestsConstants.TEST_INDEX_PHRASE + "/phrase";

    @Test
    public void query() {
        assertThat(
            query(
                FROM_ACCOUNTS,
                "WHERE QUERY('CA')"
            ),
            contains(
                queryStringQuery("CA")
            )
        );
    }

    @Test
    public void matchQueryRegularField() {
        assertThat(
            query(
                FROM_ACCOUNTS,
                "WHERE MATCH_QUERY(firstname, 'Ayers')"
            ),
            contains(
                matchQuery("firstname", "Ayers")
            )
        );
    }

    @Test
    public void matchQueryNestedField() {
        assertThat(
            query(
                FROM_NESTED,
                "WHERE MATCH_QUERY(NESTED(comment.data), 'aa')"
            ),
            contains(
                nestedQuery("comment", matchQuery("comment.data", "aa"), ScoreMode.None)
            )
        );
    }

    @Test
    public void scoreQuery() {
        assertThat(
            query(
                FROM_ACCOUNTS,
                "WHERE SCORE(MATCH_QUERY(firstname, 'Ayers'), 10)"
            ),
            contains(
                constantScoreQuery(
                    matchQuery("firstname", "Ayers")
                ).boost(10)
            )
        );
    }

    @Test
    public void scoreQueryWithNestedField() {
        assertThat(
            query(
                FROM_NESTED,
                "WHERE SCORE(MATCH_QUERY(NESTED(comment.data), 'ab'), 10)"
            ),
            contains(
                constantScoreQuery(
                    nestedQuery("comment", matchQuery("comment.data", "ab"), ScoreMode.None)
                ).boost(10)
            )
        );
    }

    @Test
    public void wildcardQueryRegularField() {
        assertThat(
            query(
                FROM_ACCOUNTS,
                "WHERE WILDCARD_QUERY(city.keyword, 'B*')"
            ),
            contains(
                wildcardQuery("city.keyword", "B*")
            )
        );
    }

    @Test
    public void wildcardQueryNestedField() {
        assertThat(
            query(
                FROM_NESTED,
                "WHERE WILDCARD_QUERY(nested(comment.data), 'a*')"
            ),
            contains(
                nestedQuery("comment", wildcardQuery("comment.data", "a*"), ScoreMode.None)
            )
        );
    }

    @Test
    public void matchPhraseQueryDefault() {
        assertThat(
            query(
                FROM_PHRASE,
                "WHERE MATCH_PHRASE(phrase, 'brown fox')"
            ),
            contains(
                matchPhraseQuery("phrase", "brown fox")
            )
        );
    }

    @Test
    public void matchPhraseQueryWithSlop() {
        assertThat(
            query(
                FROM_PHRASE,
                "WHERE MATCH_PHRASE(phrase, 'brown fox', slop=2)"
            ),
            contains(
                matchPhraseQuery("phrase", "brown fox").slop(2)
            )
        );
    }

    @Test
    public void multiMatchQuerySingleField() {
        assertThat(
            query(
                FROM_ACCOUNTS,
                "WHERE MULTI_MATCH(query='Ayers', fields='firstname')"
            ),
            contains(
                multiMatchQuery("Ayers").field("firstname")
            )
        );
    }

    @Test
    public void multiMatchQueryWildcardField() {
        assertThat(
            query(
                FROM_ACCOUNTS,
                "WHERE MULTI_MATCH(query='Ay', fields='*name', type='phrase_prefix')"
            ),
            contains(
                multiMatchQuery("Ay").
                                field("*name").
                                type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX)
            )
        );
    }

    private String query(String from, String... statements) {
        return explain(SELECT_ALL + " " + from + " " + String.join(" ", statements));
    }

    private String explain(String sql) {
        try {
            Client mockClient = Mockito.mock(Client.class);
            CheckScriptContents.stubMockClient(mockClient);
            QueryAction queryAction = ESActionFactory.create(mockClient, sql);

            return queryAction.explain().explain();
        } catch (SqlParseException | SQLFeatureNotSupportedException e) {
            throw new ParserException("Illegal sql expr in: " + sql);
        }
    }

    private Matcher<String> contains(AbstractQueryBuilder queryBuilder) {
        return containsString(Strings.toString(queryBuilder, false, false));
    }
}
