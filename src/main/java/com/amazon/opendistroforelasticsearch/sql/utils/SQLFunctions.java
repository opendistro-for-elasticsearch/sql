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
import com.alibaba.druid.sql.ast.expr.*;
import com.amazon.opendistroforelasticsearch.sql.domain.KVValue;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.elasticsearch.common.collect.Tuple;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by allwefantasy on 8/19/16.
 */
public class SQLFunctions {

    private final static Set<String> numberOperators = Sets.newHashSet(
            "exp", "expm1", "log", "log2", "log10", "sqrt", "cbrt", "ceil", "floor", "rint", "pow",
            "round", "random", "abs"
    );

    private final static Set<String> mathConstants = Sets.newHashSet( "e", "pi");

    private final static Set<String> trigFunctions = Sets.newHashSet(
            "degrees", "radians", "sin", "cos", "tan", "asin", "acos", "atan", "sinh", "cosh"
    );

    private final static Set<String> stringOperators = Sets.newHashSet(
            "split", "concat_ws", "substring", "trim"
    );

    private final static Set<String> binaryOperators = Sets.newHashSet(
            "add", "multiply", "divide", "subtract", "modulus"
    );

    private final static Set<String> dateFunctions = Sets.newHashSet(
            "date_format", "year", "month_of_year", "week_of_year", "day_of_year", "day_of_month",
            "day_of_week", "hour_of_day", "minute_of_day", "minute_of_hour", "second_of_minute"
    );

    private final static Set<String> utilityFunctions = Sets.newHashSet("field", "assign");
    
    public final static Set<String> builtInFunctions = Stream.of(
            numberOperators,
            mathConstants,
            trigFunctions,
            stringOperators,
            binaryOperators,
            dateFunctions,
            utilityFunctions)
            .flatMap(Set::stream).collect(Collectors.toSet());

    public static boolean isBuiltInFunction(String function) {
        return builtInFunctions.contains(function.toLowerCase());
    }

    public static Tuple<String, String> function(String methodName, List<KVValue> paramers, String name, boolean returnValue) {
        Tuple<String, String> functionStr = null;
        switch (methodName) {
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
                functionStr = mathSingleValueTemplate("Math." + methodName, methodName, (SQLExpr) paramers.get(0).value, name);
                break;

            case "pow":
                functionStr = mathDoubleValueTemplate("Math." + methodName, methodName, (SQLExpr) paramers.get(0).value, Util.expr2Object((SQLExpr) paramers.get(1).value).toString(), name);
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
                List<SQLExpr> logs = Lists.newArrayList();
                for (int i = 0; i < paramers.size(); i++) {
                    logs.add((SQLExpr) paramers.get(0).value);
                }
                if (logs.size() > 1) {
                    functionStr = log(logs.get(0), logs.get(1), name);
                } else {
                    functionStr = log(SQLUtils.toSQLExpr("Math.E"), logs.get(0), name);
                }
                break;
            case "assign":
                functionStr = assign((SQLExpr) paramers.get(0).value);
                break;
            default:

        }
        if(returnValue){
            String generatedFieldName = functionStr.v1();
            String returnCommand = ";return " + generatedFieldName +";" ;
            String newScript = functionStr.v2() + returnCommand;
            functionStr = new Tuple<>(generatedFieldName, newScript);
        }
        return functionStr;
    }

    private static String random() {
        return Math.abs(new Random().nextInt()) + "";
    }

    public static String randomize(String methodName) {
        return methodName + "_" + random();
    }

    private static String def(String name, String value) { return "def " + name + " = " + value; }

    private static String doc(SQLExpr field) { return "doc['" + exprString(field) + "']"; }

    private static String doc(String field) { return "doc['" + field + "']"; }

    private static String exprString(SQLExpr expr) { return Util.expr2Object(expr).toString(); }

    private static String func(String methodName, boolean quotes, String... params) {
        if (quotes)
            return methodName + "(" + quoteParams(params) + ")";

        return methodName + "(" + String.join(", ", params) + ")";
    }

    /** Helper method to surround each param with '' (single quotes) for painless script */
    private static String quoteParams(String... params) {
        return Stream.of(params).collect(Collectors.joining("', '", "'", "'"));
    }

    private static Tuple<String, String> concat_ws(String split, List<SQLExpr> columns) {
        String name = randomize("concat_ws");
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
    public static Tuple<String, String> split(SQLExpr field, String pattern, int index, String valueName) {
        String name = randomize("split");
        String script = "";
        if (valueName == null) {
            script = def(name,
                    getPropertyOrValue(field) + "." +
                            func("split", true, pattern) + "[" + index + "]");
        } else {
            script = "; " + def(name, valueName + "." +
                    func("split", true, pattern) + "[" + index + "]");
        }
        return new Tuple<>(name, script);
    }

    //split(Column expr, java.lang.String pattern)
    public static Tuple<String, String> split(SQLExpr field, String pattern, String valueName) {
        String name = randomize("split");
        if (valueName == null) {
            return new Tuple<>(name,
                    def(name, getPropertyOrValue(field) + "." +
                            func("split", true, pattern)));
        } else {
            return new Tuple<>(name, getPropertyOrValue(field) + "; " +
                    def(name, valueName + "." +
                            func("split", true, pattern)));
        }
    }

    private static Tuple<String, String> date_format(SQLExpr field, String pattern, String zoneId, String valueName) {
        String name = randomize("date_format");
        if (valueName == null) {
            return new Tuple<>(name, "def " + name + " = DateTimeFormatter.ofPattern('" + pattern + "').withZone(" +
                    (zoneId != null ? "ZoneId.of('" + zoneId + "')" : "ZoneId.systemDefault()") +
                    ").format(Instant.ofEpochMilli(" + getPropertyOrValue(field) + ".getMillis()))");
        } else {
            return new Tuple<>(name, exprString(field) + "; " +
                    "def " + name + " = new SimpleDateFormat('" + pattern + "').format(" +
                    "new Date(" + valueName + " - 8*1000*60*60))");
        }

    }

    private static Tuple<String, String> dateFunctionTemplate(String methodName, SQLExpr field) {
        String name = randomize(methodName);
        return new Tuple<>(name, def(name, doc(field) + ".value." + methodName));
    }

    public static Tuple<String, String> add(SQLExpr a, SQLExpr b) {
        return binaryOpertator("add", "+", a, b);
    }

    public static Tuple<String, String> assign(SQLExpr a) {
        String name = randomize("assign");
        return new Tuple<>(name,
                        def(name, extractName(a)));
    }

    private static Tuple<String, String> modulus(SQLExpr a, SQLExpr b) {
        return binaryOpertator("modulus", "%", a, b);
    }

    public static Tuple<String, String> field(String a) {
        String name = randomize("field");
        return new Tuple<>(name, def(name, doc(a) + ".value"));
    }

    private static Tuple<String, String> subtract(SQLExpr a, SQLExpr b) {
        return binaryOpertator("subtract", "-", a, b);
    }

    private static Tuple<String, String> multiply(SQLExpr a, SQLExpr b) {
        return binaryOpertator("multiply", "*", a, b);
    }

    private static Tuple<String, String> divide(SQLExpr a, SQLExpr b) {
        return binaryOpertator("divide", "/", a, b);
    }

    private static Tuple<String, String> binaryOpertator(String methodName, String operator, SQLExpr a, SQLExpr b) {
        String name = randomize(methodName);
        return new Tuple<>(name,
                scriptDeclare(a) + scriptDeclare(b) +
                        convertType(a) + convertType(b) +
                        def(name, extractName(a) + " " + operator + " " + extractName(b)));
    }

    private static boolean isProperty(SQLExpr expr) {
        return (expr instanceof SQLIdentifierExpr || expr instanceof SQLPropertyExpr || expr instanceof SQLVariantRefExpr);
    }

    private static String getPropertyOrValue(SQLExpr expr) {
        if (isProperty(expr))
            return doc(expr) + ".value";
        else
            return exprString(expr);
    }

    private static String scriptDeclare(SQLExpr a) {

        if (isProperty(a) || a instanceof SQLNumericLiteralExpr)
            return "";
        else
            return exprString(a) + ";";
    }

    private static String extractName(SQLExpr script) {
        if (isProperty(script)) return doc(script) + ".value";
        String scriptStr = exprString(script);
        String[] variance = scriptStr.split(";");
        String newScript = variance[variance.length - 1];
        if (newScript.trim().startsWith("def ")) {
            //for now ,if variant is string,then change to double.
            return newScript.trim().substring(4).split("=")[0].trim();
        } else return scriptStr;
    }

    //cast(year as int)

    private static String convertType(SQLExpr script) {
        String[] variance = exprString(script).split(";");
        String newScript = variance[variance.length - 1];
        if (newScript.trim().startsWith("def ")) {
            //for now ,if variant is string,then change to double.
            String temp = newScript.trim().substring(4).split("=")[0].trim();

            return " if( " + temp + " instanceof String) " + temp + "= Double.parseDouble(" + temp.trim() + "); ";
        } else return "";


    }

    /**
     * Using exprString() rather than getPropertyOrValue() for "base" since something like "Math.E" gets evaluated
     * incorrectly in getPropertyOrValue(), returning it as a doc value instead of the literal string
     */
    public static Tuple<String, String> log(SQLExpr base, SQLExpr field, String valueName) {
        String name = randomize("log");
        String result;
        if (valueName == null) {
            result = def(name, func("Math.log", false, getPropertyOrValue(field)) +
                    "/" + func("Math.log", false, exprString(base)));
        } else {
            result = getPropertyOrValue(field) + "; " +
                    def(name, func("Math.log", false, valueName) + "/" +
                    func("Math.log", false, exprString(base)));
        }
        return new Tuple<>(name, result);
    }

    public static Tuple<String, String> trim(SQLExpr field, String valueName) {

        return strSingleValueTemplate("trim", field, valueName);

    }

    private static Tuple<String, String> degrees(SQLExpr field, String valueName) {
        return mathSingleValueTemplate("Math.toDegrees", "degrees", field, valueName);
    }

    private static Tuple<String, String> radians(SQLExpr field, String valueName) {
        return mathSingleValueTemplate("Math.toRadians", "radians", field, valueName);
    }

    private static Tuple<String, String> mathDoubleValueTemplate(String methodName, String fieldName, SQLExpr val1, String val2, String valueName) {
        String name = randomize(fieldName);
        if (valueName == null) {
            return new Tuple<>(name, def(name, func(methodName, false, getPropertyOrValue(val1), val2)));
        } else {
            return new Tuple<>(name, getPropertyOrValue(val1) + "; " +
                    def(name, func(methodName, false, valueName, val2)));
        }
    }

    private static Tuple<String, String> mathSingleValueTemplate(String methodName, String fieldName, SQLExpr field, String valueName) {
        String name = randomize(fieldName);
        if (valueName == null) {
            return new Tuple<>(name, def(name, func(methodName, false, getPropertyOrValue(field))));
        } else {
            return new Tuple<>(name, getPropertyOrValue(field) + "; " +
                    def(name, func(methodName, false, valueName)));
        }

    }

    private static Tuple<String, String> mathConstantTemplate(String methodName, String fieldName) {
        String name = randomize(fieldName);
        return new Tuple<>(name, def(name, methodName));
    }

    private static Tuple<String, String> strSingleValueTemplate(String methodName, SQLExpr field, String valueName) {
        String name = randomize(methodName);
        if (valueName == null) {
            return new Tuple<>(name, def(name, getPropertyOrValue(field) + "." + func(methodName, false)));
        } else {
            return new Tuple<>(name, getPropertyOrValue(field) + "; " +
                    def(name, valueName + "." + func(methodName, false)));
        }

    }

    //substring(Column expr, int pos, int len)
    public static Tuple<String, String> substring(SQLExpr field, int pos, int len, String valueName) {
        String name = randomize("substring");
        if (valueName == null) {
            return new Tuple<>(name, def(name, getPropertyOrValue(field) + "." +
                    func("substring", false, Integer.toString(pos), Integer.toString(len))));
        } else {
            return new Tuple<>(name, getPropertyOrValue(field) + "; " +
                    def(name, valueName + "." +
                            func("substring", false, Integer.toString(pos), Integer.toString(len))));
        }

    }

}
