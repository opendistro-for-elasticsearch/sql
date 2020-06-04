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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical;

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Order;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.join.TableInJoinRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.Config;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.Plan;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.QueryParams;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node.Filter;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node.Group;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node.Join;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node.Join.JoinCondition;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node.Project;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node.Sort;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node.TableScan;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node.Top;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.rule.ProjectionPushDown;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.rule.SelectionPushDown;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node.Project.Visitor;

/**
 * Logical query plan.
 */
public class LogicalPlan implements Plan {

    /**
     * Planner configuration
     */
    private final Config config;

    /**
     * Parameters
     */
    private final QueryParams params;

    /**
     * Root node of logical query plan tree
     */
    private final LogicalOperator root;

    /**
     * Transformation rule
     */
    private final List<LogicalPlanVisitor> rules = Arrays.asList(
            new SelectionPushDown(), //Enforce this run first to simplify Group. Avoid this order dependency in future.
            new ProjectionPushDown()
    );

    public LogicalPlan(Config config, QueryParams params) {
        this.config = config;
        this.params = params;
        this.root = buildPlanTree();
    }

    @Override
    public void traverse(Visitor visitor) {
        root.accept(visitor);
    }

    @Override
    public void optimize() {
        for (LogicalPlanVisitor rule : rules) {
            root.accept(rule);
        }
    }

    /**
     * Build logical plan tree
     */
    private LogicalOperator buildPlanTree() {
        return project(
                top(
                        sort(
                                filter(
                                        join(
                                                top(
                                                        group(params.firstRequest(), config.scrollPageSize()[0]),
                                                        config.tableLimit1()
                                                ),
                                                top(
                                                        group(params.secondRequest(), config.scrollPageSize()[1]),
                                                        config.tableLimit2()
                                                )
                                        )
                                )
                        ), config.totalLimit()
                )
        );
    }

    /**
     * Create projection operator
     */
    private LogicalOperator project(LogicalOperator next) {
        Project project = new Project(next);
        for (TableInJoinRequestBuilder req : getRequests()) {
            if (req.getOriginalSelect().isSelectAll()) {
                project.projectAll(req.getAlias());
            } else {
                project.project(req.getAlias(), req.getReturnedFields());
            }
        }
        return project;
    }

    private LogicalOperator top(LogicalOperator next, int limit) {
        if (limit > 0) {
            return new Top(next, limit);
        }
        return next;
    }

    private LogicalOperator sort(LogicalOperator next) {
        List<String> orderByColNames = new ArrayList<>();
        String orderByType = "";
        for (TableInJoinRequestBuilder request : getRequests()) {
            List<Order> orderBys = request.getOriginalSelect().getOrderBys();
            if (orderBys != null) {
                String tableAlias = request.getAlias() == null ? "" : request.getAlias() + ".";
                for (Order orderBy : orderBys) {
                    orderByColNames.add(tableAlias + orderBy.getName());
                    orderByType = orderBy.getType();
                }
            }
        }

        if (orderByColNames.isEmpty()) {
            return next;
        }
        return new Sort(next, orderByColNames, orderByType);
    }

    private LogicalOperator filter(LogicalOperator next) {
        Filter filter = new Filter(next, getRequests());
        if (filter.isNoOp()) {
            return next;
        }
        return filter;
    }

    private LogicalOperator join(LogicalOperator left, LogicalOperator right) {
        return new Join(
                left, right,
                params.joinType(),
                groupJoinConditionByOr(),
                config.blockSize(),
                config.isUseTermsFilterOptimization()
        );
    }

    /**
     * Group conditions in ON by OR because it makes hash table group be required too
     */
    private JoinCondition groupJoinConditionByOr() {
        String leftTableAlias = params.firstRequest().getAlias();
        String rightTableAlias = params.secondRequest().getAlias();

        JoinCondition orCond;
        if (params.joinConditions().isEmpty()) {
            orCond = new JoinCondition(leftTableAlias, rightTableAlias, 0);
        } else {
            orCond = new JoinCondition(leftTableAlias, rightTableAlias, params.joinConditions().size());
            for (int i = 0; i < params.joinConditions().size(); i++) {
                List<Map.Entry<Field, Field>> andCond = params.joinConditions().get(i);
                String[] leftColumnNames = new String[andCond.size()];
                String[] rightColumnNames = new String[andCond.size()];

                for (int j = 0; j < andCond.size(); j++) {
                    Map.Entry<Field, Field> cond = andCond.get(j);
                    leftColumnNames[j] = cond.getKey().getName();
                    rightColumnNames[j] = cond.getValue().getName();
                }

                orCond.addLeftColumnNames(i, leftColumnNames);
                orCond.addRightColumnNames(i, rightColumnNames);
            }
        }
        return orCond;
    }

    private LogicalOperator group(TableInJoinRequestBuilder request, int pageSize) {
        return new Group(new TableScan(request, pageSize));
    }

    private List<TableInJoinRequestBuilder> getRequests() {
        return Arrays.asList(params.firstRequest(), params.secondRequest());
    }

    private <T, U> List<T> map(Collection<U> source, Function<U, T> func) {
        return source.stream().map(func).collect(Collectors.toList());
    }

}
