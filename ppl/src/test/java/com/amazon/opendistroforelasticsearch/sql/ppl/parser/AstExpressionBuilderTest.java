/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.agg;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.aggregate;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.alias;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.and;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.argument;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.booleanLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.compare;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.defaultFieldsArgs;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.defaultSortFieldArgs;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.defaultStatsArgs;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.doubleLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.equalTo;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.eval;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.exprList;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.field;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.filter;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.function;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.in;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.intLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.intervalLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.let;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.not;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.nullLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.or;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.projectWithArg;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.qualifiedName;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.relation;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.sort;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.stringLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.xor;
import static java.util.Collections.emptyList;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.AllFields;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.DataType;
import org.junit.Ignore;
import org.junit.Test;

public class AstExpressionBuilderTest extends AstBuilderTest {

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
  public void testLogicalXorExpr() {
    assertEqual("source=t a=1 xor b=2",
        filter(
            relation("t"),
            xor(
                compare("=", field("a"), intLiteral(1)),
                compare("=", field("b"), intLiteral(2))
            )
        ));
  }

  @Test
  public void testLogicalLikeExpr() {
    assertEqual("source=t like(a, '_a%b%c_d_')",
        filter(
            relation("t"),
            function("like", field("a"), stringLiteral("_a%b%c_d_"))
        ));
  }

  @Test
  public void testBooleanIsNullFunction() {
    assertEqual("source=t isnull(a)",
        filter(
            relation("t"),
            function("is null", field("a"))
        ));
  }

  @Test
  public void testBooleanIsNotNullFunction() {
    assertEqual("source=t isnotnull(a)",
        filter(
            relation("t"),
            function("is not null", field("a"))
        ));
  }

  /**
   * Todo. search operator should not include functionCall, need to change antlr.
   */
  @Ignore("search operator should not include functionCall, need to change antlr")
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
  public void testEvalFunctionExpr() {
    assertEqual("source=t | eval f=abs(a)",
        eval(
            relation("t"),
            let(
                field("f"),
                function("abs", field("a"))
            )
        ));
  }

  @Test
  public void testEvalBinaryOperationExpr() {
    assertEqual("source=t | eval f=a+b",
        eval(
            relation("t"),
            let(
                field("f"),
                function("+", field("a"), field("b"))
            )
        ));
    assertEqual("source=t | eval f=(a+b)",
        eval(
            relation("t"),
            let(
                field("f"),
                function("+", field("a"), field("b"))
            )
        ));
  }

  @Test
  public void testLiteralValueBinaryOperationExpr() {
    assertEqual("source=t | eval f=3+2",
        eval(
            relation("t"),
            let(
                field("f"),
                function("+", intLiteral(3), intLiteral(2))
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
        sort(
            relation("t"),
            field("f", defaultSortFieldArgs())
        ));
  }

  @Test
  public void testSortFieldWithMinusKeyword() {
    assertEqual("source=t | sort - f",
        sort(
            relation("t"),
            field(
                "f",
                argument("asc", booleanLiteral(false)),
                argument("type", nullLiteral())
            )
        ));
  }

  @Test
  public void testSortFieldWithAutoKeyword() {
    assertEqual("source=t | sort auto(f)",
        sort(
            relation("t"),
            field(
                "f",
                argument("asc", booleanLiteral(true)),
                argument("type", stringLiteral("auto"))
            )
        ));
  }

  @Test
  public void testSortFieldWithIpKeyword() {
    assertEqual("source=t | sort ip(f)",
        sort(
            relation("t"),
            field(
                "f",
                argument("asc", booleanLiteral(true)),
                argument("type", stringLiteral("ip"))
            )
        ));
  }

  @Test
  public void testSortFieldWithNumKeyword() {
    assertEqual("source=t | sort num(f)",
        sort(
            relation("t"),
            field(
                "f",
                argument("asc", booleanLiteral(true)),
                argument("type", stringLiteral("num"))
            )
        ));
  }

  @Test
  public void testSortFieldWithStrKeyword() {
    assertEqual("source=t | sort str(f)",
        sort(
            relation("t"),
            field(
                "f",
                argument("asc", booleanLiteral(true)),
                argument("type", stringLiteral("str"))
            )
        ));
  }

  @Test
  public void testAggFuncCallExpr() {
    assertEqual("source=t | stats avg(a) by b",
        agg(
            relation("t"),
            exprList(
                alias(
                    "avg(a)",
                    aggregate("avg", field("a"))
                )
            ),
            emptyList(),
            exprList(
                alias(
                    "b",
                    field("b")
                )),
            defaultStatsArgs()
        ));
  }

  @Test
  public void testPercentileAggFuncExpr() {
    assertEqual("source=t | stats percentile<1>(a)",
        agg(
            relation("t"),
            exprList(
                alias("percentile<1>(a)",
                    aggregate(
                        "percentile",
                        field("a"),
                        argument("rank", intLiteral(1))
                    )
                )
            ),
            emptyList(),
            emptyList(),
            defaultStatsArgs()
        ));
  }

  @Test
  public void testCountFuncCallExpr() {
    assertEqual("source=t | stats count() by b",
        agg(
            relation("t"),
            exprList(
                alias(
                    "count()",
                    aggregate("count", AllFields.of())
                )
            ),
            emptyList(),
            exprList(
                alias(
                    "b",
                    field("b")
                )),
            defaultStatsArgs()
        ));
  }

  @Test
  public void testEvalFuncCallExpr() {
    assertEqual("source=t | eval f=abs(a)",
        eval(
            relation("t"),
            let(
                field("f"),
                function("abs", field("a"))
            )
        ));
  }

  @Test
  public void testNestedFieldName() {
    assertEqual("source=t | fields field0.field1.field2",
        projectWithArg(
            relation("t"),
            defaultFieldsArgs(),
            field(
                qualifiedName("field0", "field1", "field2")
            )
        ));
  }

  @Test
  public void testFieldNameWithSpecialChars() {
    assertEqual("source=t | fields `field-0`.`field#1`.`field*2`",
        projectWithArg(
            relation("t"),
            defaultFieldsArgs(),
            field(
                qualifiedName("field-0", "field#1", "field*2")
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

  @Test
  public void testIntervalLiteralExpr() {
    assertEqual(
        "source=t a = interval 1 day",
        filter(
            relation("t"),
            compare(
                "=",
                field("a"),
                intervalLiteral(1, DataType.INTEGER, "day")
            )
        ));
  }

  @Test
  public void testKeywordsAsIdentifiers() {
    assertEqual(
        "source=timestamp",
        relation("timestamp")
    );

    assertEqual(
        "source=t | fields timestamp",
        projectWithArg(
            relation("t"),
            defaultFieldsArgs(),
            field("timestamp")
        )
    );
  }

}
