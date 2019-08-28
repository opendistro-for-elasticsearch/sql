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

package com.amazon.opendistroforelasticsearch.sql.unittest.rewriter.inline;

import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.parser.ElasticSqlExprParser;
import com.amazon.opendistroforelasticsearch.sql.rewriter.inline.OrderByInliner;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class OrderByInlinerTest {

    @Test
    public void fieldTest() {
        String query = "SELECT utc_time date " +
                "FROM kibana_sample_data_logs " +
                "ORDER BY date DESC";

        assertThat(rewrite(query), equalTo(parseQuery(
                "SELECT utc_time date " +
                "FROM kibana_sample_data_logs " +
                "ORDER BY utc_time DESC")));
    }

    @Test
    public void expressionTest() {
        String query = "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                "FROM kibana_sample_data_logs " +
                "ORDER BY date DESC";

        assertThat(rewrite(query), equalTo(parseQuery(
                "SELECT date_format(utc_time, 'dd-MM-YYYY') date " +
                        "FROM kibana_sample_data_logs " +
                        "ORDER BY date_format(utc_time, 'dd-MM-YYYY') DESC")));
    }

    private SQLQueryExpr rewrite(String query) {
        SQLQueryExpr parsed = parseQuery(query);
        new OrderByInliner().rewrite(parsed);
        return parsed;
    }

    private SQLQueryExpr parseQuery(String query) {
        ElasticSqlExprParser parser = new ElasticSqlExprParser(query);
        return (SQLQueryExpr)parser.expr();
    }
}
