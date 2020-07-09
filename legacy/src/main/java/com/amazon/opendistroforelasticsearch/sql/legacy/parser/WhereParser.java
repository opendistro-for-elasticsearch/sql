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

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLBooleanExpr;
import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNotExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumericLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLTextLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Condition;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.KVValue;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.MethodField;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.maker.Maker;
import com.amazon.opendistroforelasticsearch.sql.legacy.spatial.SpatialParamsFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.SQLFunctions;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.Util;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by allwefantasy on 9/2/16.
 */
public class WhereParser {
    private FieldMaker fieldMaker;

    private MySqlSelectQueryBlock query;
    private SQLDeleteStatement delete;
    private SQLExpr where;
    private SqlParser sqlParser;

    public WhereParser(SqlParser sqlParser, MySqlSelectQueryBlock query, FieldMaker fieldMaker) {
        this.sqlParser = sqlParser;
        this.where = query.getWhere();

        this.query = query;
        this.fieldMaker = fieldMaker;
    }

    public WhereParser(SqlParser sqlParser, SQLDeleteStatement delete) {
        this(sqlParser, delete.getWhere());

        this.delete = delete;
    }

    public WhereParser(SqlParser sqlParser, SQLExpr expr) {
        this(sqlParser);
        this.where = expr;
    }

    public WhereParser(SqlParser sqlParser) {
        this.sqlParser = sqlParser;
        this.fieldMaker = new FieldMaker();
    }

    public Where findWhere() throws SqlParseException {
        if (where == null) {
            return null;
        }

        Where myWhere = Where.newInstance();
        parseWhere(where, myWhere);
        return myWhere;
    }

    public void parseWhere(SQLExpr expr, Where where) throws SqlParseException {
        if (expr instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr bExpr = (SQLBinaryOpExpr) expr;
            if (explainSpecialCondWithBothSidesAreLiterals(bExpr, where)) {
                return;
            }
            if (explainSpecialCondWithBothSidesAreProperty(bExpr, where)) {
                return;
            }
        }

        if (expr instanceof SQLBinaryOpExpr && !isCond((SQLBinaryOpExpr) expr)) {
            SQLBinaryOpExpr bExpr = (SQLBinaryOpExpr) expr;
            routeCond(bExpr, bExpr.getLeft(), where);
            routeCond(bExpr, bExpr.getRight(), where);
        } else if (expr instanceof SQLNotExpr) {
            parseWhere(((SQLNotExpr) expr).getExpr(), where);
            negateWhere(where);
        } else {
            explainCond("AND", expr, where);
        }
    }

    private void negateWhere(Where where) throws SqlParseException {
        for (Where sub : where.getWheres()) {
            if (sub instanceof Condition) {
                Condition cond = (Condition) sub;
                cond.setOPERATOR(cond.getOPERATOR().negative());
            } else {
                negateWhere(sub);
            }
            sub.setConn(sub.getConn().negative());
        }
    }

    //some where conditions eg. 1=1 or 3>2 or 'a'='b'
    private boolean explainSpecialCondWithBothSidesAreLiterals(SQLBinaryOpExpr bExpr, Where where)
            throws SqlParseException {
        if ((bExpr.getLeft() instanceof SQLNumericLiteralExpr || bExpr.getLeft() instanceof SQLCharExpr)
                && (bExpr.getRight() instanceof SQLNumericLiteralExpr || bExpr.getRight() instanceof SQLCharExpr)
        ) {
            SQLMethodInvokeExpr sqlMethodInvokeExpr = new SQLMethodInvokeExpr("script", null);
            String operator = bExpr.getOperator().getName();
            if (operator.equals("=")) {
                operator = "==";
            }
            sqlMethodInvokeExpr.addParameter(
                    new SQLCharExpr(Util.expr2Object(bExpr.getLeft(), "'")
                            + " " + operator + " " + Util.expr2Object(bExpr.getRight(), "'"))
            );

            explainCond("AND", sqlMethodInvokeExpr, where);
            return true;
        }
        return false;
    }

    //some where conditions eg. field1=field2 or field1>field2
    private boolean explainSpecialCondWithBothSidesAreProperty(SQLBinaryOpExpr bExpr, Where where)
            throws SqlParseException {
        //join is not support
        if ((bExpr.getLeft() instanceof SQLPropertyExpr || bExpr.getLeft() instanceof SQLIdentifierExpr)
                && (bExpr.getRight() instanceof SQLPropertyExpr || bExpr.getRight() instanceof SQLIdentifierExpr)
                && Sets.newHashSet("=", "<", ">", ">=", "<=").contains(bExpr.getOperator().getName())
                && !Util.isFromJoinOrUnionTable(bExpr)
        ) {
            SQLMethodInvokeExpr sqlMethodInvokeExpr = new SQLMethodInvokeExpr("script", null);
            String operator = bExpr.getOperator().getName();
            if (operator.equals("=")) {
                operator = "==";
            }

            String leftProperty = Util.expr2Object(bExpr.getLeft()).toString();
            String rightProperty = Util.expr2Object(bExpr.getRight()).toString();
            if (leftProperty.split("\\.").length > 1) {

                leftProperty = leftProperty.substring(leftProperty.split("\\.")[0].length() + 1);
            }

            if (rightProperty.split("\\.").length > 1) {
                rightProperty = rightProperty.substring(rightProperty.split("\\.")[0].length() + 1);
            }

            sqlMethodInvokeExpr.addParameter(new SQLCharExpr(
                    "doc['" + leftProperty + "'].value " + operator + " doc['" + rightProperty + "'].value"));

            explainCond("AND", sqlMethodInvokeExpr, where);
            return true;
        }
        return false;
    }


    private boolean isCond(SQLBinaryOpExpr expr) {
        SQLExpr leftSide = expr.getLeft();
        if (leftSide instanceof SQLMethodInvokeExpr) {
            return isAllowedMethodOnConditionLeft((SQLMethodInvokeExpr) leftSide, expr.getOperator());
        }
        return leftSide instanceof SQLIdentifierExpr
                || leftSide instanceof SQLPropertyExpr
                || leftSide instanceof SQLVariantRefExpr
                || leftSide instanceof SQLCastExpr;
    }

    private boolean isAllowedMethodOnConditionLeft(SQLMethodInvokeExpr method, SQLBinaryOperator operator) {
        return (method.getMethodName().toLowerCase().equals("nested")
                || method.getMethodName().toLowerCase().equals("children")
                || SQLFunctions.isFunctionTranslatedToScript(method.getMethodName())
        ) && !operator.isLogical();
    }


    private void routeCond(SQLBinaryOpExpr bExpr, SQLExpr sub, Where where) throws SqlParseException {
        if (sub instanceof SQLBinaryOpExpr && !isCond((SQLBinaryOpExpr) sub)) {
            SQLBinaryOpExpr binarySub = (SQLBinaryOpExpr) sub;
            if (binarySub.getOperator().priority != bExpr.getOperator().priority) {
                Where subWhere = new Where(bExpr.getOperator().name);
                where.addWhere(subWhere);
                parseWhere(binarySub, subWhere);
            } else {
                parseWhere(binarySub, where);
            }
        } else if (sub instanceof SQLNotExpr) {
            Where subWhere = new Where(bExpr.getOperator().name);
            where.addWhere(subWhere);
            parseWhere(((SQLNotExpr) sub).getExpr(), subWhere);
            negateWhere(subWhere);
        } else {
            explainCond(bExpr.getOperator().name, sub, where);
        }
    }

    private void explainCond(String opear, SQLExpr expr, Where where) throws SqlParseException {
        if (expr instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr soExpr = (SQLBinaryOpExpr) expr;

            boolean methodAsOpear = false;

            boolean isNested = false;
            boolean isChildren = false;

            NestedType nestedType = new NestedType();
            if (nestedType.tryFillFromExpr(soExpr.getLeft())) {
                soExpr.setLeft(new SQLIdentifierExpr(nestedType.field));
                isNested = true;
            }

            ChildrenType childrenType = new ChildrenType();
            if (childrenType.tryFillFromExpr(soExpr.getLeft())) {
                soExpr.setLeft(new SQLIdentifierExpr(childrenType.field));
                isChildren = true;
            }

            if (soExpr.getRight() instanceof SQLMethodInvokeExpr) {
                SQLMethodInvokeExpr method = (SQLMethodInvokeExpr) soExpr.getRight();
                String methodName = method.getMethodName().toLowerCase();

                if (Condition.OPERATOR.methodNameToOpear.containsKey(methodName)) {
                    Object[] methodParametersValue = getMethodValuesWithSubQueries(method);

                    final Condition condition;
                    // fix OPEAR
                    Condition.OPERATOR oper = Condition.OPERATOR.methodNameToOpear.get(methodName);
                    if (soExpr.getOperator() == SQLBinaryOperator.LessThanOrGreater
                            || soExpr.getOperator() == SQLBinaryOperator.NotEqual) {
                        oper = oper.negative();
                    }
                    if (isNested) {
                        condition = new Condition(Where.CONN.valueOf(opear), soExpr.getLeft().toString(),
                                soExpr.getLeft(), oper, methodParametersValue, soExpr.getRight(), nestedType);
                    } else if (isChildren) {
                        condition = new Condition(Where.CONN.valueOf(opear), soExpr.getLeft().toString(),
                                soExpr.getLeft(), oper, methodParametersValue, soExpr.getRight(), childrenType);
                    } else {
                        condition = new Condition(Where.CONN.valueOf(opear), soExpr.getLeft().toString(),
                                soExpr.getLeft(), oper, methodParametersValue, soExpr.getRight(), null);
                    }

                    where.addWhere(condition);
                    methodAsOpear = true;
                }
            }

            if (!methodAsOpear) {
                final Condition condition;

                if (isNested) {
                    condition = new Condition(Where.CONN.valueOf(opear), soExpr.getLeft().toString(), soExpr.getLeft(),
                            soExpr.getOperator().name, parseValue(soExpr.getRight()), soExpr.getRight(), nestedType);
                } else if (isChildren) {
                    condition = new Condition(Where.CONN.valueOf(opear), soExpr.getLeft().toString(), soExpr.getLeft(),
                            soExpr.getOperator().name, parseValue(soExpr.getRight()), soExpr.getRight(), childrenType);
                } else {
                    SQLMethodInvokeExpr sqlMethodInvokeExpr = parseSQLBinaryOpExprWhoIsConditionInWhere(soExpr);
                    if (sqlMethodInvokeExpr == null) {
                        condition = new Condition(Where.CONN.valueOf(opear), soExpr.getLeft().toString(),
                                soExpr.getLeft(), soExpr.getOperator().name, parseValue(soExpr.getRight()),
                                soExpr.getRight(), null);
                    } else {
                        ScriptFilter scriptFilter = new ScriptFilter();
                        if (!scriptFilter.tryParseFromMethodExpr(sqlMethodInvokeExpr)) {
                            throw new SqlParseException("could not parse script filter");
                        }
                        condition = new Condition(Where.CONN.valueOf(opear), null, soExpr.getLeft(),
                                "SCRIPT", scriptFilter, soExpr.getRight());

                    }

                }
                where.addWhere(condition);
            }
        } else if (expr instanceof SQLInListExpr) {
            SQLInListExpr siExpr = (SQLInListExpr) expr;
            String leftSide = siExpr.getExpr().toString();

            boolean isNested = false;
            boolean isChildren = false;

            NestedType nestedType = new NestedType();
            if (nestedType.tryFillFromExpr(siExpr.getExpr())) {
                leftSide = nestedType.field;

                isNested = false;
            }

            ChildrenType childrenType = new ChildrenType();
            if (childrenType.tryFillFromExpr(siExpr.getExpr())) {
                leftSide = childrenType.field;

                isChildren = true;
            }

            final Condition condition;

            if (isNested) {
                condition = new Condition(Where.CONN.valueOf(opear), leftSide, null, siExpr.isNot() ? "NOT IN" : "IN",
                        parseValue(siExpr.getTargetList()), null, nestedType);
            } else if (isChildren) {
                condition = new Condition(Where.CONN.valueOf(opear), leftSide, null, siExpr.isNot() ? "NOT IN" : "IN",
                        parseValue(siExpr.getTargetList()), null, childrenType);
            } else {
                condition = new Condition(Where.CONN.valueOf(opear), leftSide, null, siExpr.isNot() ? "NOT IN" : "IN",
                        parseValue(siExpr.getTargetList()), null);
            }

            where.addWhere(condition);
        } else if (expr instanceof SQLBetweenExpr) {
            SQLBetweenExpr between = ((SQLBetweenExpr) expr);
            String leftSide = between.getTestExpr().toString();

            boolean isNested = false;
            boolean isChildren = false;

            NestedType nestedType = new NestedType();
            if (nestedType.tryFillFromExpr(between.getTestExpr())) {
                leftSide = nestedType.field;

                isNested = true;
            }

            ChildrenType childrenType = new ChildrenType();
            if (childrenType.tryFillFromExpr(between.getTestExpr())) {
                leftSide = childrenType.field;

                isChildren = true;
            }

            final Condition condition;

            if (isNested) {
                condition = new Condition(Where.CONN.valueOf(opear), leftSide, null,
                        between.isNot() ? "NOT BETWEEN" : "BETWEEN", new Object[]{parseValue(between.beginExpr),
                        parseValue(between.endExpr)}, null, nestedType);
            } else if (isChildren) {
                condition = new Condition(Where.CONN.valueOf(opear), leftSide, null,
                        between.isNot() ? "NOT BETWEEN" : "BETWEEN", new Object[]{parseValue(between.beginExpr),
                        parseValue(between.endExpr)}, null, childrenType);
            } else {
                condition = new Condition(Where.CONN.valueOf(opear), leftSide, null,
                        between.isNot() ? "NOT BETWEEN" : "BETWEEN", new Object[]{parseValue(between.beginExpr),
                        parseValue(between.endExpr)}, null, null);
            }

            where.addWhere(condition);
        } else if (expr instanceof SQLMethodInvokeExpr) {

            SQLMethodInvokeExpr methodExpr = (SQLMethodInvokeExpr) expr;
            List<SQLExpr> methodParameters = methodExpr.getParameters();

            String methodName = methodExpr.getMethodName();
            if (SpatialParamsFactory.isAllowedMethod(methodName)) {
                String fieldName = methodParameters.get(0).toString();

                boolean isNested = false;
                boolean isChildren = false;

                NestedType nestedType = new NestedType();
                if (nestedType.tryFillFromExpr(methodParameters.get(0))) {
                    fieldName = nestedType.field;

                    isNested = true;
                }

                ChildrenType childrenType = new ChildrenType();
                if (childrenType.tryFillFromExpr(methodParameters.get(0))) {
                    fieldName = childrenType.field;

                    isChildren = true;
                }

                Object spatialParamsObject = SpatialParamsFactory.generateSpatialParamsObject(methodName,
                        methodParameters);

                final Condition condition;

                if (isNested) {
                    condition = new Condition(Where.CONN.valueOf(opear), fieldName, null, methodName,
                            spatialParamsObject, null, nestedType);
                } else if (isChildren) {
                    condition = new Condition(Where.CONN.valueOf(opear), fieldName, null, methodName,
                            spatialParamsObject, null, childrenType);
                } else {
                    condition = new Condition(Where.CONN.valueOf(opear), fieldName, null, methodName,
                            spatialParamsObject, null, null);
                }

                where.addWhere(condition);
            } else if (methodName.toLowerCase().equals("nested")) {
                NestedType nestedType = new NestedType();

                if (!nestedType.tryFillFromExpr(expr)) {
                    throw new SqlParseException("could not fill nested from expr:" + expr);
                }

                Condition condition = new Condition(Where.CONN.valueOf(opear), nestedType.path, null,
                        methodName.toUpperCase(), nestedType.where, null);

                where.addWhere(condition);
            } else if (methodName.toLowerCase().equals("children")) {
                ChildrenType childrenType = new ChildrenType();

                if (!childrenType.tryFillFromExpr(expr)) {
                    throw new SqlParseException("could not fill children from expr:" + expr);
                }

                Condition condition = new Condition(Where.CONN.valueOf(opear), childrenType.childType, null,
                        methodName.toUpperCase(), childrenType.where, null);

                where.addWhere(condition);
            } else if (methodName.toLowerCase().equals("script")) {
                ScriptFilter scriptFilter = new ScriptFilter();
                if (!scriptFilter.tryParseFromMethodExpr(methodExpr)) {
                    throw new SqlParseException("could not parse script filter");
                }
                Condition condition = new Condition(Where.CONN.valueOf(opear), null, null, "SCRIPT",
                        scriptFilter, null);
                where.addWhere(condition);
            } else if (Maker.isQueryFunction(methodName)) {
                Condition condition = getConditionForMethod(expr, Where.CONN.valueOf(opear));

                where.addWhere(condition);
            } else {
                throw new SqlParseException("unsupported method: " + methodName);
            }
        } else if (expr instanceof SQLInSubQueryExpr) {
            SQLInSubQueryExpr sqlIn = (SQLInSubQueryExpr) expr;

            Select innerSelect = sqlParser.parseSelect((MySqlSelectQueryBlock) sqlIn.getSubQuery().getQuery());

            if (innerSelect.getFields() == null || innerSelect.getFields().size() != 1) {
                throw new SqlParseException("should only have one return field in subQuery");
            }

            SubQueryExpression subQueryExpression = new SubQueryExpression(innerSelect);

            String leftSide = sqlIn.getExpr().toString();

            boolean isNested = false;
            boolean isChildren = false;

            NestedType nestedType = new NestedType();
            if (nestedType.tryFillFromExpr(sqlIn.getExpr())) {
                leftSide = nestedType.field;

                isNested = true;
            }

            ChildrenType childrenType = new ChildrenType();
            if (childrenType.tryFillFromExpr(sqlIn.getExpr())) {
                leftSide = childrenType.field;

                isChildren = true;
            }

            final Condition condition;

            if (isNested) {
                condition = new Condition(Where.CONN.valueOf(opear), leftSide, null, sqlIn.isNot() ? "NOT IN" : "IN",
                        subQueryExpression, null, nestedType);
            } else if (isChildren) {
                condition = new Condition(Where.CONN.valueOf(opear), leftSide, null, sqlIn.isNot() ? "NOT IN" : "IN",
                        subQueryExpression, null, childrenType);
            } else {
                condition = new Condition(Where.CONN.valueOf(opear), leftSide, null, sqlIn.isNot() ? "NOT IN" : "IN",
                        subQueryExpression, null, null);
            }

            where.addWhere(condition);
        } else {
            throw new SqlParseException("err find condition " + expr.getClass());
        }
    }

    private MethodField parseSQLMethodInvokeExprWithFunctionInWhere(SQLMethodInvokeExpr soExpr)
            throws SqlParseException {

        MethodField methodField = fieldMaker.makeMethodField(soExpr.getMethodName(),
                soExpr.getParameters(),
                null,
                null,
                query != null ? query.getFrom().getAlias() : null,
                false);
        return methodField;
    }

    private MethodField parseSQLCastExprWithFunctionInWhere(SQLCastExpr soExpr) throws SqlParseException {
        ArrayList<SQLExpr> parameters = new ArrayList<>();
        parameters.add(soExpr.getExpr());
        return fieldMaker.makeMethodField(
                "CAST",
                parameters,
                null,
                null,
                query != null ? query.getFrom().getAlias() : null,
                false
        );
    }

    private SQLMethodInvokeExpr parseSQLBinaryOpExprWhoIsConditionInWhere(SQLBinaryOpExpr soExpr)
            throws SqlParseException {

        if (bothSideAreNotFunction(soExpr) && bothSidesAreNotCast(soExpr)) {
            return null;
        }

        if (soExpr.getLeft() instanceof SQLMethodInvokeExpr) {
            if (!SQLFunctions.isFunctionTranslatedToScript(((SQLMethodInvokeExpr) soExpr.getLeft()).getMethodName())) {
                return null;
            }
        }

        if (soExpr.getRight() instanceof SQLMethodInvokeExpr) {
            if (!SQLFunctions.isFunctionTranslatedToScript(((SQLMethodInvokeExpr) soExpr.getRight()).getMethodName())) {
                return null;
            }
        }


        MethodField leftMethod = new MethodField(null, Lists.newArrayList(
                new KVValue("", Util.expr2Object(soExpr.getLeft(), "'"))), null, null);
        MethodField rightMethod = new MethodField(null, Lists.newArrayList(
                new KVValue("", Util.expr2Object(soExpr.getRight(), "'"))), null, null);

        if (soExpr.getLeft() instanceof SQLIdentifierExpr || soExpr.getLeft() instanceof SQLPropertyExpr) {
            leftMethod = new MethodField(null, Lists.newArrayList(
                    new KVValue("", "doc['" + Util.expr2Object(soExpr.getLeft(), "'") + "'].value")),
                    null, null);
        }

        if (soExpr.getRight() instanceof SQLIdentifierExpr || soExpr.getRight() instanceof SQLPropertyExpr) {
            rightMethod = new MethodField(null, Lists.newArrayList(
                    new KVValue("", "doc['" + Util.expr2Object(soExpr.getRight(), "'") + "'].value")),
                    null, null);
        }

        if (soExpr.getLeft() instanceof SQLMethodInvokeExpr) {
            leftMethod = parseSQLMethodInvokeExprWithFunctionInWhere((SQLMethodInvokeExpr) soExpr.getLeft());
        }
        if (soExpr.getRight() instanceof SQLMethodInvokeExpr) {
            rightMethod = parseSQLMethodInvokeExprWithFunctionInWhere((SQLMethodInvokeExpr) soExpr.getRight());
        }

        if (soExpr.getLeft() instanceof SQLCastExpr) {
            leftMethod = parseSQLCastExprWithFunctionInWhere((SQLCastExpr) soExpr.getLeft());
        }
        if (soExpr.getRight() instanceof SQLCastExpr) {
            rightMethod = parseSQLCastExprWithFunctionInWhere((SQLCastExpr) soExpr.getRight());
        }

        String v1 = leftMethod.getParams().get(0).value.toString();
        String v1Dec = leftMethod.getParams().size() == 2 ? leftMethod.getParams().get(1).value.toString() + ";" : "";


        String v2 = rightMethod.getParams().get(0).value.toString();
        String v2Dec = rightMethod.getParams().size() == 2 ? rightMethod.getParams().get(1).value.toString() + ";" : "";

        String operator = soExpr.getOperator().getName();

        if (operator.equals("=")) {
            operator = "==";
        }

        String finalStr = v1Dec + v2Dec + v1 + " " + operator + " " + v2;

        SQLMethodInvokeExpr scriptMethod = new SQLMethodInvokeExpr("script", null);
        scriptMethod.addParameter(new SQLCharExpr(finalStr));
        return scriptMethod;

    }

    private Boolean bothSideAreNotFunction(SQLBinaryOpExpr soExpr) {
        return !(soExpr.getLeft() instanceof SQLMethodInvokeExpr || soExpr.getRight() instanceof SQLMethodInvokeExpr);
    }

    private Boolean bothSidesAreNotCast(SQLBinaryOpExpr soExpr) {
        return !(soExpr.getLeft() instanceof SQLCastExpr || soExpr.getRight() instanceof SQLCastExpr);
    }

    private Object[] getMethodValuesWithSubQueries(SQLMethodInvokeExpr method) throws SqlParseException {
        List<Object> values = new ArrayList<>();
        for (SQLExpr innerExpr : method.getParameters()) {
            if (innerExpr instanceof SQLQueryExpr) {
                Select select = sqlParser.parseSelect((MySqlSelectQueryBlock) ((SQLQueryExpr) innerExpr).getSubQuery()
                        .getQuery());
                values.add(new SubQueryExpression(select));
            } else if (innerExpr instanceof SQLTextLiteralExpr) {
                values.add(((SQLTextLiteralExpr) innerExpr).getText());
            } else {
                values.add(innerExpr);
            }

        }
        return values.toArray();
    }

    private Object[] parseValue(List<SQLExpr> targetList) throws SqlParseException {
        Object[] value = new Object[targetList.size()];
        for (int i = 0; i < targetList.size(); i++) {
            value[i] = parseValue(targetList.get(i));
        }
        return value;
    }

    private Object parseValue(SQLExpr expr) throws SqlParseException {
        if (expr instanceof SQLNumericLiteralExpr) {
            Number number = ((SQLNumericLiteralExpr) expr).getNumber();
            if (number instanceof BigDecimal) {
                return number.doubleValue();
            }
            if (number instanceof BigInteger) {
                return number.longValue();
            }
            return ((SQLNumericLiteralExpr) expr).getNumber();
        } else if (expr instanceof SQLCharExpr) {
            return ((SQLCharExpr) expr).getText();
        } else if (expr instanceof SQLMethodInvokeExpr) {
            return expr;
        } else if (expr instanceof SQLNullExpr) {
            return null;
        } else if (expr instanceof SQLIdentifierExpr) {
            return expr;
        } else if (expr instanceof SQLPropertyExpr) {
            return expr;
        } else if (expr instanceof SQLBooleanExpr) {
            return ((SQLBooleanExpr) expr).getValue();
        } else {
            throw new SqlParseException(
                    String.format("Failed to parse SqlExpression of type %s. expression value: %s",
                            expr.getClass(), expr)
            );
        }
    }

    public static Condition getConditionForMethod(SQLExpr expr, Where.CONN conn) throws SqlParseException {
        SQLExpr param = ((SQLMethodInvokeExpr) expr).getParameters().get(0);
        String fieldName = param.toString();

        NestedType nestedType = new NestedType();
        ChildrenType childrenType = new ChildrenType();

        if (nestedType.tryFillFromExpr(param)) {
            return new Condition(conn, nestedType.field, null, "=", expr, expr, nestedType);
        } else if (childrenType.tryFillFromExpr(param)) {
            return new Condition(conn, childrenType.field, null, "=", expr, expr, childrenType);
        } else {
            return new Condition(conn, fieldName, null, "=", expr, expr, null);
        }
    }
}
