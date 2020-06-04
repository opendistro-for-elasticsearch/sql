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

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr.visitor;

import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.parser.OpenDistroSqlParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlFeatureNotImplementedException;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils;
import com.google.common.collect.Sets;

import java.util.Set;

import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.parser.OpenDistroSqlParser.MathOperatorContext;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.parser.OpenDistroSqlParser.RegexpPredicateContext;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.parser.OpenDistroSqlParser.ScalarFunctionCallContext;

public class UnsupportedSemanticVerifier {

    private static final Set<String> mathConstants = Sets.newHashSet(
            "e", "pi"
    );

    private static final Set<String> supportedNestedFunctions = Sets.newHashSet(
            "nested", "reverse_nested", "score", "match_query", "matchquery"
    );

    /**
     * The following two sets include the functions and operators that have requested or issued by users
     * but the plugin does not support yet.
     */
    private static final Set<String> unsupportedFunctions = Sets.newHashSet(
            "adddate", "addtime", "datetime", "greatest", "least"
    );

    private static final Set<String> unsupportedOperators = Sets.newHashSet(
            "div"
    );


    /**
     * The scalar function calls are separated into (a)typical function calls; (b)nested function calls with functions
     * as arguments, like abs(log(...)); (c)aggregations with functions as aggregators, like max(abs(....)).
     * Currently, we do not support nested functions or nested aggregations, aka (b) and (c).
     * However, for the special EsFunctions included in the [supportedNestedFunctions] set, we have supported them in
     * nested function calls and aggregations (b&c). Besides, the math constants included in the [mathConstants] set
     * are regraded as scalar functions, but they are working well in the painless script.
     *
     * Thus, the types of functions to throw exceptions:
     * (I)case (b) except that the arguments are from the [mathConstants] set;
     * (II) case (b) except that the arguments are from the [supportedNestedFunctions] set;
     * (III) case (c) except that the aggregators are from thet [supportedNestedFunctions] set.
     */
    public static void verify(ScalarFunctionCallContext ctx) {
        String funcName = StringUtils.toLower(ctx.scalarFunctionName().getText());

        // type (III)
        if (ctx.parent.parent instanceof OpenDistroSqlParser.FunctionAsAggregatorFunctionContext
                && !(supportedNestedFunctions.contains(StringUtils.toLower(funcName)))) {
            throw new SqlFeatureNotImplementedException(StringUtils.format(
                    "Aggregation calls with function aggregator like [%s] are not supported yet",
                    ctx.parent.parent.getText()));

            // type (I) and (II)
        } else if (ctx.parent.parent instanceof OpenDistroSqlParser.NestedFunctionArgsContext
                && !(mathConstants.contains(funcName) || supportedNestedFunctions.contains(funcName))) {
            throw new SqlFeatureNotImplementedException(StringUtils.format(
                    "Nested function calls like [%s] are not supported yet", ctx.parent.parent.parent.getText()));

            // unsupported functions
        } else if (unsupportedFunctions.contains(funcName)) {
            throw new SqlFeatureNotImplementedException(StringUtils.format("Function [%s] is not supported yet",
                    funcName));
        }
    }

    public static void verify(MathOperatorContext ctx) {
        if (unsupportedOperators.contains(StringUtils.toLower(ctx.getText()))) {
            throw new SqlFeatureNotImplementedException(StringUtils.format("Operator [%s] is not supported yet",
                    ctx.getText()));
        }
    }

    public static void verify(RegexpPredicateContext ctx) {
        throw new SqlFeatureNotImplementedException("Regexp predicate is not supported yet");
    }
}
