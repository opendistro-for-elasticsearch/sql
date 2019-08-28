package com.amazon.opendistroforelasticsearch.sql.unittest.rewriter.inline;

import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.query.AggregationQueryAction;
import com.amazon.opendistroforelasticsearch.sql.rewriter.inline.GroupByInliner;
import com.amazon.opendistroforelasticsearch.sql.rewriter.inline.OrderByInliner;
import org.elasticsearch.client.Client;
import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.unittest.rewriter.inline.OrderByInlinerTest.parseQuery;
import static com.amazon.opendistroforelasticsearch.sql.unittest.rewriter.inline.OrderByInlinerTest.rewrite;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class GroupByInlinerTest {
    @Test
    public void fieldTest() throws SqlParseException {
        String query = "SELECT utc_time date " +
                "FROM kibana_sample_data_logs " +
                "GROUP BY date DESC";

        assertThat(rewrite(query, new GroupByInliner()), equalTo(parseQuery(
                "SELECT utc_time date " +
                        "FROM kibana_sample_data_logs " +
                        "GROUP BY utc_time DESC")));


        System.out.println(new AggregationQueryAction(mock(Client.class), new SqlParser().parseSelect(rewrite(query, new GroupByInliner()))).explain().explain());
    }

    @Test
    public void expressionTest() throws SqlParseException {
        String query = "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                "FROM kibana_sample_data_logs " +
                "GROUP BY date";

        assertThat(rewrite(query, new GroupByInliner()), equalTo(parseQuery(
                "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                        "FROM kibana_sample_data_logs " +
                        "GROUP BY date_format(utc_time, 'dd-MM-YYYY')")));

        System.out.println(new AggregationQueryAction(mock(Client.class), new SqlParser().parseSelect(rewrite(query, new GroupByInliner()))).explain().explain());
    }

    @Test
    public void expressionAndOrderByTest() throws SqlParseException {
        String query = "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                "FROM kibana_sample_data_logs " +
                "GROUP BY date " +
                "ORDER BY date DESC";

        SQLQueryExpr rewrite = rewrite(query, new OrderByInliner());
        new GroupByInliner().rewrite(rewrite);
        System.out.println(new AggregationQueryAction(mock(Client.class), new SqlParser().parseSelect(rewrite)).explain().explain());

        assertThat(rewrite, equalTo(parseQuery(
                "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                        "FROM kibana_sample_data_logs " +
                        "GROUP BY date_format(utc_time, 'dd-MM-YYYY') " +
                        "ORDER BY date_format(utc_time, 'dd-MM-YYYY') DESC")));
    }

    @Test
    public void expressionWithHavingTest() throws SqlParseException {
        String query = "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                "FROM kibana_sample_data_logs " +
                "GROUP BY date DESC " +
                "HAVING date > '15-06-2015'";

        assertThat(rewrite(query, new GroupByInliner()), equalTo(parseQuery(
                "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                        "FROM kibana_sample_data_logs " +
                        "GROUP BY date_format(utc_time, 'dd-MM-YYYY') DESC " +
                        "HAVING date_format(utc_time, 'dd-MM-YYYY') > '15-06-2015'")));

        System.out.println(new AggregationQueryAction(mock(Client.class), new SqlParser().parseSelect(rewrite(query, new GroupByInliner()))).explain().explain());
    }
}
