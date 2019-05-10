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

package com.amazon.opendistroforelasticsearch.sql.intgtest;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.Token;
import com.amazon.opendistroforelasticsearch.sql.domain.JoinSelect;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.parser.ElasticSqlExprParser;
import com.amazon.opendistroforelasticsearch.sql.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.SqlElasticRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.query.join.ESJoinQueryActionFactory;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.elasticsearch.client.Client;
import com.amazon.opendistroforelasticsearch.sql.executor.join.ElasticJoinExecutor;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Assert;
import com.amazon.opendistroforelasticsearch.sql.plugin.SearchDao;
import com.amazon.opendistroforelasticsearch.sql.request.SqlRequest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * Hash join base test to provide query and assert util methods.
 */
public class HashJoinTest {

    /** Hint to use old join algorithm */
    protected static final String USE_OLD_JOIN_ALGORITHM = "/*! USE_NL*/";

    /** Set limit to 100% to bypass circuit break check */
    protected static final String BYPASS_CIRCUIT_BREAK = "/*! JOIN_CIRCUIT_BREAK_LIMIT(100)*/";

    protected SearchHits query(String sql) {
        try {
            SearchDao searchDao = MainTestSuite.getSearchDao();
            SqlElasticRequestBuilder request = createRequestBuilder(searchDao.getClient(), sql);
            ElasticJoinExecutor executor = ElasticJoinExecutor.createJoinExecutor(searchDao.getClient(), request);
            executor.run();
            return executor.getHits();
        }
        catch (Exception e) {
            throw new IllegalStateException("Error occurred during query: " + sql, e);
        }
    }

    /** Assert expected and actual SearchHits */
    protected void assertEquals(SearchHits expected, SearchHits actual) {
        Collection<SearchHitComparator> expectedSet = wrapAndIgnoreUninterestedFields(expected.getHits());
        Collection<SearchHitComparator> actualSet = wrapAndIgnoreUninterestedFields(actual.getHits());
        try {
            Assert.assertEquals(expected.getTotalHits().value, actual.getTotalHits().value);
            Assert.assertEquals(expected.getHits().length, actual.getHits().length);
            Assert.assertEquals(expectedSet, actualSet);
        }
        catch (AssertionError e) {
            String errorMsg;
            if (expectedSet.size() > actualSet.size()) {
                expectedSet.removeAll(actualSet);
                errorMsg = String.format("Missing expected row: %s", expectedSet);
            } else {
                actualSet.removeAll(expectedSet);
                errorMsg = String.format("Unexpected actual row: %s", actualSet);
            }
            throw new AssertionError(errorMsg, e);
        }
    }

    protected String joinSpace(String... strs) {
        return String.join(" ", strs);
    }

    private SqlElasticRequestBuilder createRequestBuilder(Client client, String sql) {
        try {
            SQLQueryExpr sqlExpr = (SQLQueryExpr) toSqlExpr(sql);
            JoinSelect joinSelect = new SqlParser().parseJoinSelect(sqlExpr); // Ignore handleSubquery()
            QueryAction queryAction = ESJoinQueryActionFactory.createJoinAction(client, joinSelect);
            queryAction.setSqlRequest(new SqlRequest(sql, null));
            return queryAction.explain();
        }
        catch (SqlParseException e) {
            throw new IllegalStateException("Invalid query: " + sql, e);
        }
    }

    private SQLExpr toSqlExpr(String sql) {
        SQLExprParser parser = new ElasticSqlExprParser(sql);
        SQLExpr expr = parser.expr();

        if (parser.getLexer().token() != Token.EOF) {
            throw new ParserException("illegal sql expr : " + sql);
        }
        return expr;
    }

    /** Wrap by SearchHitComparator with custom hashcode/equals() and put into Bag for comparison */
    private Collection<SearchHitComparator> wrapAndIgnoreUninterestedFields(SearchHit[] hits) {
        Multiset<SearchHitComparator> bag = HashMultiset.create();
        for (SearchHit hit : hits) {
            bag.add(new SearchHitComparator(hit));
        }
        return bag;
    }

    /**
     * Custom SearchHit equals()/hashcode() to ignore any field we're not interested in
     */
    static class SearchHitComparator {

        private final SearchHit hit;

        SearchHitComparator(SearchHit hit) {
            this.hit = hit;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SearchHitComparator other = (SearchHitComparator) obj;
            return /*Objects.equals(id, other.id)
                &&*/ Objects.equals(hit.getType(), other.hit.getType())
                && Objects.equals(hit.getNestedIdentity(), other.hit.getNestedIdentity())
                && Objects.equals(hit.getVersion(), other.hit.getVersion())
                && Objects.equals(hit.getSourceRef(), other.hit.getSourceRef())
                && Objects.equals(hit.getFields(), other.hit.getFields())
                && Objects.equals(hit.getHighlightFields(), other.hit.getHighlightFields())
                && Arrays.equals(hit.getMatchedQueries(), other.hit.getMatchedQueries())
                && Objects.equals(hit.getExplanation(), other.hit.getExplanation())
                && Objects.equals(hit.getShard(), other.hit.getShard())
                && Objects.equals(hit.getInnerHits(), other.hit.getInnerHits());
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                /*id, */
                hit.getType(),
                hit.getNestedIdentity(),
                hit.getVersion(),
                hit.getSourceRef(),
                hit.getFields(),
                hit.getHighlightFields(),
                Arrays.hashCode(hit.getMatchedQueries()),
                hit.getExplanation(),
                hit.getShard(),
                hit.getInnerHits()
            );
        }

        @Override
        public String toString() {
            return "SearchHitComparator{" + "hit=" + hit.getSourceAsMap() + '}';
        }

    }

}
