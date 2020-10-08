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
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAggregateOption;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumericLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.KVValue;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.MethodField;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.ScriptMethodField;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlFeatureNotImplementedException;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.SQLFunctions;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.Util;
import com.google.common.base.Strings;
import org.elasticsearch.common.collect.Tuple;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 一些具有参数的一般在 select 函数.或者group by 函数
 *
 * @author ansj
 */
public class FieldMaker {
    private SQLFunctions sqlFunctions = new SQLFunctions();

    public Field makeField(SQLExpr expr, String alias, String tableAlias) throws SqlParseException {
        Field field = makeFieldImpl(expr, alias, tableAlias);
        addGroupByForDistinctFieldsInSelect(expr, field);

        // why we may get null as a field???
        if (field != null) {
            field.setExpression(expr);
        }

        return field;
    }

    private Field makeFieldImpl(SQLExpr expr, String alias, String tableAlias) throws SqlParseException {
        if (expr instanceof SQLIdentifierExpr || expr instanceof SQLPropertyExpr || expr instanceof SQLVariantRefExpr) {
            return handleIdentifier(expr, alias, tableAlias);
        } else if (expr instanceof SQLQueryExpr) {
            throw new SqlParseException("unknown field name : " + expr);
        } else if (expr instanceof SQLBinaryOpExpr) {
            //make a SCRIPT method field;
            return makeFieldImpl(makeBinaryMethodField((SQLBinaryOpExpr) expr, alias, true), alias, tableAlias);
        } else if (expr instanceof SQLAllColumnExpr) {
            return Field.STAR;
        } else if (expr instanceof SQLMethodInvokeExpr) {
            SQLMethodInvokeExpr mExpr = (SQLMethodInvokeExpr) expr;

            String methodName = mExpr.getMethodName();

            if (methodName.equalsIgnoreCase("nested") || methodName.equalsIgnoreCase("reverse_nested")) {
                NestedType nestedType = new NestedType();
                if (nestedType.tryFillFromExpr(mExpr)) {
                    return handleIdentifier(nestedType, alias, tableAlias);
                }
            } else if (methodName.equalsIgnoreCase("children")) {
                ChildrenType childrenType = new ChildrenType();
                if (childrenType.tryFillFromExpr(mExpr)) {
                    return handleIdentifier(childrenType, alias, tableAlias);
                }
            } else if (methodName.equalsIgnoreCase("filter")) {
                return makeFilterMethodField(mExpr, alias);
            }

            if ((SQLFunctions.builtInFunctions.contains(methodName.toLowerCase())) && Strings.isNullOrEmpty(alias)) {
                alias = mExpr.toString();
            }
            return makeMethodField(methodName, mExpr.getParameters(), null, alias, tableAlias, true);
        } else if (expr instanceof SQLAggregateExpr) {
            SQLAggregateExpr sExpr = (SQLAggregateExpr) expr;
            return makeMethodField(sExpr.getMethodName(), sExpr.getArguments(), sExpr.getOption(),
                    alias, tableAlias, true);
        } else if (expr instanceof SQLCaseExpr) {
            String scriptCode = new CaseWhenParser((SQLCaseExpr) expr, alias, tableAlias).parse();
            List<KVValue> methodParameters = new ArrayList<>();
            methodParameters.add(new KVValue(alias));
            methodParameters.add(new KVValue(scriptCode));
            return new MethodField("script", methodParameters, null, alias);
        } else if (expr instanceof SQLCastExpr) {
            SQLCastExpr castExpr = (SQLCastExpr) expr;
            if (alias == null) {
                alias = "cast_" + castExpr.getExpr().toString();
            }
            ArrayList<SQLExpr> methodParameters = new ArrayList<>();
            methodParameters.add(((SQLCastExpr) expr).getExpr());
            return makeMethodField("CAST", methodParameters, null, alias, tableAlias, true);
        } else if (expr instanceof SQLNumericLiteralExpr) {
            SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr("assign", null);
            methodInvokeExpr.addParameter(expr);
            return makeMethodField(methodInvokeExpr.getMethodName(), methodInvokeExpr.getParameters(),
                    null, alias, tableAlias, true);
        } else {
            throw new SqlParseException("unknown field name : " + expr);
        }
    }

    private void addGroupByForDistinctFieldsInSelect(SQLExpr expr, Field field) {
        if (expr.getParent() != null && expr.getParent() instanceof SQLSelectItem
                && expr.getParent().getParent() != null
                && expr.getParent().getParent() instanceof SQLSelectQueryBlock) {
            SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) expr.getParent().getParent();
            if (queryBlock.getDistionOption() == SQLSetQuantifier.DISTINCT) {
                SQLAggregateOption option = SQLAggregateOption.DISTINCT;
                field.setAggregationOption(option);
                if (queryBlock.getGroupBy() == null) {
                    queryBlock.setGroupBy(new SQLSelectGroupByClause());
                }
                SQLSelectGroupByClause groupByClause = queryBlock.getGroupBy();
                groupByClause.addItem(expr);
                queryBlock.setGroupBy(groupByClause);
            }
        }
    }

    private static Object getScriptValue(SQLExpr expr) throws SqlParseException {
        return Util.getScriptValue(expr);
    }

    private Field makeScriptMethodField(SQLBinaryOpExpr binaryExpr, String alias, String tableAlias)
            throws SqlParseException {
        List<SQLExpr> params = new ArrayList<>();

        String scriptFieldAlias;
        if (alias == null || alias.equals("")) {
            scriptFieldAlias = binaryExpr.toString();
        } else {
            scriptFieldAlias = alias;
        }
        params.add(new SQLCharExpr(scriptFieldAlias));

        Object left = getScriptValue(binaryExpr.getLeft());
        Object right = getScriptValue(binaryExpr.getRight());
        String script = String.format("%s %s %s", left, binaryExpr.getOperator().getName(), right);

        params.add(new SQLCharExpr(script));

        return makeMethodField("script", params, null, null, tableAlias, false);
    }


    private static Field makeFilterMethodField(SQLMethodInvokeExpr filterMethod, String alias)
            throws SqlParseException {
        List<SQLExpr> parameters = filterMethod.getParameters();
        int parametersSize = parameters.size();
        if (parametersSize != 1 && parametersSize != 2) {
            throw new SqlParseException("filter group by field should only have one or 2 parameters"
                    + " filter(Expr) or filter(name,Expr)");
        }
        String filterAlias = filterMethod.getMethodName();
        SQLExpr exprToCheck = null;
        if (parametersSize == 1) {
            exprToCheck = parameters.get(0);
            filterAlias = "filter(" + exprToCheck.toString().replaceAll("\n", " ") + ")";
        }
        if (parametersSize == 2) {
            filterAlias = Util.extendedToString(parameters.get(0));
            exprToCheck = parameters.get(1);
        }
        Where where = Where.newInstance();
        new WhereParser(new SqlParser()).parseWhere(exprToCheck, where);
        if (where.getWheres().size() == 0) {
            throw new SqlParseException("Failed to parse filter condition");
        }
        List<KVValue> methodParameters = new ArrayList<>();
        methodParameters.add(new KVValue("where", where));
        methodParameters.add(new KVValue("alias", filterAlias + "@FILTER"));
        return new MethodField("filter", methodParameters, null, alias);
    }


    private static Field handleIdentifier(NestedType nestedType, String alias, String tableAlias) {
        Field field = handleIdentifier(new SQLIdentifierExpr(nestedType.field), alias, tableAlias);
        field.setNested(nestedType);
        field.setChildren(null);
        return field;
    }

    private static Field handleIdentifier(ChildrenType childrenType, String alias, String tableAlias) {
        Field field = handleIdentifier(new SQLIdentifierExpr(childrenType.field), alias, tableAlias);
        field.setNested(null);
        field.setChildren(childrenType);
        return field;
    }


    //binary method can nested
    public SQLMethodInvokeExpr makeBinaryMethodField(SQLBinaryOpExpr expr, String alias, boolean first)
            throws SqlParseException {
        List<SQLExpr> params = new ArrayList<>();

        String scriptFieldAlias;
        if (first && (alias == null || alias.equals(""))) {
            scriptFieldAlias = sqlFunctions.nextId("field");
        } else {
            scriptFieldAlias = alias;
        }
        params.add(new SQLCharExpr(scriptFieldAlias));

        switch (expr.getOperator()) {
            case Add:
                return convertBinaryOperatorToMethod("add", expr);
            case Multiply:
                return convertBinaryOperatorToMethod("multiply", expr);

            case Divide:
                return convertBinaryOperatorToMethod("divide", expr);

            case Modulus:
                return convertBinaryOperatorToMethod("modulus", expr);

            case Subtract:
                return convertBinaryOperatorToMethod("subtract", expr);
            default:
                throw new SqlParseException("Unsupported operator: " + expr.getOperator().getName());
        }
    }

    private static SQLMethodInvokeExpr convertBinaryOperatorToMethod(String operator, SQLBinaryOpExpr expr) {
        SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr(operator, null);
        methodInvokeExpr.addParameter(expr.getLeft());
        methodInvokeExpr.addParameter(expr.getRight());
        methodInvokeExpr.putAttribute("source", expr);
        return methodInvokeExpr;
    }


    private static Field handleIdentifier(SQLExpr expr, String alias, String tableAlias) {
        String name = expr.toString().replace("`", "");
        String newFieldName = name;
        Field field = null;
        if (tableAlias != null) {
            String aliasPrefix = tableAlias + ".";
            if (name.startsWith(aliasPrefix)) {
                newFieldName = name.replaceFirst(aliasPrefix, "");
                field = new Field(newFieldName, alias);
            }
        }

        if (tableAlias == null) {
            field = new Field(newFieldName, alias);
        }

        return field;
    }

    public MethodField makeMethodField(String name, List<SQLExpr> arguments, SQLAggregateOption option,
                                              String alias, String tableAlias, boolean first) throws SqlParseException {
        List<KVValue> paramers = new LinkedList<>();

        for (SQLExpr object : arguments) {

            if (object instanceof SQLBinaryOpExpr) {

                SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) object;

                if (SQLFunctions.isFunctionTranslatedToScript(binaryOpExpr.getOperator().toString())) {
                    SQLMethodInvokeExpr mExpr = makeBinaryMethodField(binaryOpExpr, alias, first);
                    MethodField abc = makeMethodField(mExpr.getMethodName(), mExpr.getParameters(),
                            null, null, tableAlias, false);
                    paramers.add(new KVValue(abc.getParams().get(0).toString(),
                            new SQLCharExpr(abc.getParams().get(1).toString())));
                } else {
                    if (!binaryOpExpr.getOperator().getName().equals("=")) {
                        paramers.add(new KVValue("script", makeScriptMethodField(binaryOpExpr, null, tableAlias)));
                    } else {
                        SQLExpr right = binaryOpExpr.getRight();
                        Object value = Util.expr2Object(right);
                        paramers.add(new KVValue(binaryOpExpr.getLeft().toString(), value));
                    }
                }

            } else if (object instanceof SQLMethodInvokeExpr) {
                SQLMethodInvokeExpr mExpr = (SQLMethodInvokeExpr) object;
                String methodName = mExpr.getMethodName().toLowerCase();
                if (methodName.equals("script")) {
                    KVValue script = new KVValue("script", makeMethodField(mExpr.getMethodName(), mExpr.getParameters(),
                            null, alias, tableAlias, true));
                    paramers.add(script);
                } else if (methodName.equals("nested") || methodName.equals("reverse_nested")) {
                    NestedType nestedType = new NestedType();

                    if (!nestedType.tryFillFromExpr(object)) {
                        throw new SqlParseException("Failed to parse nested expression: " + object);
                    }

                    // Fix bug: method name of reversed_nested() was set to "nested" wrongly
                    paramers.add(new KVValue(methodName, nestedType));
                } else if (methodName.equals("children")) {
                    ChildrenType childrenType = new ChildrenType();

                    if (!childrenType.tryFillFromExpr(object)) {
                        throw new SqlParseException("Failed to parse children expression: " + object);
                    }

                    paramers.add(new KVValue("children", childrenType));
                } else if (SQLFunctions.isFunctionTranslatedToScript(methodName)) {
                    //throw new SqlParseException("only support script/nested as inner functions");
                    MethodField abc = makeMethodField(methodName, mExpr.getParameters(), null, null, tableAlias, false);
                    paramers.add(new KVValue(abc.getParams().get(0).toString(),
                            new SQLCharExpr(abc.getParams().get(1).toString())));
                } else {
                    throw new SqlParseException("only support script/nested/children as inner functions");
                }
            } else if (object instanceof SQLCaseExpr) {
                String scriptCode = new CaseWhenParser((SQLCaseExpr) object, alias, tableAlias).parse();
                paramers.add(new KVValue("script", new SQLCharExpr(scriptCode)));
            } else if (object instanceof SQLCastExpr) {
                String castName = sqlFunctions.nextId("cast");
                List<KVValue> methodParameters = new ArrayList<>();
                methodParameters.add(new KVValue(((SQLCastExpr) object).getExpr().toString()));
                String castType = ((SQLCastExpr) object).getDataType().getName();
                String scriptCode = sqlFunctions.getCastScriptStatement(castName, castType, methodParameters);

                // Parameter "first" indicates if return statement is required. Take CAST statement nested in
                // aggregate function SUM(CAST...) for example, return statement is required in this case.
                // Otherwise DSL with metric aggregation always returns 0 as result. And this works also because
                // the caller makeFieldImpl(SQLExpr("SUM...")) does pass first=true to here.
                if (first) {
                    scriptCode += "; return " + castName;
                }
                methodParameters.add(new KVValue(scriptCode));
                paramers.add(new KVValue("script", new SQLCharExpr(scriptCode)));
            } else if (object instanceof SQLAggregateExpr) {
                SQLObject parent = object.getParent();
                SQLExpr source = (SQLExpr) parent.getAttribute("source");

                if (parent instanceof SQLMethodInvokeExpr && source == null) {
                    throw new SqlFeatureNotImplementedException(
                            "Function calls of form '"
                                    + ((SQLMethodInvokeExpr) parent).getMethodName()
                                    + "("
                                    + ((SQLAggregateExpr) object).getMethodName()
                                    + "(...))' are not implemented yet");
                }

                throw new SqlFeatureNotImplementedException(
                        "The complex aggregate expressions are not implemented yet: " + source);
            } else {
                paramers.add(new KVValue(Util.removeTableAilasFromField(object, tableAlias)));
            }

        }

        //just check we can find the function
        boolean builtInScriptFunction = SQLFunctions.isFunctionTranslatedToScript(name);
        if (builtInScriptFunction) {
            if (alias == null && first) {
                alias = sqlFunctions.nextId(name);
            }
            //should check if field and first .
            Tuple<String, String> newFunctions = sqlFunctions.function(name.toLowerCase(), paramers,
                    paramers.isEmpty() ? null : paramers.get(0).key, first);
            paramers.clear();
            if (!first) {
                //variance
                paramers.add(new KVValue(newFunctions.v1()));
            } else {
                paramers.add(new KVValue(alias));
            }

            paramers.add(new KVValue(newFunctions.v2()));
        }
        if (first) {
            List<KVValue> tempParamers = new LinkedList<>();
            for (KVValue temp : paramers) {
                if (temp.value instanceof SQLExpr) {
                    tempParamers.add(new KVValue(temp.key, Util.expr2Object((SQLExpr) temp.value)));
                } else {
                    tempParamers.add(new KVValue(temp.key, temp.value));
                }
            }
            paramers.clear();
            paramers.addAll(tempParamers);
        }

        if (builtInScriptFunction) {
            return new ScriptMethodField(name, paramers, option, alias);
        } else {
            return new MethodField(name, paramers, option, alias);
        }
    }
}
