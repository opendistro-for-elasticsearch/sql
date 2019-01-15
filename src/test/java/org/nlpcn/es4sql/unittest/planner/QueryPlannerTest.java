package org.nlpcn.es4sql.unittest.planner;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.Token;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.nlpcn.es4sql.SqlRequest;
import org.nlpcn.es4sql.domain.JoinSelect;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.parse.ElasticSqlExprParser;
import org.nlpcn.es4sql.parse.SqlParser;
import org.nlpcn.es4sql.query.QueryAction;
import org.nlpcn.es4sql.query.SqlElasticRequestBuilder;
import org.nlpcn.es4sql.query.join.ESJoinQueryActionFactory;
import org.nlpcn.es4sql.query.planner.HashJoinQueryPlanRequestBuilder;
import org.nlpcn.es4sql.query.planner.core.QueryPlanner;

import java.util.Arrays;
import java.util.List;

import static org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory.newConfigurationBuilder;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Test base class for all query planner tests.
 */
public abstract class QueryPlannerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    protected Client client;

    @Spy
    private SearchResponse response1 = new SearchResponse();
    private static final String SCROLL_ID1 = "1";

    @Spy
    private SearchResponse response2 = new SearchResponse();
    private static final String SCROLL_ID2 = "2";

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

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        // Differentiate response for Scroll-1/2 by call count and scroll ID.
        when(client.execute(any(), any()).actionGet()).thenAnswer(new Answer<SearchResponse>() {
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

        when(client.prepareSearchScroll(SCROLL_ID1).setScroll(any(TimeValue.class)).get()).thenReturn(response1);
        when(client.prepareSearchScroll(SCROLL_ID2).setScroll(any(TimeValue.class)).get()).thenReturn(response2);
    }

    protected SearchHits query(String sql, MockSearchHits mockHits1, MockSearchHits mockHits2) {
        doAnswer(mockHits1).when(response1).getHits();
        doAnswer(mockHits2).when(response2).getHits();

        when(client.prepareClearScroll().addScrollId(any()).get()).thenAnswer(new Answer<ClearScrollResponse>() {
            @Override
            public ClearScrollResponse answer(InvocationOnMock invocation) throws Throwable {
                mockHits2.reset();
                return new ClearScrollResponse(true, 0);
            }
        });

        List<SearchHit> hits = plan(sql).execute();
        return new SearchHits(hits.toArray(new SearchHit[0]), hits.size(), 0);
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
            return new SearchHits(curBatch, allHits.length, 0);
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
