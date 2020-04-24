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

package com.amazon.opendistroforelasticsearch.sql.ppl.parser;

import java.util.Collections;
import org.junit.Test;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.agg;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.aggregate;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.and;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.booleanLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.compare;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.defaultSortArgs;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.defaultStatsArgs;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.doubleLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.equalTo;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.filter;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.function;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.in;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.intLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.not;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.or;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.project;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.relation;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.stringLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.unresolvedAttr;

public class AstExpressionBuilderTest extends AstBuilderTest{

    @Test
    public void testLogicalNotExpr() {
        assertEqual("source=t not a=1",
                filter(
                        relation("t"),
                        not(
                                compare("=", unresolvedAttr("a"), intLiteral(1))
                        )
                ));
    }

    @Test
    public void testLogicalOrExpr() {
        assertEqual("source=t a=1 or b=2",
                filter(
                        relation("t"),
                        or(
                                compare("=", unresolvedAttr("a"), intLiteral(1)),
                                compare("=", unresolvedAttr("b"), intLiteral(2))
                        )
                ));
    }

    @Test
    public void testLogicalAndExpr() {
        assertEqual("source=t a=1 and b=2",
                filter(
                        relation("t"),
                        and(
                                compare("=", unresolvedAttr("a"), intLiteral(1)),
                                compare("=", unresolvedAttr("b"), intLiteral(2))
                        )
                ));
    }

    @Test
    public void testLogicalAndExprWithoutKeywordAnd() {
        assertEqual("source=t a=1 b=2",
                filter(
                        relation("t"),
                        and(
                                compare("=", unresolvedAttr("a"), intLiteral(1)),
                                compare("=", unresolvedAttr("b"), intLiteral(2))
                        )
                ));
    }

    @Test
    public void testEvalExpr() {
        assertEqual("source=t f=abs(a)",
                filter(
                        relation("t"),
                        equalTo(
                                unresolvedAttr("f"),
                                function("abs", unresolvedAttr("a"))
                        )
                ));
    }

    @Test
    public void testCompareExprWhenEqual() {
        assertEqual("source=t a='b'",
                filter(
                        relation("t"),
                        compare("=", unresolvedAttr("a"), stringLiteral("b"))
                ));
    }

    @Test
    public void testInExpr() {
        assertEqual("source=t f in (1, 2, 3)",
                filter(
                        relation("t"),
                        in(
                                unresolvedAttr("f"),
                                intLiteral(1), intLiteral(2), intLiteral(3))
                ));
    }

    @Test
    public void testFieldExpr() {
        assertEqual("source=t | sort +f",
                agg(
                        relation("t"),
                        null,
                        Collections.singletonList(unresolvedAttr("f")),
                        null,
                        defaultSortArgs()
                ));
    }

    @Test
    public void testAggFuncCallExpr() {
        assertEqual("source=t | stats avg(a) by b",
                agg(
                        relation("t"),
                        Collections.singletonList(
                                aggregate("avg", unresolvedAttr("a"))
                        ),
                        null,
                        Collections.singletonList(unresolvedAttr("b")),
                        defaultStatsArgs()
                ));
    }

    @Test
    public void testEvalFuncCallExpr() {
        assertEqual("source=t | eval f=abs(a)",
                project(
                        relation("t"),
                        equalTo(
                                unresolvedAttr("f"),
                                function("abs", unresolvedAttr("a"))
                        )
                ));
    }

    @Test
    public void testStringLiteralExpr() {
        assertEqual("source=t a=\"string\"",
                filter(
                        relation("t"),
                        compare(
                                "=",
                                unresolvedAttr("a"),
                                stringLiteral("string")
                        )
                ));
    }

    @Test
    public void testIntegerLiteralExpr() {
        assertEqual("source=t a=1",
                filter(
                        relation("t"),
                        compare(
                                "=",
                                unresolvedAttr("a"),
                                intLiteral(1)
                        )
                ));
    }

    @Test
    public void testDoubleLiteralExpr() {
        assertEqual("source=t b=0.1",
                filter(
                        relation("t"),
                        compare(
                                "=",
                                unresolvedAttr("b"),
                                doubleLiteral(0.1)
                        )
                ));
    }

    @Test
    public void testBooleanLiteralExpr() {
        assertEqual("source=t a=true",
                filter(
                        relation("t"),
                        compare(
                                "=",
                                unresolvedAttr("a"),
                                booleanLiteral(true)
                        )
                ));
    }

}
