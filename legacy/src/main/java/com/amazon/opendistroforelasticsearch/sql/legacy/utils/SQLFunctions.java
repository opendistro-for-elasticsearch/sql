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

package com.amazon.opendistroforelasticsearch.sql.legacy.utils;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBooleanExpr;
import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumericLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLTextLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.KVValue;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.MethodField;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.ScriptMethodField;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.Schema;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.elasticsearch.common.collect.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils.format;
import static com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils.isQuoted;

/**
 * Created by allwefantasy on 8/19/16.
 */
public class SQLFunctions {

    private static final Set<String> numberOperators = Sets.newHashSet(
            "exp", "expm1", "log", "log2", "log10", "ln", "sqrt", "cbrt", "ceil", "floor", "rint", "pow", "power",
            "round", "rand", "abs", "sign", "signum"
    );

    private static final Set<String> mathConstants = Sets.newHashSet("e", "pi");

    private static final Set<String> trigFunctions = Sets.newHashSet(
            "degrees", "radians", "sin", "cos", "tan", "asin", "acos", "atan", "atan2", "sinh", "cosh", "cot"
    );

    private static final Set<String> stringOperators = Sets.newHashSet(
            "split", "concat_ws", "substring", "trim", "lower", "upper", "rtrim", "ltrim", "replace",
            "left", "right"
    );

    private static final Set<String> stringFunctions = Sets.newHashSet(
            "length", "locate", "ascii"
    );

    private static final Set<String> binaryOperators = Sets.newHashSet(
            "add", "multiply", "divide", "subtract", "modulus"
    );

    private static final Set<String> dateFunctions = Sets.newHashSet(
            "date_format", "year", "month_of_year", "week_of_year", "day_of_year", "day_of_month",
            "day_of_week", "hour_of_day", "minute_of_day", "minute_of_hour", "second_of_minute", "month", "dayofmonth",
            "date", "monthname", "timestamp", "maketime", "now", "curdate"
    );

    private static final Set<String> conditionalFunctions = Sets.newHashSet(
            "if", "ifnull", "isnull"
    );

    private static final Set<String> utilityFunctions = Sets.newHashSet("field", "assign", "cast");

    public static final Set<String> builtInFunctions = Stream.of(
            numberOperators,
            mathConstants,
            trigFunctions,
            stringOperators,
            stringFunctions,
            binaryOperators,
            dateFunctions,
            conditionalFunctions,
            utilityFunctions)
            .flatMap(Set::stream).collect(Collectors.toSet());

    private Map<String, Integer> generatedIds = new HashMap<>();

    /**
     * Generates next id for given method name. The id's are increasing for each method name, so
     * nextId("a"), nextId("a"), nextId("b") will return a_1, a_2, b_1
     */
    public String nextId(String methodName) {
        return methodName + "_" + generatedIds.merge(methodName, 1, Integer::sum);
    }


    /**
     * Is the function actually translated into Elastic DSL script during execution?
     */
    public static boolean isFunctionTranslatedToScript(String function) {
        return builtInFunctions.contains(function.toLowerCase());
    }

    public Tuple<String, String> function(String methodName, List<KVValue> paramers, String name,
                                                 boolean returnValue) throws SqlParseException {
        Tuple<String, String> functionStr = null;
        switch (methodName.toLowerCase()) {
            case "cast": {
                SQLCastExpr castExpr = (SQLCastExpr) ((SQLIdentifierExpr) paramers.get(0).value).getParent();
                String typeName = castExpr.getDataType().getName();
                functionStr = cast(typeName, paramers);
                break;
            }
            case "lower": {
                functionStr = lower(
                        (SQLExpr) paramers.get(0).value,
                        getLocaleForCaseChangingFunction(paramers),
                        name
                );
                break;
            }
            case "upper": {
                functionStr = upper(
                        (SQLExpr) paramers.get(0).value,
                        getLocaleForCaseChangingFunction(paramers),
                        name);
                break;
            }

            // Split is currently not supported since its using .split() in painless which is not whitelisted
            case "split":
                if (paramers.size() == 3) {
                    functionStr = split((SQLExpr) paramers.get(0).value,
                            Util.expr2Object((SQLExpr) paramers.get(1).value).toString(),
                            Integer.parseInt(Util.expr2Object((SQLExpr) paramers.get(2).value).toString()), name);
                } else {
                    functionStr = split((SQLExpr) paramers.get(0).value,
                            paramers.get(1).value.toString(),
                            name);
                }

                break;

            case "concat_ws":
                List<SQLExpr> result = Lists.newArrayList();
                for (int i = 1; i < paramers.size(); i++) {
                    result.add((SQLExpr) paramers.get(i).value);
                }
                functionStr = concat_ws(paramers.get(0).value.toString(), result);

                break;


            case "date_format":
                functionStr = date_format(
                        (SQLExpr) paramers.get(0).value,
                        Util.expr2Object((SQLExpr) paramers.get(1).value).toString(),
                        paramers.size() > 2 ? Util.expr2Object((SQLExpr) paramers.get(2).value).toString() : null,
                        name);
                break;

            case "year":
                functionStr = dateFunctionTemplate("year", (SQLExpr) paramers.get(0).value);
                break;
            case "month_of_year":
            case "month":
                functionStr = dateFunctionTemplate("monthValue", (SQLExpr) paramers.get(0).value);
                break;
            case "monthname":
                functionStr = dateFunctionTemplate("month", (SQLExpr) paramers.get(0).value);
                break;
            case "week_of_year":
                functionStr = dateFunctionTemplate("weekOfWeekyear",
                                                   "get(WeekFields.ISO.weekOfWeekBasedYear())",
                                                   (SQLExpr) paramers.get(0).value);
                break;
            case "day_of_year":
                functionStr = dateFunctionTemplate("dayOfYear", (SQLExpr) paramers.get(0).value);
                break;
            case "day_of_month":
            case "dayofmonth":
                functionStr = dateFunctionTemplate("dayOfMonth", (SQLExpr) paramers.get(0).value);
                break;
            case "day_of_week":
                functionStr = dateFunctionTemplate("dayOfWeek",
                                                   "getDayOfWeekEnum().getValue()",
                                                   (SQLExpr) paramers.get(0).value);
                break;
            case "date":
                functionStr = date((SQLExpr) paramers.get(0).value);
                break;
            case "hour_of_day":
                functionStr = dateFunctionTemplate("hour", (SQLExpr) paramers.get(0).value);
                break;
            case "minute_of_day":
                functionStr = dateFunctionTemplate("minuteOfDay",
                                                   "get(ChronoField.MINUTE_OF_DAY)",
                                                   (SQLExpr) paramers.get(0).value);
                break;
            case "minute_of_hour":
                functionStr = dateFunctionTemplate("minute", (SQLExpr) paramers.get(0).value);
                break;
            case "second_of_minute":
                functionStr = dateFunctionTemplate("second", (SQLExpr) paramers.get(0).value);
                break;
            case "timestamp":
                functionStr = timestamp((SQLExpr) paramers.get(0).value);
                break;
            case "maketime":
                functionStr = maketime((SQLExpr) paramers.get(0).value, (SQLExpr) paramers.get(1).value,
                        (SQLExpr) paramers.get(2).value);
                break;
            case "now":
                functionStr = now();
                break;
            case "curdate":
                functionStr = curdate();
                break;

            case "e":
            case "pi":
                methodName = methodName.toUpperCase();
                functionStr = mathConstantTemplate("Math." + methodName, methodName);
                break;

            case "abs":
            case "round":
            case "floor":
            case "ceil":
            case "cbrt":
            case "rint":
            case "exp":
            case "expm1":
            case "sqrt":
            case "sin":
            case "cos":
            case "tan":
            case "asin":
            case "acos":
            case "atan":
            case "sinh":
            case "cosh":
                functionStr = mathSingleValueTemplate("Math." + methodName, methodName,
                        (SQLExpr) paramers.get(0).value, name);
                break;

            case "rand":
                if (paramers.isEmpty()) {
                    functionStr = rand();
                } else {
                    functionStr = rand((SQLExpr) paramers.get(0).value);
                }
                break;

            case "cot":
                // ES does not support the function name cot
                functionStr = mathSingleValueTemplate("1 / Math.tan", methodName,
                        (SQLExpr) paramers.get(0).value, name);
                break;

            case "sign":
            case "signum":
                methodName = "signum";
                functionStr = mathSingleValueTemplate("Math." + methodName, methodName,
                        (SQLExpr) paramers.get(0).value, name);
                break;

            case "pow":
            case "power":
                methodName = "pow";
                functionStr = mathDoubleValueTemplate("Math." + methodName, methodName,
                        (SQLExpr) paramers.get(0).value, Util.expr2Object((SQLExpr) paramers.get(1).value).toString(),
                        name);
                break;

            case "atan2":
                functionStr = mathDoubleValueTemplate("Math." + methodName, methodName,
                        (SQLExpr) paramers.get(0).value, (SQLExpr) paramers.get(1).value);
                break;

            case "substring":
                functionStr = substring((SQLExpr) paramers.get(0).value,
                        Integer.parseInt(Util.expr2Object((SQLExpr) paramers.get(1).value).toString()),
                        Integer.parseInt(Util.expr2Object((SQLExpr) paramers.get(2).value).toString()));
                break;

            case "degrees":
                functionStr = degrees((SQLExpr) paramers.get(0).value, name);
                break;
            case "radians":
                functionStr = radians((SQLExpr) paramers.get(0).value, name);
                break;

            case "trim":
                functionStr = trim((SQLExpr) paramers.get(0).value, name);
                break;

            case "add":
                functionStr = add((SQLExpr) paramers.get(0).value, (SQLExpr) paramers.get(1).value);
                break;

            case "subtract":
                functionStr = subtract((SQLExpr) paramers.get(0).value, (SQLExpr) paramers.get(1).value);
                break;
            case "divide":
                functionStr = divide((SQLExpr) paramers.get(0).value, (SQLExpr) paramers.get(1).value);
                break;

            case "multiply":
                functionStr = multiply((SQLExpr) paramers.get(0).value, (SQLExpr) paramers.get(1).value);
                break;
            case "modulus":
                functionStr = modulus((SQLExpr) paramers.get(0).value, (SQLExpr) paramers.get(1).value);
                break;

            case "field":
                functionStr = field(Util.expr2Object((SQLExpr) paramers.get(0).value).toString());
                break;

            case "log2":
                functionStr = log(SQLUtils.toSQLExpr("2"), (SQLExpr) paramers.get(0).value, name);
                break;
            case "log10":
                functionStr = log10((SQLExpr) paramers.get(0).value);
                break;
            case "log":
                if (paramers.size() > 1) {
                    functionStr = log((SQLExpr) paramers.get(0).value, (SQLExpr) paramers.get(1).value, name);
                } else {
                    functionStr = ln((SQLExpr) paramers.get(0).value);
                }
                break;
            case "ln":
                functionStr = ln((SQLExpr) paramers.get(0).value);
                break;
            case "assign":
                functionStr = assign((SQLExpr) paramers.get(0).value);
                break;
            case "length":
                functionStr = length((SQLExpr) paramers.get(0).value);
                break;
            case "replace":
                functionStr = replace((SQLExpr) paramers.get(0).value, paramers.get(1).value.toString(),
                        paramers.get(2).value.toString());
                break;
            case "locate":
                int start = 0;
                if (paramers.size() > 2) {
                    start = Integer.parseInt(paramers.get(2).value.toString());
                }
                functionStr = locate(paramers.get(0).value.toString(), (SQLExpr) paramers.get(1).value, start);
                break;
            case "rtrim":
                functionStr = rtrim((SQLExpr) paramers.get(0).value);
                break;
            case "ltrim":
                functionStr = ltrim((SQLExpr) paramers.get(0).value);
                break;
            case "ascii":
                functionStr = ascii((SQLExpr) paramers.get(0).value);
                break;
            case "left":
                functionStr = left((SQLExpr) paramers.get(0).value, (SQLExpr) paramers.get(1).value);
                break;
            case "right":
                functionStr = right((SQLExpr) paramers.get(0).value, (SQLExpr) paramers.get(1).value);
                break;

            case "if":
                functionStr = ifFunc(paramers);
                break;
            case "ifnull":
                functionStr = ifnull((SQLExpr) paramers.get(0).value, (SQLExpr) paramers.get(1).value);
                break;
            case "isnull":
                functionStr = isnull((SQLExpr) paramers.get(0).value);
                break;

            default:

        }
        if (returnValue) {
            String generatedFieldName = functionStr.v1();
            String returnCommand = ";return " + generatedFieldName + ";";
            String newScript = functionStr.v2() + returnCommand;
            functionStr = new Tuple<>(generatedFieldName, newScript);
        }
        return functionStr;
    }

    public String getLocaleForCaseChangingFunction(List<KVValue> paramers) {
        String locale;
        if (paramers.size() == 1) {
            locale = Locale.getDefault().getLanguage();
        } else {
            locale = Util.expr2Object((SQLExpr) paramers.get(1).value).toString();
        }
        return locale;
    }

    public Tuple<String, String> cast(String castType, List<KVValue> paramers) throws SqlParseException {
        String name = nextId("cast");
        return new Tuple<>(name, getCastScriptStatement(name, castType, paramers));
    }


    public Tuple<String, String> upper(SQLExpr field, String locale, String valueName) {
        String name = nextId("upper");

        if (valueName == null) {
            return new Tuple<>(name, def(name, upper(getPropertyOrStringValue(field), locale)));
        } else {
            return new Tuple<>(name, getPropertyOrStringValue(field) + "; "
                    + def(name, valueName + "." + upper(getPropertyOrStringValue(field), locale)));
        }
    }

    public Tuple<String, String> lower(SQLExpr field, String locale, String valueName) {
        String name = nextId("lower");

        if (valueName == null) {
            return new Tuple<>(name, def(name, lower(getPropertyOrStringValue(field), locale)));
        } else {
            return new Tuple<>(name, getPropertyOrStringValue(field) + "; "
                    + def(name, valueName + "." + lower(getPropertyOrStringValue(field), locale)));
        }
    }

    private static String def(String name, String value) {
        return "def " + name + " = " + value;
    }

    private static String doc(SQLExpr field) {
        return "doc['" + exprString(field) + "']";
    }

    private static String doc(String field) {
        return "doc['" + field + "']";
    }

    private static String exprString(SQLExpr expr) {
        return Util.expr2Object(expr).toString();
    }

    private static String func(String methodName, boolean quotes, String... params) {
        if (quotes) {
            return methodName + "(" + quoteParams(params) + ")";
        }

        return methodName + "(" + String.join(", ", params) + ")";
    }

    /**
     * Helper method to surround each param with '' (single quotes) for painless script
     */
    private static String quoteParams(String... params) {
        return Stream.of(params).collect(Collectors.joining("', '", "'", "'"));
    }

    private Tuple<String, String> concat_ws(String split, List<SQLExpr> columns) {
        String name = nextId("concat_ws");
        List<String> result = Lists.newArrayList();

        for (SQLExpr column : columns) {
            String strColumn = exprString(column);
            if (strColumn.startsWith("def ")) {
                result.add(strColumn);
            } else if (isProperty(column)) {
                result.add("doc['" + strColumn + "'].value");
            } else {
                result.add("'" + strColumn + "'");
            }

        }
        return new Tuple<>(name, def(name, Joiner.on("+ " + split + " +").join(result)));
    }


    //split(Column expr, java.lang.String pattern)
    public Tuple<String, String> split(SQLExpr field, String pattern, int index, String valueName) {
        String name = nextId("split");
        final String script;
        if (valueName == null) {
            script = def(name,
                    getPropertyOrValue(field) + "."
                    + func("split", true, pattern) + "[" + index + "]");
        } else {
            script = "; " + def(name, valueName + "."
                    + func("split", true, pattern) + "[" + index + "]");
        }
        return new Tuple<>(name, script);
    }

    //split(Column expr, java.lang.String pattern)
    public Tuple<String, String> split(SQLExpr field, String pattern, String valueName) {
        String name = nextId("split");
        if (valueName == null) {
            return new Tuple<>(name,
                    def(name, getPropertyOrValue(field) + "."
                            + func("split", true, pattern)));
        } else {
            return new Tuple<>(name, getPropertyOrValue(field) + "; "
                    + def(name, valueName + "." + func("split", true, pattern)));
        }
    }

    private Tuple<String, String> date_format(SQLExpr field, String pattern, String zoneId, String valueName) {
        String name = nextId("date_format");
        if (valueName == null) {
            return new Tuple<>(name, "def " + name + " = DateTimeFormatter.ofPattern('" + pattern + "').withZone("
                    + (zoneId != null ? "ZoneId.of('" + zoneId + "')" : "ZoneId.of(\"UTC\")")
                    + ").format(Instant.ofEpochMilli(" + getPropertyOrValue(field) + ".toInstant().toEpochMilli()))");
        } else {
            return new Tuple<>(name, exprString(field) + "; "
                    + "def " + name + " = new SimpleDateFormat('" + pattern + "').format("
                    + "new Date(" + valueName + " - 8*1000*60*60))");
        }
    }

    /**
     * Explicitly pass in name used to generate variable ID because methodName is not always valid
     *
     * For example,
     *  <code>
     *      functionStr = dateFunctionTemplate("weekOfWeekyear",
     *                                         "get(WeekFields.ISO.weekOfWeekBasedYear())",
     *                                         (SQLExpr) paramers.get(0).value);
     *  </code>
     *
     *  The old dateFunctionTemplate(methodName, field) passes string "get(WeekFields.ISO.weekOfWeekBasedYear())"
     *  to nextId() which generates an invalid variable name in painless script.
     */
    private Tuple<String, String> dateFunctionTemplate(String name, String methodName, SQLExpr field) {
        String id = nextId(name);
        return new Tuple<>(id, def(id, doc(field) + ".value." + methodName));
    }

    private Tuple<String, String> dateFunctionTemplate(String methodName, SQLExpr field) {
        return dateFunctionTemplate(methodName, methodName, field);
    }

    public Tuple<String, String> add(SQLExpr a, SQLExpr b) {
        return binaryOpertator("add", "+", a, b);
    }

    public Tuple<String, String> assign(SQLExpr a) {
        String name = nextId("assign");
        return new Tuple<>(name,
                def(name, extractName(a)));
    }

    private Tuple<String, String> modulus(SQLExpr a, SQLExpr b) {
        return binaryOpertator("modulus", "%", a, b);
    }

    public Tuple<String, String> field(String a) {
        String name = nextId("field");
        return new Tuple<>(name, def(name, doc(a) + ".value"));
    }

    private Tuple<String, String> subtract(SQLExpr a, SQLExpr b) {
        return binaryOpertator("subtract", "-", a, b);
    }

    private Tuple<String, String> multiply(SQLExpr a, SQLExpr b) {
        return binaryOpertator("multiply", "*", a, b);
    }

    private Tuple<String, String> divide(SQLExpr a, SQLExpr b) {
        return binaryOpertator("divide", "/", a, b);
    }

    private Tuple<String, String> binaryOpertator(String methodName, String operator, SQLExpr a, SQLExpr b) {
        String name = nextId(methodName);
        return new Tuple<>(name,
                scriptDeclare(a) + scriptDeclare(b) + convertType(a) + convertType(b)
                        + def(name, extractName(a) + " " + operator + " " + extractName(b)));
    }

    private static boolean isProperty(SQLExpr expr) {
        return (expr instanceof SQLIdentifierExpr || expr instanceof SQLPropertyExpr
                || expr instanceof SQLVariantRefExpr);
    }

    private static String getPropertyOrValue(SQLExpr expr) {
        if (isProperty(expr)) {
            return doc(expr) + ".value";
        } else {
            return exprString(expr);
        }
    }

    private static String getPropertyOrValue(String expr) {
        if (isQuoted(expr, "'")) {
            return expr;
        } else if (StringUtils.isNumeric(expr)) {
            return expr;
        } else {
            return doc(expr) + ".value";
        }
    }

    private static String getPropertyOrStringValue(SQLExpr expr) {
        if (isProperty(expr)) {
            return doc(expr) + ".value";
        } else {
            return "'" + exprString(expr) + "'";
        }
    }

    private static String scriptDeclare(SQLExpr a) {

        if (isProperty(a) || a instanceof SQLNumericLiteralExpr) {
            return "";
        } else {
            return exprString(a) + ";";
        }
    }

    private static String extractName(SQLExpr script) {
        if (isProperty(script)) {
            return doc(script) + ".value";
        }
        String scriptStr = exprString(script);
        String[] variance = scriptStr.split(";");
        String newScript = variance[variance.length - 1];
        if (newScript.trim().startsWith("def ")) {
            //for now ,if variant is string,then change to double.
            return newScript.trim().substring(4).split("=")[0].trim();
        } else {
            return scriptStr;
        }
    }

    //cast(year as int)

    private static String convertType(SQLExpr script) {
        String[] variance = exprString(script).split(";");
        String newScript = variance[variance.length - 1];
        if (newScript.trim().startsWith("def ")) {
            //for now ,if variant is string,then change to double.
            String temp = newScript.trim().substring(4).split("=")[0].trim();

            return " if( " + temp + " instanceof String) " + temp + "= Double.parseDouble(" + temp.trim() + "); ";
        } else {
            return "";
        }


    }

    private String getScriptText(MethodField field) {
        String content = ((SQLTextLiteralExpr) field.getParams().get(1).value).getText();
        return content;
    }

    /**
     * Using exprString() rather than getPropertyOrValue() for "base" since something like "Math.E" gets evaluated
     * incorrectly in getPropertyOrValue(), returning it as a doc value instead of the literal string
     */
    public Tuple<String, String> log(SQLExpr base, SQLExpr field, String valueName) {
        String name = nextId("log");
        String result;
        if (valueName == null) {
            result = def(name, func("Math.log", false, getPropertyOrValue(field))
                    + "/" + func("Math.log", false, exprString(base)));
        } else {
            result = getPropertyOrValue(field) + "; "
                    + def(name, func("Math.log", false, valueName) + "/"
                            + func("Math.log", false, exprString(base)));
        }
        return new Tuple<>(name, result);
    }

    public Tuple<String, String> log10(SQLExpr field) {
        String name = nextId("log10");
        return new Tuple<>(name, def(name, StringUtils.format("Math.log10(%s)", getPropertyOrValue(field))));
    }

    public Tuple<String, String> ln(SQLExpr field) {
        String name = nextId("ln");
        return new Tuple<>(name, def(name, StringUtils.format("Math.log(%s)", getPropertyOrValue(field))));
    }

    public Tuple<String, String> trim(SQLExpr field, String valueName) {
        return strSingleValueTemplate("trim", field, valueName);
    }

    private Tuple<String, String> degrees(SQLExpr field, String valueName) {
        return mathSingleValueTemplate("Math.toDegrees", "degrees", field, valueName);
    }

    private Tuple<String, String> radians(SQLExpr field, String valueName) {
        return mathSingleValueTemplate("Math.toRadians", "radians", field, valueName);
    }

    private Tuple<String, String> rand(SQLExpr expr) {
        String name = nextId("rand");
        return new Tuple<>(name, def(name, format("new Random(%s).nextDouble()", getPropertyOrValue(expr))));
    }

    private Tuple<String, String> rand() {
        String name = nextId("rand");
        return new Tuple<>(name, def(name, "new Random().nextDouble()"));
    }

    private Tuple<String, String> mathDoubleValueTemplate(String methodName, String fieldName, SQLExpr val1,
                                                                 String val2, String valueName) {
        String name = nextId(fieldName);
        if (valueName == null) {
            return new Tuple<>(name, def(name, func(methodName, false, getPropertyOrValue(val1),
                    getPropertyOrValue(val2))));
        } else {
            return new Tuple<>(name, getPropertyOrValue(val1) + "; "
                    + def(name, func(methodName, false, valueName, getPropertyOrValue(val2))));
        }
    }

    private Tuple<String, String> mathDoubleValueTemplate(String methodName, String fieldName, SQLExpr val1,
                                                          SQLExpr val2) {
        String name = nextId(fieldName);
        return new Tuple<>(name, def(name, func(methodName, false,
                getPropertyOrValue(val1), getPropertyOrValue(val2))));
    }

    private Tuple<String, String> mathSingleValueTemplate(String methodName, String fieldName, SQLExpr field,
                                                                 String valueName) {
        String name = nextId(fieldName);
        if (valueName == null) {
            return new Tuple<>(name, def(name, func(methodName, false, getPropertyOrValue(field))));
        } else {
            return new Tuple<>(name, getPropertyOrValue(field) + "; "
                    + def(name, func(methodName, false, valueName)));
        }

    }

    private Tuple<String, String> mathConstantTemplate(String methodName, String fieldName) {
        String name = nextId(fieldName);
        return new Tuple<>(name, def(name, methodName));
    }

    private Tuple<String, String> strSingleValueTemplate(String methodName, SQLExpr field, String valueName) {
        String name = nextId(methodName);
        if (valueName == null) {
            return new Tuple<>(name, def(name, getPropertyOrStringValue(field) + "." + func(methodName, false)));
        } else {
            return new Tuple<>(name, getPropertyOrStringValue(field) + "; "
                    + def(name, valueName + "." + func(methodName, false)));
        }

    }

    // query: substring(Column expr, int pos, int len)
    // painless script: substring(int begin, int end)
    // es behavior: 1-index, supports out-of-bound index
    public Tuple<String, String> substring(SQLExpr field, int pos, int len) {
        String name = nextId("substring");
        // start and end are 0-indexes
        int start = pos < 1 ? 0 : pos - 1;
        return new Tuple<>(name, StringUtils.format(
                "def end = (int) Math.min(%s + %s, %s.length()); "
                + def(name, getPropertyOrStringValue(field) + "."
                + func("substring", false, Integer.toString(start), "end")),
                Integer.toString(start), Integer.toString(len), getPropertyOrStringValue(field)
        ));
    }

    private String lower(String property, String culture) {
        return property + ".toLowerCase(Locale.forLanguageTag(\"" + culture + "\"))";
    }

    private String upper(String property, String culture) {
        return property + ".toUpperCase(Locale.forLanguageTag(\"" + culture + "\"))";
    }

    private Tuple<String, String> length(SQLExpr field) {
        String name = nextId("length");
        return new Tuple<>(name, def(name, getPropertyOrStringValue(field) + ".length()"));
    }

    private Tuple<String, String> replace(SQLExpr field, String target, String replacement) {
        String name = nextId("replace");
        return new Tuple<>(name, def(name, getPropertyOrStringValue(field)
                + ".replace(" + target + "," + replacement + ")"));
    }

    // es behavior: both 'start' and return value are 1-index; return 0 if pattern does not exist;
    // support out-of-bound index
    private Tuple<String, String> locate(String pattern, SQLExpr source, int start) {
        String name = nextId("locate");
        String docSource = getPropertyOrStringValue(source);
        start = start < 1 ? 0 : start - 1;
        return new Tuple<>(name, def(name, StringUtils.format("%s.indexOf(%s,%d)+1", docSource, pattern, start)));
    }

    private Tuple<String, String> rtrim(SQLExpr field) {
        String name = nextId("rtrim");
        String fieldString = getPropertyOrStringValue(field);
        return new Tuple<>(name, StringUtils.format(
                "int pos=%s.length()-1;"
                + "while(pos >= 0 && Character.isWhitespace(%s.charAt(pos))) {pos --;} "
                + def(name, "%s.substring(0, pos+1)"),
                fieldString, fieldString, fieldString
        ));
    }

    private Tuple<String, String> ltrim(SQLExpr field) {
        String name = nextId("ltrim");
        String fieldString = getPropertyOrStringValue(field);
        return new Tuple<>(name, StringUtils.format(
                "int pos=0;"
                + "while(pos < %s.length() && Character.isWhitespace(%s.charAt(pos))) {pos ++;} "
                + def(name, "%s.substring(pos, %s.length())"),
                fieldString, fieldString, fieldString, fieldString
        ));
    }

    private Tuple<String, String> ascii(SQLExpr field) {
        String name = nextId("ascii");
        return new Tuple<>(name, def(name, "(int) " + getPropertyOrStringValue(field) + ".charAt(0)"));
    }

    private Tuple<String, String> left(SQLExpr expr, SQLExpr length) {
        String name = nextId("left");
        return new Tuple<>(name, StringUtils.format(
                "def len = (int) Math.min(%s, %s.length()); def %s = %s.substring(0, len)",
                exprString(length), getPropertyOrStringValue(expr), name, getPropertyOrStringValue(expr)));
    }

    private Tuple<String, String> right(SQLExpr expr, SQLExpr length) {
        String name = nextId("right");
        return new Tuple<>(name, StringUtils.format(
                "def start = (int) Math.max(0, %s.length()-%s); def %s = %s.substring(start)",
                getPropertyOrStringValue(expr), exprString(length), name, getPropertyOrStringValue(expr)));
    }

    private Tuple<String, String> date(SQLExpr field) {
        String name = nextId("date");
        return new Tuple<>(name, def(name,
                "LocalDate.parse(" + getPropertyOrStringValue(field) + ".toString(),"
                        + "DateTimeFormatter.ISO_DATE_TIME)"));
    }

    private Tuple<String, String> timestamp(SQLExpr field) {
        String name = nextId("timestamp");
        return new Tuple<>(name, def(name,
                "DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm:ss').format("
                        + "DateTimeFormatter.ISO_DATE_TIME.parse("
                        + getPropertyOrStringValue(field) + ".toString()))"));
    }

    private Tuple<String, String> maketime(SQLExpr hr, SQLExpr min, SQLExpr sec) {
        String name = nextId("maketime");
        return new Tuple<>(name, def(name, StringUtils.format(
                "LocalTime.of(%s, %s, %s).format(DateTimeFormatter.ofPattern('HH:mm:ss'))",
                hr.toString(), min.toString(), sec.toString())));
    }

    private Tuple<String, String> now() {
        String name = nextId("now");
        return new Tuple<>(name, def(name, "new SimpleDateFormat('HH:mm:ss').format(System.currentTimeMillis())"));
    }

    private Tuple<String, String> curdate() {
        String name = nextId("curdate");
        return new Tuple<>(name, def(name, "new SimpleDateFormat('yyyy-MM-dd').format(System.currentTimeMillis())"));
    }

    private Tuple<String, String> ifFunc(List<KVValue> paramers) {
        String expr1 = paramers.get(1).value.toString();
        String expr2 = paramers.get(2).value.toString();
        String name = nextId("if");

        /** Input with null is regarded as false */
        if (paramers.get(0).value instanceof SQLNullExpr) {
            return new Tuple<>(name, def(name, expr2));
        }
        if (paramers.get(0).value instanceof MethodField) {
            String condition = getScriptText((MethodField) paramers.get(0).value);
            return new Tuple<>(name, "boolean cond = " + condition + ";"
                    + def(name, "cond ? " + expr1 + " : " + expr2));
        } else if (paramers.get(0).value instanceof SQLBooleanExpr) {
            Boolean condition = ((SQLBooleanExpr) paramers.get(0).value).getValue();
            if (condition) {
                return new Tuple<>(name, def(name, expr1));
            } else {
                return new Tuple<>(name, def(name, expr2));
            }
        } else {
            /**
             * Detailed explanation of cases that come here:
             * the condition expression would be in the format of a=b:
             * a is parsed as the key (String) of a KVValue (get from paramers.get(0))
             * and b is parsed as the value (Object) of this KVValue.
             *
             * Either a or b could be a column name, literal, or a number:
             * - if isNumeric is true --> number
             * - else if this string is single quoted --> literal
             * - else --> column name
             */
            String key = getPropertyOrValue(paramers.get(0).key);
            String value = getPropertyOrValue(paramers.get(0).value.toString());
            String condition = key + " == " + value;
            return new Tuple<>(name, "boolean cond = " + condition + ";"
                    + def(name, "cond ? " + expr1 + " : " + expr2));
        }
    }

    private Tuple<String, String> ifnull(SQLExpr condition, SQLExpr expr) {
        String name = nextId("ifnull");
        if (condition instanceof SQLNullExpr) {
            return new Tuple<>(name, def(name, expr.toString()));
        }
        if (isProperty(condition)) {
            return new Tuple<>(name, def(name, doc(condition) + ".size()==0 ? " + expr.toString() + " : "
                    + getPropertyOrValue(condition)));
        } else {
            String condStr = Strings.isNullOrEmpty(condition.toString()) ? null : getPropertyOrStringValue(condition);
            return new Tuple<>(name, def(name, condStr));
        }
    }

    private Tuple<String, String> isnull(SQLExpr expr) {
        String name = nextId("isnull");
        if (expr instanceof SQLNullExpr) {
            return new Tuple<>(name, def(name, "1"));
        }
        if (isProperty(expr)) {
            return new Tuple<>(name, def(name, doc(expr) + ".size()==0 ? 1 : 0"));
        }
        // cases that return 1:
        // expr is null || expr is math func but tends to throw "divided by zero" arithmetic exception
        String resultStr = "0";
        if (Strings.isNullOrEmpty(expr.toString())) {
            resultStr = "1";
        }
        if (expr instanceof SQLCharExpr && this.generatedIds.size() > 1) {
            // the expr is a math expression
            String mathExpr = ((SQLCharExpr) expr).getText();
            return new Tuple<>(name, StringUtils.format(
                    "try {%s;} "
                            + "catch(ArithmeticException e) "
                            + "{return 1;} "
                            + "def %s=0",
                    mathExpr, name, name)
            );
        }
        return new Tuple<>(name, def(name, resultStr));
    }

    public String getCastScriptStatement(String name, String castType, List<KVValue> paramers)
            throws SqlParseException {
        String castFieldName = String.format("doc['%s'].value", paramers.get(0).toString());
        switch (StringUtils.toUpper(castType)) {
            case "INT":
            case "LONG":
            case "FLOAT":
            case "DOUBLE":
                return getCastToNumericValueScript(name, castFieldName, StringUtils.toLower(castType));
            case "STRING":
                return String.format("def %s = %s.toString()", name, castFieldName);
            case "DATETIME":
                return String.format("def %s = DateTimeFormatter.ofPattern(\"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\").format("
                        + "DateTimeFormatter.ISO_DATE_TIME.parse(%s.toString()))", name, castFieldName);
            default:
                throw new SqlParseException("Unsupported cast type " + castType);
        }
    }

    private String getCastToNumericValueScript(String varName, String docValue, String targetType) {
        String script =
            "def %1$s = (%2$s instanceof boolean) "
                + "? (%2$s ? 1 : 0) "
                + ": Double.parseDouble(%2$s.toString()).%3$sValue()";
        return StringUtils.format(script, varName, docValue, targetType);
    }

    /**
     * Returns return type of script function. This is simple approach, that might be not the best solution in the long
     * term. For example - for JDBC, if the column type in index is INTEGER, and the query is "select column+5", current
     * approach will return type of result column as DOUBLE, although there is enough information to understand that
     * it might be safely treated as INTEGER.
     */
    public static Schema.Type getScriptFunctionReturnType(MethodField field, Schema.Type resolvedType) {
        String functionName = ((ScriptMethodField) field).getFunctionName().toLowerCase();
        if (functionName.equals("cast")) {
            String castType = ((SQLCastExpr) field.getExpression()).getDataType().getName();
            return getCastFunctionReturnType(castType);
        }
        return resolvedType;
    }

    public static Schema.Type getCastFunctionReturnType(String castType) {
        switch (StringUtils.toUpper(castType)) {
            case "FLOAT":
                return Schema.Type.FLOAT;
            case "DOUBLE":
                return Schema.Type.DOUBLE;
            case "INT":
                return Schema.Type.INTEGER;
            case "STRING":
                return Schema.Type.TEXT;
            case "DATETIME":
                return Schema.Type.DATE;
            case "LONG":
                return Schema.Type.LONG;
            default:
                throw new UnsupportedOperationException(
                    StringUtils.format("The following type is not supported by cast(): %s", castType)
                );
        }
    }

    /**
     *
     * @param field
     * @return Schema.Type.TEXT or DOUBLE
     * There are only two ORDER BY types (TEXT, NUMBER) in Elasticsearch, so the Type that is returned here essentially
     * indicates the category of the function as opposed to the actual return type.
     */
    public static Schema.Type getOrderByFieldType(Field field) {
        String functionName = ((ScriptMethodField) field).getFunctionName().toLowerCase();
        if (functionName.equals("cast")) {
            String castType = ((SQLCastExpr) field.getExpression()).getDataType().getName();
            return getCastFunctionReturnType(castType);
        }

        if (numberOperators.contains(functionName) || mathConstants.contains(functionName)
                || trigFunctions.contains(functionName) || binaryOperators.contains(functionName)) {
            return Schema.Type.DOUBLE;
        } else if (dateFunctions.contains(functionName)) {
            if (functionName.equals("date_format") || functionName.equals("now")
                    || functionName.equals("curdate") || functionName.equals("date")
                    || functionName.equals("timestamp") || functionName.equals("monthname")) {
                return Schema.Type.TEXT;
            }
            return Schema.Type.DOUBLE;
        } else if (stringFunctions.contains(functionName) || stringOperators.contains(functionName)) {
            return Schema.Type.TEXT;
        }

        throw new UnsupportedOperationException(
                String.format(
                        "The following method is not supported in Schema for Order By: %s",
                        functionName));
    }
}
