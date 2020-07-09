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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.planner.converter;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.util.JdbcConstants;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.ColumnTypeProvider;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.converter.SQLToOperatorConverter;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.PhysicalOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.node.project.PhysicalProject;
import org.elasticsearch.client.Client;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SQLToOperatorConverterTest {
    @Mock
    private Client client;

    private SQLToOperatorConverter converter;

    @Before
    public void setup() {
        converter = new SQLToOperatorConverter(client, new ColumnTypeProvider());
    }

    @Test
    public void convertAggShouldPass() {
        String sql = "SELECT dayOfWeek, max(FlightDelayMin), MIN(FlightDelayMin) as min " +
                     "FROM kibana_sample_data_flights " +
                     "GROUP BY dayOfWeek";
        toExpr(sql).accept(converter);
        PhysicalOperator<BindingTuple> physicalOperator = converter.getPhysicalOperator();

        assertTrue(physicalOperator instanceof PhysicalProject);
    }

    @Test
    public void convertMaxMinusMinShouldPass() {
        String sql = "SELECT dayOfWeek, max(FlightDelayMin) - MIN(FlightDelayMin) as diff " +
                     "FROM kibana_sample_data_flights " +
                     "GROUP BY dayOfWeek";
        toExpr(sql).accept(converter);
        PhysicalOperator<BindingTuple> physicalOperator = converter.getPhysicalOperator();

        assertTrue(physicalOperator instanceof PhysicalProject);
    }

    @Test
    public void convertDistinctPass() {
        String sql = "SELECT dayOfWeek, max(FlightDelayMin) - MIN(FlightDelayMin) as diff " +
                     "FROM kibana_sample_data_flights " +
                     "GROUP BY dayOfWeek";
        toExpr(sql).accept(converter);
        PhysicalOperator<BindingTuple> physicalOperator = converter.getPhysicalOperator();

        assertTrue(physicalOperator instanceof PhysicalProject);
    }

    private SQLQueryExpr toExpr(String sql) {
        String dbType = JdbcConstants.MYSQL;
        return (SQLQueryExpr) SQLUtils.toSQLExpr(sql, dbType);
    }
}