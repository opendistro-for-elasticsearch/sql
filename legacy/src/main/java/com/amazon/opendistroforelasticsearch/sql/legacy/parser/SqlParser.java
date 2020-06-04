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

package com.amazon.opendistroforelasticsearch.sql.legacy.parser;

import com.alibaba.druid.sql.ast.SQLCommentHint;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLOrderingSpecification;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLListExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlSelectGroupByExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Condition;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Delete;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.From;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Having;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.JoinSelect;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.MethodField;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.TableOnJoinSelect;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.hints.Hint;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.hints.HintFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.multi.MultiQuerySelect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.amazon.opendistroforelasticsearch.sql.legacy.utils.Util.NESTED_JOIN_TYPE;


/**
 * es sql support
 *
 * @author ansj
 */
public class SqlParser {
    private FieldMaker fieldMaker = new FieldMaker();

    public SqlParser() {

    }

    public Select parseSelect(SQLQueryExpr mySqlExpr) throws SqlParseException {
        MySqlSelectQueryBlock query = (MySqlSelectQueryBlock) mySqlExpr.getSubQuery().getQuery();
        SubQueryParser subQueryParser = new SubQueryParser(this);
        if (subQueryParser.containSubqueryInFrom(query)) {
            return subQueryParser.parseSubQueryInFrom(query);
        } else {
            return parseSelect(query);
        }
    }

    public Select parseSelect(MySqlSelectQueryBlock query) throws SqlParseException {

        Select select = new Select();
        WhereParser whereParser = new WhereParser(this, query, fieldMaker);

        if (query.getAttribute(NESTED_JOIN_TYPE) != null) {
            select.setNestedJoinType((SQLJoinTableSource.JoinType) query.getAttribute(NESTED_JOIN_TYPE));
        }

        findSelect(query, select, query.getFrom().getAlias());

        select.getFrom().addAll(findFrom(query.getFrom()));

        select.setWhere(whereParser.findWhere());

        select.fillSubQueries();

        select.getHints().addAll(parseHints(query.getHints()));

        findLimit(query.getLimit(), select);

        if (query.getOrderBy() != null) {
            addOrderByToSelect(select, query, query.getOrderBy().getItems(), null);
        }

        if (query.getGroupBy() != null) {
            findGroupBy(query, select);
        }

        return select;
    }

    public Delete parseDelete(SQLDeleteStatement deleteStatement) throws SqlParseException {
        Delete delete = new Delete();
        WhereParser whereParser = new WhereParser(this, deleteStatement);

        delete.getFrom().addAll(findFrom(deleteStatement.getTableSource()));

        delete.setWhere(whereParser.findWhere());

        return delete;
    }

    public MultiQuerySelect parseMultiSelect(SQLUnionQuery query) throws SqlParseException {
        Select firstTableSelect = this.parseSelect((MySqlSelectQueryBlock) query.getLeft());
        Select secondTableSelect = this.parseSelect((MySqlSelectQueryBlock) query.getRight());
        return new MultiQuerySelect(query.getOperator(), firstTableSelect, secondTableSelect);
    }

    private void findSelect(MySqlSelectQueryBlock query, Select select, String tableAlias) throws SqlParseException {
        List<SQLSelectItem> selectList = query.getSelectList();
        for (SQLSelectItem sqlSelectItem : selectList) {
            Field field = fieldMaker.makeField(sqlSelectItem.getExpr(), sqlSelectItem.getAlias(), tableAlias);
            select.addField(field);
        }
    }

    private void findGroupBy(MySqlSelectQueryBlock query, Select select) throws SqlParseException {
        Map<String, SQLExpr> aliasesToExperssions = query
                .getSelectList()
                .stream()
                .filter(item -> item.getAlias() != null)
                .collect(Collectors.toMap(SQLSelectItem::getAlias, SQLSelectItem::getExpr));

        SQLSelectGroupByClause groupBy = query.getGroupBy();
        SQLTableSource sqlTableSource = query.getFrom();

        findHaving(query, select);

        List<SQLExpr> items = groupBy.getItems();

        List<SQLExpr> standardGroupBys = new ArrayList<>();
        for (SQLExpr sqlExpr : items) {
            //todo: mysql expr patch
            if (sqlExpr instanceof MySqlSelectGroupByExpr) {
                MySqlSelectGroupByExpr sqlSelectGroupByExpr = (MySqlSelectGroupByExpr) sqlExpr;
                sqlExpr = sqlSelectGroupByExpr.getExpr();
            }

            if ((sqlExpr instanceof SQLParensIdentifierExpr || !(sqlExpr instanceof SQLIdentifierExpr
                    || sqlExpr instanceof SQLMethodInvokeExpr)) && !standardGroupBys.isEmpty()) {
                // flush the standard group bys
                select.addGroupBy(convertExprsToFields(standardGroupBys, sqlTableSource));
                standardGroupBys = new ArrayList<>();
            }

            if (sqlExpr instanceof SQLParensIdentifierExpr) {
                // single item with parens (should get its own aggregation)
                select.addGroupBy(fieldMaker.makeField(sqlExpr, null, sqlTableSource.getAlias()));
            } else if (sqlExpr instanceof SQLListExpr) {
                // multiple items in their own list
                SQLListExpr listExpr = (SQLListExpr) sqlExpr;
                select.addGroupBy(convertExprsToFields(listExpr.getItems(), sqlTableSource));
            } else {
                // check if field is actually alias
                if (aliasesToExperssions.containsKey(sqlExpr.toString())) {
                    sqlExpr = aliasesToExperssions.get(sqlExpr.toString());
                }
                standardGroupBys.add(sqlExpr);
            }
        }
        if (!standardGroupBys.isEmpty()) {
            select.addGroupBy(convertExprsToFields(standardGroupBys, sqlTableSource));
        }
    }

    private void findHaving(MySqlSelectQueryBlock query, Select select) throws SqlParseException {
        select.setHaving(new Having(query.getGroupBy(), new WhereParser(this, query, fieldMaker)));
    }

    private List<Field> convertExprsToFields(List<? extends SQLExpr> exprs, SQLTableSource sqlTableSource)
            throws SqlParseException {
        List<Field> fields = new ArrayList<>(exprs.size());
        for (SQLExpr expr : exprs) {
            //here we suppose groupby field will not have alias,so set null in second parameter
            fields.add(fieldMaker.makeField(expr, null, sqlTableSource.getAlias()));
        }
        return fields;
    }

    private String sameAliasWhere(Where where, String... aliases) throws SqlParseException {
        if (where == null) {
            return null;
        }

        if (where instanceof Condition) {
            Condition condition = (Condition) where;
            String fieldName = condition.getName();
            for (String alias : aliases) {
                String prefix = alias + ".";
                if (fieldName.startsWith(prefix)) {
                    return alias;
                }
            }
            throw new SqlParseException(String.format("Field [%s] with condition [%s] does not contain an alias",
                    fieldName, condition.toString()));
        }
        List<String> sameAliases = new ArrayList<>();
        if (where.getWheres() != null && where.getWheres().size() > 0) {
            for (Where innerWhere : where.getWheres()) {
                sameAliases.add(sameAliasWhere(innerWhere, aliases));
            }
        }

        if (sameAliases.contains(null)) {
            return null;
        }
        String firstAlias = sameAliases.get(0);
        //return null if more than one alias
        for (String alias : sameAliases) {
            if (!alias.equals(firstAlias)) {
                return null;
            }
        }
        return firstAlias;
    }

    private void addOrderByToSelect(Select select, MySqlSelectQueryBlock queryBlock, List<SQLSelectOrderByItem> items,
                                    String alias)
            throws SqlParseException {

        Map<String, SQLExpr> aliasesToExpressions = queryBlock
                .getSelectList()
                .stream()
                .filter(item -> item.getAlias() != null)
                .collect(Collectors.toMap(SQLSelectItem::getAlias, SQLSelectItem::getExpr));

        for (SQLSelectOrderByItem sqlSelectOrderByItem : items) {
            if (sqlSelectOrderByItem.getType() == null) {
                sqlSelectOrderByItem.setType(SQLOrderingSpecification.ASC);
            }
            String type = sqlSelectOrderByItem.getType().toString();
            SQLExpr expr = extractExprFromOrderExpr(sqlSelectOrderByItem);

            if (expr instanceof SQLIdentifierExpr) {
                if (queryBlock.getGroupBy() == null || queryBlock.getGroupBy().getItems().isEmpty()) {
                    if (aliasesToExpressions.containsKey(((SQLIdentifierExpr) expr).getName())) {
                        expr = aliasesToExpressions.get(((SQLIdentifierExpr) expr).getName());
                    }
                }
            }

            Field field = fieldMaker.makeField(expr, null, null);

            SQLExpr sqlExpr = sqlSelectOrderByItem.getExpr();
            if (sqlExpr instanceof SQLBinaryOpExpr && hasNullOrderInBinaryOrderExpr(sqlExpr)) {
                // override Field.expression to SQLBinaryOpExpr,
                // which was set by FieldMaker.makeField() to SQLIdentifierExpr above
                field.setExpression(sqlExpr);
            }

            String orderByName;
            if (field.isScriptField()) {
                MethodField methodField = (MethodField) field;

                // 0 - generated field name
                final int SCRIPT_CONTENT_INDEX = 1;
                orderByName = methodField.getParams().get(SCRIPT_CONTENT_INDEX).toString();

            } else {
                orderByName = field.toString();
            }

            orderByName = orderByName.replace("`", "");
            if (alias != null) {
                orderByName = orderByName.replaceFirst(alias + "\\.", "");
            }
            select.addOrderBy(field.getNestedPath(), orderByName, type, field);
        }
    }

    private SQLExpr extractExprFromOrderExpr(SQLSelectOrderByItem sqlSelectOrderByItem) {
        SQLExpr expr = sqlSelectOrderByItem.getExpr();

        // extract SQLIdentifier from Order IS NULL/NOT NULL expression to generate Field
        // else passing SQLBinaryOpExpr to FieldMaker.makeFieldImpl tries to convert to SQLMethodInvokeExpr
        // and throws SQLParserException
        if (hasNullOrderInBinaryOrderExpr(expr)) {
            return ((SQLBinaryOpExpr) expr).getLeft();
        }
        return expr;
    }

    private boolean hasNullOrderInBinaryOrderExpr(SQLExpr expr) {
        /**
         * Valid AST that meets ORDER BY IS NULL/NOT NULL condition (true)
         *
         *            SQLSelectOrderByItem
         *                      |
         *             SQLBinaryOpExpr (Is || IsNot)
         *                    /  \
         *    SQLIdentifierExpr  SQLNullExpr
         */
        if (!(expr instanceof SQLBinaryOpExpr)) {
            return false;
        }

        // check "shape of expression": <identifier> <operator> <NULL>
        SQLBinaryOpExpr binaryExpr = (SQLBinaryOpExpr) expr;
        if (!(binaryExpr.getLeft() instanceof SQLIdentifierExpr)|| !(binaryExpr.getRight() instanceof SQLNullExpr)) {
            return false;
        }

        // check that operator IS or IS NOT
        SQLBinaryOperator operator = binaryExpr.getOperator();
        return operator == SQLBinaryOperator.Is || operator == SQLBinaryOperator.IsNot;

    }

    private void findLimit(MySqlSelectQueryBlock.Limit limit, Select select) {

        if (limit == null) {
            return;
        }

        select.setRowCount(Integer.parseInt(limit.getRowCount().toString()));

        if (limit.getOffset() != null) {
            select.setOffset(Integer.parseInt(limit.getOffset().toString()));
        }
    }

    /**
     * Parse the from clause
     *
     * @param from the from clause.
     * @return list of From objects represents all the sources.
     */
    private List<From> findFrom(SQLTableSource from) {
        boolean isSqlExprTable = from.getClass().isAssignableFrom(SQLExprTableSource.class);

        if (isSqlExprTable) {
            SQLExprTableSource fromExpr = (SQLExprTableSource) from;
            String[] split = fromExpr.getExpr().toString().split(",");

            ArrayList<From> fromList = new ArrayList<>();
            for (String source : split) {
                fromList.add(new From(source.trim(), fromExpr.getAlias()));
            }
            return fromList;
        }

        SQLJoinTableSource joinTableSource = ((SQLJoinTableSource) from);
        List<From> fromList = new ArrayList<>();
        fromList.addAll(findFrom(joinTableSource.getLeft()));
        fromList.addAll(findFrom(joinTableSource.getRight()));
        return fromList;
    }

    public JoinSelect parseJoinSelect(SQLQueryExpr sqlExpr) throws SqlParseException {

        MySqlSelectQueryBlock query = (MySqlSelectQueryBlock) sqlExpr.getSubQuery().getQuery();

        List<From> joinedFrom = findJoinedFrom(query.getFrom());
        if (joinedFrom.size() != 2) {
            throw new RuntimeException("currently supports only 2 tables join");
        }

        JoinSelect joinSelect = createBasicJoinSelectAccordingToTableSource((SQLJoinTableSource) query.getFrom());
        List<Hint> hints = parseHints(query.getHints());
        joinSelect.setHints(hints);
        String firstTableAlias = joinedFrom.get(0).getAlias();
        String secondTableAlias = joinedFrom.get(1).getAlias();
        Map<String, Where> aliasToWhere = splitAndFindWhere(query.getWhere(), firstTableAlias, secondTableAlias);
        Map<String, List<SQLSelectOrderByItem>> aliasToOrderBy = splitAndFindOrder(query.getOrderBy(), firstTableAlias,
                secondTableAlias);
        List<Condition> connectedConditions = getConditionsFlatten(joinSelect.getConnectedWhere());
        joinSelect.setConnectedConditions(connectedConditions);
        fillTableSelectedJoin(joinSelect.getFirstTable(), query, joinedFrom.get(0),
                aliasToWhere.get(firstTableAlias), aliasToOrderBy.get(firstTableAlias), connectedConditions);
        fillTableSelectedJoin(joinSelect.getSecondTable(), query, joinedFrom.get(1),
                aliasToWhere.get(secondTableAlias), aliasToOrderBy.get(secondTableAlias), connectedConditions);

        updateJoinLimit(query.getLimit(), joinSelect);

        //todo: throw error feature not supported:  no group bys on joins ?
        return joinSelect;
    }

    private Map<String, List<SQLSelectOrderByItem>> splitAndFindOrder(SQLOrderBy orderBy, String firstTableAlias,
                                                                      String secondTableAlias)
            throws SqlParseException {
        Map<String, List<SQLSelectOrderByItem>> aliasToOrderBys = new HashMap<>();
        aliasToOrderBys.put(firstTableAlias, new ArrayList<>());
        aliasToOrderBys.put(secondTableAlias, new ArrayList<>());
        if (orderBy == null) {
            return aliasToOrderBys;
        }
        List<SQLSelectOrderByItem> orderByItems = orderBy.getItems();
        for (SQLSelectOrderByItem orderByItem : orderByItems) {
            if (orderByItem.getExpr().toString().startsWith(firstTableAlias + ".")) {
                aliasToOrderBys.get(firstTableAlias).add(orderByItem);
            } else if (orderByItem.getExpr().toString().startsWith(secondTableAlias + ".")) {
                aliasToOrderBys.get(secondTableAlias).add(orderByItem);
            } else {
                throw new SqlParseException("order by field on join request should have alias before, got "
                        + orderByItem.getExpr().toString());
            }

        }
        return aliasToOrderBys;
    }

    private void updateJoinLimit(MySqlSelectQueryBlock.Limit limit, JoinSelect joinSelect) {
        if (limit != null && limit.getRowCount() != null) {
            int sizeLimit = Integer.parseInt(limit.getRowCount().toString());
            joinSelect.setTotalLimit(sizeLimit);
        }
    }

    private List<Hint> parseHints(List<SQLCommentHint> sqlHints) throws SqlParseException {
        List<Hint> hints = new ArrayList<>();
        for (SQLCommentHint sqlHint : sqlHints) {
            Hint hint = HintFactory.getHintFromString(sqlHint.getText());
            if (hint != null) {
                hints.add(hint);
            }
        }
        return hints;
    }

    private JoinSelect createBasicJoinSelectAccordingToTableSource(SQLJoinTableSource joinTableSource)
            throws SqlParseException {
        JoinSelect joinSelect = new JoinSelect();
        if (joinTableSource.getCondition() != null) {
            Where where = Where.newInstance();
            WhereParser whereParser = new WhereParser(this, joinTableSource.getCondition());
            whereParser.parseWhere(joinTableSource.getCondition(), where);
            joinSelect.setConnectedWhere(where);
        }
        SQLJoinTableSource.JoinType joinType = joinTableSource.getJoinType();
        joinSelect.setJoinType(joinType);
        return joinSelect;
    }

    private Map<String, Where> splitAndFindWhere(SQLExpr whereExpr, String firstTableAlias, String secondTableAlias)
            throws SqlParseException {
        WhereParser whereParser = new WhereParser(this, whereExpr);
        Where where = whereParser.findWhere();
        return splitWheres(where, firstTableAlias, secondTableAlias);
    }

    private void fillTableSelectedJoin(TableOnJoinSelect tableOnJoin, MySqlSelectQueryBlock query, From tableFrom,
                                       Where where, List<SQLSelectOrderByItem> orderBys, List<Condition> conditions)
            throws SqlParseException {
        String alias = tableFrom.getAlias();
        fillBasicTableSelectJoin(tableOnJoin, tableFrom, where, orderBys, query);
        tableOnJoin.setConnectedFields(getConnectedFields(conditions, alias));
        tableOnJoin.setSelectedFields(new ArrayList<>(tableOnJoin.getFields()));
        tableOnJoin.setAlias(alias);
        tableOnJoin.fillSubQueries();
    }

    private List<Field> getConnectedFields(List<Condition> conditions, String alias) throws SqlParseException {
        List<Field> fields = new ArrayList<>();
        String prefix = alias + ".";
        for (Condition condition : conditions) {
            if (condition.getName().startsWith(prefix)) {
                fields.add(new Field(condition.getName().replaceFirst(prefix, ""), null));
            } else {
                if (!((condition.getValue() instanceof SQLPropertyExpr)
                        || (condition.getValue() instanceof SQLIdentifierExpr)
                        || (condition.getValue() instanceof String))) {
                    throw new SqlParseException("Illegal condition content: " + condition.toString());
                }
                String aliasDotValue = condition.getValue().toString();
                int indexOfDot = aliasDotValue.indexOf(".");
                String owner = aliasDotValue.substring(0, indexOfDot);
                if (owner.equals(alias)) {
                    fields.add(new Field(aliasDotValue.substring(indexOfDot + 1), null));
                }
            }
        }
        return fields;
    }

    private void fillBasicTableSelectJoin(TableOnJoinSelect select, From from, Where where,
                                          List<SQLSelectOrderByItem> orderBys, MySqlSelectQueryBlock query)
            throws SqlParseException {
        select.getFrom().add(from);
        findSelect(query, select, from.getAlias());
        select.setWhere(where);
        addOrderByToSelect(select, query, orderBys, from.getAlias());
    }

    private List<Condition> getJoinConditionsFlatten(SQLJoinTableSource from) throws SqlParseException {
        List<Condition> conditions = new ArrayList<>();
        if (from.getCondition() == null) {
            return conditions;
        }
        Where where = Where.newInstance();
        WhereParser whereParser = new WhereParser(this, from.getCondition());
        whereParser.parseWhere(from.getCondition(), where);
        addIfConditionRecursive(where, conditions);
        return conditions;
    }

    private List<Condition> getConditionsFlatten(Where where) throws SqlParseException {
        List<Condition> conditions = new ArrayList<>();
        if (where == null) {
            return conditions;
        }
        addIfConditionRecursive(where, conditions);
        return conditions;
    }


    private Map<String, Where> splitWheres(Where where, String... aliases) throws SqlParseException {
        Map<String, Where> aliasToWhere = new HashMap<>();
        for (String alias : aliases) {
            aliasToWhere.put(alias, null);
        }
        if (where == null) {
            return aliasToWhere;
        }

        String allWhereFromSameAlias = sameAliasWhere(where, aliases);
        if (allWhereFromSameAlias != null) {
            removeAliasPrefix(where, allWhereFromSameAlias);
            aliasToWhere.put(allWhereFromSameAlias, where);
            return aliasToWhere;
        }
        for (Where innerWhere : where.getWheres()) {
            String sameAlias = sameAliasWhere(innerWhere, aliases);
            if (sameAlias == null) {
                throw new SqlParseException("Currently support only one hierarchy on different tables where");
            }
            removeAliasPrefix(innerWhere, sameAlias);
            Where aliasCurrentWhere = aliasToWhere.get(sameAlias);
            if (aliasCurrentWhere == null) {
                aliasToWhere.put(sameAlias, innerWhere);
            } else {
                Where andWhereContainer = Where.newInstance();
                andWhereContainer.addWhere(aliasCurrentWhere);
                andWhereContainer.addWhere(innerWhere);
                aliasToWhere.put(sameAlias, andWhereContainer);
            }
        }

        return aliasToWhere;
    }

    private void removeAliasPrefix(Where where, String alias) {

        if (where instanceof Condition) {
            Condition cond = (Condition) where;
            String aliasPrefix = alias + ".";
            cond.setName(cond.getName().replaceFirst(aliasPrefix, ""));
            return;
        }
        for (Where innerWhere : where.getWheres()) {
            removeAliasPrefix(innerWhere, alias);
        }
    }

    private void addIfConditionRecursive(Where where, List<Condition> conditions) throws SqlParseException {
        if (where instanceof Condition) {
            Condition cond = (Condition) where;
            if (!((cond.getValue() instanceof SQLIdentifierExpr) || (cond.getValue() instanceof SQLPropertyExpr)
                    || (cond.getValue() instanceof String))) {
                throw new SqlParseException("conditions on join should be one side is secondTable OPEAR firstTable, "
                        + "condition was:" + cond.toString());
            }
            conditions.add(cond);
        }
        for (Where innerWhere : where.getWheres()) {
            addIfConditionRecursive(innerWhere, conditions);
        }
    }

    private List<From> findJoinedFrom(SQLTableSource from) {
        SQLJoinTableSource joinTableSource = ((SQLJoinTableSource) from);
        List<From> fromList = new ArrayList<>();
        fromList.addAll(findFrom(joinTableSource.getLeft()));
        fromList.addAll(findFrom(joinTableSource.getRight()));
        return fromList;
    }


}
