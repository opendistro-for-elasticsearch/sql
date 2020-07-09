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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.converter;

import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.ColumnTypeProvider;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.AggregationQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.ColumnNode;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.PhysicalOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.node.project.PhysicalProject;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.node.scroll.PhysicalScroll;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Client;

import java.util.List;

/**
 * Definition of SQL to PhysicalOperator converter.
 */
public class SQLToOperatorConverter extends MySqlASTVisitorAdapter {
    private static final Logger LOG = LogManager.getLogger(SQLToOperatorConverter.class);

    private final Client client;
    private final SQLAggregationParser aggregationParser;

    @Getter
    private PhysicalOperator<BindingTuple> physicalOperator;

    public SQLToOperatorConverter(Client client, ColumnTypeProvider columnTypeProvider) {
        this.client = client;
        this.aggregationParser = new SQLAggregationParser(columnTypeProvider);
    }

    @Override
    public boolean visit(MySqlSelectQueryBlock query) {

        //1. parse the aggregation
        aggregationParser.parse(query);


        //2. construct the PhysicalOperator
        physicalOperator = project(scroll(query));
        return false;
    }

    /**
     * Get list of {@link ColumnNode}.
     *
     * @return list of {@link ColumnNode}.
     */
    public List<ColumnNode> getColumnNodes() {
        return aggregationParser.getColumnNodes();
    }

    private PhysicalOperator<BindingTuple> project(PhysicalOperator<BindingTuple> input) {
        return new PhysicalProject(input, aggregationParser.getColumnNodes());
    }

    @SneakyThrows
    private PhysicalOperator<BindingTuple> scroll(MySqlSelectQueryBlock query) {
        query.getSelectList().clear();
        query.getSelectList().addAll(aggregationParser.selectItemList());
        Select select = new SqlParser().parseSelect(query);
        return new PhysicalScroll(new AggregationQueryAction(client, select));
    }
}
