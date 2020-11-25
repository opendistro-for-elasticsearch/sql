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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.planner;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.Token;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.JoinSelect;
import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.metrics.Metrics;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.ElasticSqlExprParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.plugin.SqlSettings;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.SqlElasticRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.join.ESJoinQueryActionFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.HashJoinQueryPlanRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.QueryPlanner;
import com.amazon.opendistroforelasticsearch.sql.legacy.request.SqlRequest;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.TotalHits.Relation;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.search.ClearScrollRequestBuilder;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.Ignore;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Test base class for all query planner tests.
 */
@Ignore
public abstract class QueryPlannerTest {

    @Mock
    protected Client client;

    @Mock
    private SearchResponse response1;
    private static final String SCROLL_ID1 = "1";

    @Mock
    private SearchResponse response2;
    private static final String SCROLL_ID2 = "2";

    /*
    @BeforeClass
    public static void initLogger() {
        ConfigurationBuilder<BuiltConfiguration> builder = newConfigurationBuilder();
        AppenderComponentBuilder appender = builder.newAppender("stdout", "Console");

        LayoutComponentBuilder standard = builder.newLayout("PatternLayout");
        standard.addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable");
        appender.add(standard);

        RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.ERROR);
        rootLogger.add(builder.newAppenderRef("stdout"));

        LoggerComponentBuilder logger = builder.newLogger("org.nlpcn.es4sql.query.planner", Level.TRACE);
        logger.add(builder.newAppenderRef("stdout"));
        //logger.addAttribute("additivity", false);

        builder.add(logger);

        Configurator.initialize(builder.build());
    }
    */

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        SqlSettings settings = spy(new SqlSettings());
        // Force return empty list to avoid ClusterSettings be invoked which is a final class and hard to mock.
        // In this case, default value in Setting will be returned all the time.
        doReturn(emptyList()).when(settings).getSettings();
        LocalClusterState.state().setSqlSettings(settings);

        ActionFuture mockFuture = mock(ActionFuture.class);
        when(client.execute(any(), any())).thenReturn(mockFuture);

        // Differentiate response for Scroll-1/2 by call count and scroll ID.
        when(mockFuture.actionGet()).thenAnswer(new Answer<SearchResponse>() {
            private int callCnt;

            @Override
            public SearchResponse answer(InvocationOnMock invocation) {
                /*
                 * This works based on assumption that first call comes from Scroll-1, all the following calls come from Scroll-2.
                 * Because Scroll-1 only open scroll once and must be ahead of Scroll-2 which opens multiple times later.
                 */
                return callCnt++ == 0 ? response1 : response2;
            }
        });

        doReturn(SCROLL_ID1).when(response1).getScrollId();
        doReturn(SCROLL_ID2).when(response2).getScrollId();

        // Avoid NPE in empty SearchResponse
        doReturn(0).when(response1).getFailedShards();
        doReturn(0).when(response2).getFailedShards();
        doReturn(false).when(response1).isTimedOut();
        doReturn(false).when(response2).isTimedOut();

        returnMockResponse(SCROLL_ID1, response1);
        returnMockResponse(SCROLL_ID2, response2);

        Metrics.getInstance().registerDefaultMetrics();
    }

    private void returnMockResponse(String scrollId, SearchResponse response) {
        SearchScrollRequestBuilder mockReqBuilder = mock(SearchScrollRequestBuilder.class);
        when(client.prepareSearchScroll(scrollId)).thenReturn(mockReqBuilder);
        when(mockReqBuilder.setScroll(any(TimeValue.class))).thenReturn(mockReqBuilder);
        when(mockReqBuilder.get()).thenReturn(response);
    }

    protected SearchHits query(String sql, MockSearchHits mockHits1, MockSearchHits mockHits2) {
        doAnswer(mockHits1).when(response1).getHits();
        doAnswer(mockHits2).when(response2).getHits();

        ClearScrollRequestBuilder mockReqBuilder = mock(ClearScrollRequestBuilder.class);
        when(client.prepareClearScroll()).thenReturn(mockReqBuilder);
        when(mockReqBuilder.addScrollId(any())).thenReturn(mockReqBuilder);
        when(mockReqBuilder.get()).thenAnswer(new Answer<ClearScrollResponse>() {
            @Override
            public ClearScrollResponse answer(InvocationOnMock invocation) throws Throwable {
                mockHits2.reset();
                return new ClearScrollResponse(true, 0);
            }
        });

        List<SearchHit> hits = plan(sql).execute();
        return new SearchHits(hits.toArray(new SearchHit[0]), new TotalHits(hits.size(), Relation.EQUAL_TO), 0);
    }

    protected QueryPlanner plan(String sql) {
        SqlElasticRequestBuilder request = createRequestBuilder(sql);
        if (request instanceof HashJoinQueryPlanRequestBuilder) {
            return ((HashJoinQueryPlanRequestBuilder) request).plan();
        }
        throw new IllegalStateException("Not a JOIN query: " + sql);
    }

    protected SqlElasticRequestBuilder createRequestBuilder(String sql) {
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

    /**
     * Mock SearchHits and slice and return in batch.
     */
    protected static class MockSearchHits implements Answer<SearchHits> {

        private final SearchHit[] allHits;

        private final int batchSize; //TODO: should be inferred from mock object dynamically

        private int callCnt;

        MockSearchHits(SearchHit[] allHits, int batchSize) {
            this.allHits = allHits;
            this.batchSize = batchSize;
        }

        @Override
        public SearchHits answer(InvocationOnMock invocation) {
            SearchHit[] curBatch;
            if (isNoMoreBatch()) {
                curBatch = new SearchHit[0];
            } else {
                curBatch = currentBatch();
                callCnt++;
            }
            return new SearchHits(curBatch, new TotalHits(allHits.length, Relation.EQUAL_TO), 0);
        }

        private boolean isNoMoreBatch() {
            return callCnt > allHits.length / batchSize;
        }

        private SearchHit[] currentBatch() {
            return Arrays.copyOfRange(allHits, startIndex(), endIndex());
        }

        private int startIndex() {
            return callCnt * batchSize;
        }

        private int endIndex() {
            return Math.min(startIndex() + batchSize, allHits.length);
        }

        private void reset() {
            callCnt = 0;
        }
    }

    protected MockSearchHits employees(SearchHit... mockHits) {
        return employees(5, mockHits);
    }

    protected MockSearchHits employees(int pageSize, SearchHit... mockHits) {
        return new MockSearchHits(mockHits, pageSize);
    }

    protected MockSearchHits departments(SearchHit... mockHits) {
        return departments(5, mockHits);
    }

    protected MockSearchHits departments(int pageSize, SearchHit... mockHits) {
        return new MockSearchHits(mockHits, pageSize);
    }

    protected SearchHit employee(int docId, String lastname, String departmentId) {
        SearchHit hit = new SearchHit(docId);
        if (lastname == null) {
            hit.sourceRef(new BytesArray("{\"departmentId\":\"" + departmentId + "\"}"));
        }
        else if (departmentId == null) {
            hit.sourceRef(new BytesArray("{\"lastname\":\"" + lastname + "\"}"));
        }
        else {
            hit.sourceRef(new BytesArray("{\"lastname\":\"" + lastname + "\",\"departmentId\":\"" + departmentId + "\"}"));
        }
        return hit;
    }

    protected SearchHit department(int docId, String id, String name) {
        SearchHit hit = new SearchHit(docId);
        if (id == null) {
            hit.sourceRef(new BytesArray("{\"name\":\"" + name + "\"}"));
        }
        else if (name == null) {
            hit.sourceRef(new BytesArray("{\"id\":\"" + id + "\"}"));
        }
        else {
            hit.sourceRef(new BytesArray("{\"id\":\"" + id + "\",\"name\":\"" + name + "\"}"));
        }
        return hit;
    }

}
