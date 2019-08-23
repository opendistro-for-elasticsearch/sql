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

package com.amazon.opendistroforelasticsearch.sql.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
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
import com.amazon.opendistroforelasticsearch.sql.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.domain.KVValue;
import com.amazon.opendistroforelasticsearch.sql.domain.MethodField;
import com.amazon.opendistroforelasticsearch.sql.domain.ScriptMethodField;
import com.amazon.opendistroforelasticsearch.sql.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.utils.SQLFunctions;
import com.amazon.opendistroforelasticsearch.sql.utils.Util;
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
    public static Field makeField(SQLExpr expr, String alias, String tableAlias) throws SqlParseException {
        Field field = makeFieldImpl(expr, alias, tableAlias);

        // why we may get null as a field???
        if (field != null) {
            field.setExpression(expr);
        }

        return field;
    }

    private static Field makeFieldImpl(SQLExpr expr, String alias, String tableAlias) throws SqlParseException {
        if (expr instanceof SQLIdentifierExpr || expr instanceof SQLPropertyExpr || expr instanceof SQLVariantRefExpr) {
            return handleIdentifier(expr, alias, tableAlias);
        } else if (expr instanceof SQLQueryExpr) {
            throw new SqlParseException("unknow field name : " + expr);
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
            String scriptCode = new CastParser(castExpr, alias, tableAlias).parse(true);
            List<KVValue> methodParameters = new ArrayList<>();
            methodParameters.add(new KVValue(alias));
            methodParameters.add(new KVValue(scriptCode));
            return new MethodField("script", methodParameters, null, alias);
        } else if (expr instanceof SQLNumericLiteralExpr) {
            SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr("assign", null);
            methodInvokeExpr.addParameter(expr);
            return makeMethodField(methodInvokeExpr.getMethodName(), methodInvokeExpr.getParameters(),
                    null, alias, tableAlias, true);
        } else {
            throw new SqlParseException("unknown field name : " + expr);
        }
    }

    private static Object getScriptValue(SQLExpr expr) throws SqlParseException {
        return Util.getScriptValue(expr);
    }

    private static Field makeScriptMethodField(SQLBinaryOpExpr binaryExpr, String alias, String tableAlias)
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
            throw new SqlParseException("unable to parse filter where.");
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
    public static SQLMethodInvokeExpr makeBinaryMethodField(SQLBinaryOpExpr expr, String alias, boolean first)
            throws SqlParseException {
        List<SQLExpr> params = new ArrayList<>();

        String scriptFieldAlias;
        if (first && (alias == null || alias.equals(""))) {
            scriptFieldAlias = SQLFunctions.randomize("field");
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
                throw new SqlParseException(expr.getOperator().getName() + " is not support");
        }
    }

    private static SQLMethodInvokeExpr convertBinaryOperatorToMethod(String operator, SQLBinaryOpExpr expr) {
        SQLMethodInvokeExpr methodInvokeExpr = new SQLMethodInvokeExpr(operator, null);
        methodInvokeExpr.addParameter(expr.getLeft());
        methodInvokeExpr.addParameter(expr.getRight());
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

    public static MethodField makeMethodField(String name, List<SQLExpr> arguments, SQLAggregateOption option,
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
                        throw new SqlParseException("failed parsing nested expr " + object);
                    }

                    // Fix bug: method name of reversed_nested() was set to "nested" wrongly
                    paramers.add(new KVValue(methodName, nestedType));
                } else if (methodName.equals("children")) {
                    ChildrenType childrenType = new ChildrenType();

                    if (!childrenType.tryFillFromExpr(object)) {
                        throw new SqlParseException("failed parsing children expr " + object);
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
                String scriptCode = new CastParser((SQLCastExpr) object, alias, tableAlias).parse(false);
                paramers.add(new KVValue("script", new SQLCharExpr(scriptCode)));
            } else {
                paramers.add(new KVValue(Util.removeTableAilasFromField(object, tableAlias)));
            }

        }

        //just check we can find the function
        boolean builtInScriptFunction = SQLFunctions.isFunctionTranslatedToScript(name);
        if (builtInScriptFunction) {
            if (alias == null && first) {
                alias = SQLFunctions.randomize(name);
            }
            //should check if field and first .
            Tuple<String, String> newFunctions = SQLFunctions.function(name.toLowerCase(), paramers,
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
            return new ScriptMethodField(name, paramers, option == null ? null : option.name(), alias);
        } else {
            return new MethodField(name, paramers, option == null ? null : option.name(), alias);
        }
    }
}
