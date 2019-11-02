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

package com.amazon.opendistroforelasticsearch.sql.utils;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumericLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.amazon.opendistroforelasticsearch.sql.domain.KVValue;
import com.amazon.opendistroforelasticsearch.sql.executor.format.Schema;
import com.google.common.base.Joiner;
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

/**
 * Created by allwefantasy on 8/19/16.
 */
public class SQLFunctions {

    private static final Set<String> numberOperators = Sets.newHashSet(
            "exp", "expm1", "log", "log2", "log10", "ln", "sqrt", "cbrt", "ceil", "floor", "rint", "pow", "power",
            "round", "random", "abs", "sign", "signum"
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
            "day_of_week", "hour_of_day", "minute_of_day", "minute_of_hour", "second_of_minute"
    );

    private static final Set<String> utilityFunctions = Sets.newHashSet("field", "assign");

    public static final Set<String> builtInFunctions = Stream.of(
            numberOperators,
            mathConstants,
            trigFunctions,
            stringOperators,
            stringFunctions,
            binaryOperators,
            dateFunctions,
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
                                                 boolean returnValue) {
        Tuple<String, String> functionStr = null;
        switch (methodName.toLowerCase()) {
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
                functionStr = dateFunctionTemplate("monthOfYear", (SQLExpr) paramers.get(0).value);
                break;
            case "week_of_year":
                functionStr = dateFunctionTemplate("weekOfWeekyear", (SQLExpr) paramers.get(0).value);
                break;
            case "day_of_year":
                functionStr = dateFunctionTemplate("dayOfYear", (SQLExpr) paramers.get(0).value);
                break;
            case "day_of_month":
                functionStr = dateFunctionTemplate("dayOfMonth", (SQLExpr) paramers.get(0).value);
                break;
            case "day_of_week":
                functionStr = dateFunctionTemplate("dayOfWeek", (SQLExpr) paramers.get(0).value);
                break;
            case "hour_of_day":
                functionStr = dateFunctionTemplate("hourOfDay", (SQLExpr) paramers.get(0).value);
                break;
            case "minute_of_day":
                functionStr = dateFunctionTemplate("minuteOfDay", (SQLExpr) paramers.get(0).value);
                break;
            case "minute_of_hour":
                functionStr = dateFunctionTemplate("minuteOfHour", (SQLExpr) paramers.get(0).value);
                break;
            case "second_of_minute":
                functionStr = dateFunctionTemplate("secondOfMinute", (SQLExpr) paramers.get(0).value);
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
                        Integer.parseInt(Util.expr2Object((SQLExpr) paramers.get(2).value).toString())
                        , name);
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
                functionStr = log(SQLUtils.toSQLExpr("10"), (SQLExpr) paramers.get(0).value, name);
                break;
            case "log":
                if (paramers.size() > 1) {
                    functionStr = log((SQLExpr) paramers.get(0).value, (SQLExpr) paramers.get(1).value, name);
                } else {
                    functionStr = log((SQLUtils.toSQLExpr("Math.E")), (SQLExpr) paramers.get(0).value, name);
                }
                break;
            case "ln":
                functionStr = log(SQLUtils.toSQLExpr("Math.E"), (SQLExpr) paramers.get(0).value, name);
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

    public Tuple<String, String> upper(SQLExpr field, String locale, String valueName) {
        String name = nextId("upper");

        if (valueName == null) {
            return new Tuple<>(name, def(name, upper(getPropertyOrValue(field), locale)));
        } else {
            return new Tuple<>(name, getPropertyOrValue(field) + "; "
                    + def(name, valueName + "." + upper(getPropertyOrValue(field), locale)));
        }
    }

    public Tuple<String, String> lower(SQLExpr field, String locale, String valueName) {
        String name = nextId("lower");

        if (valueName == null) {
            return new Tuple<>(name, def(name, lower(getPropertyOrValue(field), locale)));
        } else {
            return new Tuple<>(name, getPropertyOrValue(field) + "; "
                    + def(name, valueName + "." + lower(getPropertyOrValue(field), locale)));
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
                    + (zoneId != null ? "ZoneId.of('" + zoneId + "')" : "ZoneId.systemDefault()")
                    + ").format(Instant.ofEpochMilli(" + getPropertyOrValue(field) + ".getMillis()))");
        } else {
            return new Tuple<>(name, exprString(field) + "; "
                    + "def " + name + " = new SimpleDateFormat('" + pattern + "').format("
                    + "new Date(" + valueName + " - 8*1000*60*60))");
        }
    }

    private Tuple<String, String> dateFunctionTemplate(String methodName, SQLExpr field) {
        String name = nextId(methodName);
        return new Tuple<>(name, def(name, doc(field) + ".value." + methodName));
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

    public Tuple<String, String> trim(SQLExpr field, String valueName) {
        return strSingleValueTemplate("trim", field, valueName);
    }

    private Tuple<String, String> degrees(SQLExpr field, String valueName) {
        return mathSingleValueTemplate("Math.toDegrees", "degrees", field, valueName);
    }

    private Tuple<String, String> radians(SQLExpr field, String valueName) {
        return mathSingleValueTemplate("Math.toRadians", "radians", field, valueName);
    }

    private Tuple<String, String> mathDoubleValueTemplate(String methodName, String fieldName, SQLExpr val1,
                                                                 String val2, String valueName) {
        String name = nextId(fieldName);
        if (valueName == null) {
            return new Tuple<>(name, def(name, func(methodName, false, getPropertyOrValue(val1), val2)));
        } else {
            return new Tuple<>(name, getPropertyOrValue(val1) + "; "
                    + def(name, func(methodName, false, valueName, val2)));
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
            return new Tuple<>(name, def(name, getPropertyOrValue(field) + "." + func(methodName, false)));
        } else {
            return new Tuple<>(name, getPropertyOrValue(field) + "; "
                    + def(name, valueName + "." + func(methodName, false)));
        }

    }

    // query: substring(Column expr, int pos, int len)
    // painless script: substring(int begin, int end)
    // es behavior: 1-index, supports out-of-bound index
    public Tuple<String, String> substring(SQLExpr field, int pos, int len, String valueName) {
        String name = nextId("substring");

        // start and end are 0-indexes
        int start = pos < 1 ? 0 : pos - 1;
        int end = Math.min(start + len, getPropertyOrValue(field).length());
        if (valueName == null) {
            return new Tuple<>(name, def(name, getPropertyOrStringValue(field) + "."
                    + func("substring", false,
                    Integer.toString(start), Integer.toString(end))));
        } else {
            return new Tuple<>(name, getPropertyOrStringValue(field) + "; "
                    + def(name, valueName + "."
                    + func("substring", false,
                    Integer.toString(start), Integer.toString(end))));
        }
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

    /**
     * Returns return type of script function. This is simple approach, that might be not the best solution in the long
     * term. For example - for JDBC, if the column type in index is INTEGER, and the query is "select column+5", current
     * approach will return type of result column as DOUBLE, although there is enough information to understand that
     * it might be safely treated as INTEGER.
     */
    public static Schema.Type getScriptFunctionReturnType(String functionName) {
        functionName = functionName.toLowerCase();

        if (dateFunctions.contains(functionName) || stringOperators.contains(functionName)) {
            return Schema.Type.TEXT;
        }

        if (mathConstants.contains(functionName) || numberOperators.contains(functionName)
                || trigFunctions.contains(functionName) || binaryOperators.contains(functionName)
                || utilityFunctions.contains(functionName)) {
            return Schema.Type.DOUBLE;
        }

        if (stringFunctions.contains(functionName)) {
            return Schema.Type.INTEGER;
        }

        throw new UnsupportedOperationException(
                String.format(
                        "The following method is not supported in Schema: %s",
                        functionName));
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
}
