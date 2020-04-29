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
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.argument;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.booleanLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.compare;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.defaultSortArgs;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.defaultSortFieldArgs;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.defaultStatsArgs;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.doubleLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.equalTo;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.field;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.filter;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.function;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.in;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.intLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.map;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.not;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.nullLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.or;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.project;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.relation;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.stringLiteral;

public class AstExpressionBuilderTest extends AstBuilderTest{

    @Test
    public void testLogicalNotExpr() {
        assertEqual("source=t not a=1",
                filter(
                        relation("t"),
                        not(
                                compare("=", field("a"), intLiteral(1))
                        )
                ));
    }

    @Test
    public void testLogicalOrExpr() {
        assertEqual("source=t a=1 or b=2",
                filter(
                        relation("t"),
                        or(
                                compare("=", field("a"), intLiteral(1)),
                                compare("=", field("b"), intLiteral(2))
                        )
                ));
    }

    @Test
    public void testLogicalAndExpr() {
        assertEqual("source=t a=1 and b=2",
                filter(
                        relation("t"),
                        and(
                                compare("=", field("a"), intLiteral(1)),
                                compare("=", field("b"), intLiteral(2))
                        )
                ));
    }

    @Test
    public void testLogicalAndExprWithoutKeywordAnd() {
        assertEqual("source=t a=1 b=2",
                filter(
                        relation("t"),
                        and(
                                compare("=", field("a"), intLiteral(1)),
                                compare("=", field("b"), intLiteral(2))
                        )
                ));
    }

    @Test
    public void testEvalExpr() {
        assertEqual("source=t f=abs(a)",
                filter(
                        relation("t"),
                        equalTo(
                                field("f"),
                                function("abs", field("a"))
                        )
                ));
    }

    @Test
    public void testCompareExpr() {
        assertEqual("source=t a='b'",
                filter(
                        relation("t"),
                        compare("=", field("a"), stringLiteral("b"))
                ));
    }

    @Test
    public void testCompareFieldsExpr() {
        assertEqual("source=t a>b",
                filter(
                        relation("t"),
                        compare(">", field("a"), field("b"))
                ));
    }

    @Test
    public void testInExpr() {
        assertEqual("source=t f in (1, 2, 3)",
                filter(
                        relation("t"),
                        in(
                                field("f"),
                                intLiteral(1), intLiteral(2), intLiteral(3))
                ));
    }

    @Test
    public void testFieldExpr() {
        assertEqual("source=t | sort + f",
                agg(
                        relation("t"),
                        null,
                        Collections.singletonList(field("f", defaultSortFieldArgs())),
                        null,
                        defaultSortArgs()
                ));
    }

    @Test
    public void testSortFieldWithMinusKeyword() {
        assertEqual("source=t | sort - f",
                agg(
                        relation("t"),
                        null,
                        Collections.singletonList(
                                field(
                                        "f",
                                        argument("exclude", booleanLiteral(true)),
                                        argument("type", nullLiteral())
                                )
                        ),
                        null,
                        defaultSortArgs()
                ));
    }

    @Test
    public void testSortFieldWithAutoKeyword() {
        assertEqual("source=t | sort auto(f)",
                agg(
                        relation("t"),
                        null,
                        Collections.singletonList(
                                field(
                                        "f",
                                        argument("exclude", booleanLiteral(false)),
                                        argument("type", stringLiteral("auto"))
                                )
                        ),
                        null,
                        defaultSortArgs()
                ));
    }

    @Test
    public void testSortFieldWithIpKeyword() {
        assertEqual("source=t | sort ip(f)",
                agg(
                        relation("t"),
                        null,
                        Collections.singletonList(
                                field(
                                        "f",
                                        argument("exclude", booleanLiteral(false)),
                                        argument("type", stringLiteral("ip"))
                                )
                        ),
                        null,
                        defaultSortArgs()
                ));
    }

    @Test
    public void testSortFieldWithNumKeyword() {
        assertEqual("source=t | sort num(f)",
                agg(
                        relation("t"),
                        null,
                        Collections.singletonList(
                                field(
                                        "f",
                                        argument("exclude", booleanLiteral(false)),
                                        argument("type", stringLiteral("num"))
                                )
                        ),
                        null,
                        defaultSortArgs()

                ));
    }

    @Test
    public void testSortFieldWithStrKeyword() {
        assertEqual("source=t | sort str(f)",
                agg(
                        relation("t"),
                        null,
                        Collections.singletonList(
                                field(
                                        "f",
                                        argument("exclude", booleanLiteral(false)),
                                        argument("type", stringLiteral("str"))
                                )
                        ),
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
                                map(
                                        aggregate("avg", field("a")),
                                        null
                                )

                        ),
                        null,
                        Collections.singletonList(field("b")),
                        defaultStatsArgs()
                ));
    }

    @Test
    public void testPercentileAggFuncExpr() {
        assertEqual("source=t | stats percentile<1>(a)",
                agg(
                        relation("t"),
                        Collections.singletonList(
                                map(
                                        aggregate(
                                                "percentile",
                                                field("a"),
                                                argument("rank", intLiteral(1))
                                        ),
                                        null
                                )
                        ),
                        null,
                        null,
                        defaultStatsArgs()
                ));
    }

    @Test
    public void testEvalFuncCallExpr() {
        assertEqual("source=t | eval f=abs(a)",
                project(
                        relation("t"),
                        equalTo(
                                field("f"),
                                function("abs", field("a"))
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
                                field("a"),
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
                                field("a"),
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
                                field("b"),
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
                                field("a"),
                                booleanLiteral(true)
                        )
                ));
    }

}
