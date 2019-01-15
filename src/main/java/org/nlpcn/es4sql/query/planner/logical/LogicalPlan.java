package org.nlpcn.es4sql.query.planner.logical;

import org.nlpcn.es4sql.domain.Field;
import org.nlpcn.es4sql.domain.Order;
import org.nlpcn.es4sql.query.join.TableInJoinRequestBuilder;
import org.nlpcn.es4sql.query.planner.core.Config;
import org.nlpcn.es4sql.query.planner.core.Plan;
import org.nlpcn.es4sql.query.planner.core.QueryParams;
import org.nlpcn.es4sql.query.planner.logical.node.Filter;
import org.nlpcn.es4sql.query.planner.logical.node.Group;
import org.nlpcn.es4sql.query.planner.logical.node.Join;
import org.nlpcn.es4sql.query.planner.logical.node.Join.JoinCondition;
import org.nlpcn.es4sql.query.planner.logical.node.Project;
import org.nlpcn.es4sql.query.planner.logical.node.Sort;
import org.nlpcn.es4sql.query.planner.logical.node.TableScan;
import org.nlpcn.es4sql.query.planner.logical.node.Top;
import org.nlpcn.es4sql.query.planner.logical.rule.ProjectionPushDown;
import org.nlpcn.es4sql.query.planner.logical.rule.SelectionPushDown;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.nlpcn.es4sql.query.planner.logical.node.Project.Visitor;

/**
 * Logical query plan.
 */
public class LogicalPlan implements Plan {

    /** Planner configuration */
    private final Config config;

    /** Parameters */
    private final QueryParams params;

    /** Root node of logical query plan tree */
    private final LogicalOperator root;

    /** Transformation rule */
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

    /** Build logical plan tree */
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

    /** Create projection operator */
    private LogicalOperator project(LogicalOperator next) {
        Project project = new Project(next);
        for (TableInJoinRequestBuilder req : getRequests()) {
            if (req.getOriginalSelect().isSelectAll()) {
                project.projectAll(req.getAlias());
            }
            else {
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

    /** Group conditions in ON by OR because it makes hash table group be required too */
    private JoinCondition groupJoinConditionByOr() {
        String leftTableAlias = params.firstRequest().getAlias();
        String rightTableAlias = params.secondRequest().getAlias();

        JoinCondition orCond;
        if (params.joinConditions().isEmpty()) {
            orCond = new JoinCondition(leftTableAlias, rightTableAlias, 0);
        }
        else {
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
