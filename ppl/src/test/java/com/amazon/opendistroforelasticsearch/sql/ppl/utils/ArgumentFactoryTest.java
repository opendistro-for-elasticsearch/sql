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

package com.amazon.opendistroforelasticsearch.sql.ppl.utils;

import com.amazon.opendistroforelasticsearch.sql.ppl.parser.AstBuilderTest;
import org.junit.Test;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.agg;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.aggregate;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.argument;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.booleanLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.exprList;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.intLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.map;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.projectWithArg;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.relation;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.stringLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.DSL.unresolvedAttr;

public class ArgumentFactoryTest extends AstBuilderTest {

    @Test
    public void testFieldsCommandArgument() {
        assertEqual("source=t | fields - a",
                projectWithArg(
                        relation("t"),
                        exprList(argument("exclude", booleanLiteral(true))),
                        unresolvedAttr("a")
                ));
    }

    @Test
    public void testFieldsCommandDefaultArgument() {
        assertEqual("source=t | fields + a", "source=t | fields a");
    }

    @Test
    public void testStatsCommandArgument() {
        assertEqual("source=t | stats partitions=1 allnum=false delim=',' avg(a) dedup_splitvalues=true",
                agg(
                        relation("t"),
                        exprList(map(aggregate("avg", unresolvedAttr("a")), null)),
                        null,
                        null,
                        exprList(
                                argument("partitions", intLiteral(1)),
                                argument("allnum", booleanLiteral(false)),
                                argument("delim", stringLiteral(",")),
                                argument("dedupsplit", booleanLiteral(true))
                        )
                ));
    }

    @Test
    public void testStatsCommandDefaultArgument() {
        assertEqual(
                "source=t | stats partitions=1 allnum=false delim=' ' avg(a) dedup_splitvalues=false",
                "source=t | stats avg(a)");
    }

    @Test
    public void testDedupCommandArgument() {
        assertEqual("source=t | dedup 3 field0 keepevents=true keepempty=false consecutive=true",
                agg(
                        relation("t"),
                        exprList(unresolvedAttr("field0")),
                        null,
                        null,
                        exprList(
                                argument("number", intLiteral(3)),
                                argument("keepevents", booleanLiteral(true)),
                                argument("keepempty", booleanLiteral(false)),
                                argument("consecutive", booleanLiteral(true))
                        )
                ));
    }

    @Test
    public void testDedupCommandDefaultArgument() {
        assertEqual(
                "source=t | dedup 1 field0 keepevents=false keepempty=false consecutive=false",
                "source=t | dedup field0"
        );
    }

    @Test
    public void testSortCommandArgument() {
        assertEqual("source=t | sort 3 field0 desc",
                agg(
                        relation("t"),
                        null,
                        exprList(unresolvedAttr("field0")),
                        null,
                        exprList(
                                argument("count", intLiteral(3)),
                                argument("desc", booleanLiteral(true))
                        )
                ));
        assertEqual("source=t | sort 3 field0 d", "source=t | sort 3 field0 desc");
    }

    @Test
    public void testSortCommandDefaultArgument() {
        assertEqual(
                "source=t | sort 1000 field0",
                "source=t | sort field0"
        );
    }

    @Test
    public void testNoArgConstructorForArgumentFactoryShouldPass() {
        new ArgumentFactory();
    }

}
