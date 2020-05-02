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

import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.PPLSyntaxParser;
import com.amazon.opendistroforelasticsearch.sql.ast.Node;
import org.junit.Test;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.agg;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.aggregate;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.compare;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.defaultDedupArgs;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.defaultFieldsArgs;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.defaultSortArgs;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.defaultSortFieldArgs;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.defaultStatsArgs;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.equalTo;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.exprList;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.field;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.filter;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.function;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.intLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.map;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.project;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.projectWithArg;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.relation;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.stringLiteral;
import static org.junit.Assert.assertEquals;

public class AstBuilderTest {

    @Test
    public void testSearchCommand() {
        assertEqual("search source=t a=1",
                filter(
                        relation("t"),
                        compare("=", field("a"), intLiteral(1))
                )
        );
    }

    @Test
    public void testSearchCommandString() {
        assertEqual("search source=t a=\"a\"",
                filter(
                        relation("t"),
                        compare("=", field("a"), stringLiteral("a"))
                )
        );
    }

    @Test
    public void testSearchCommandWithoutSearch() {
        assertEqual("source=t a=1",
                filter(
                        relation("t"),
                        compare("=", field("a"), intLiteral(1))
                )
        );
    }

    @Test
    public void testSearchCommandWithFilterBeforeSource() {
        assertEqual("search a=1 source=t",
                filter(
                        relation("t"),
                        compare("=", field("a"), intLiteral(1))
                ));
    }

    @Test
    public void testWhereCommand() {
        assertEqual("search source=t | where a=1",
                filter(
                        relation("t"),
                        compare("=", field("a"), intLiteral(1))
                )
        );
    }

    @Test
    public void testFieldsCommand() {
        assertEqual("source=t | fields f, g",
                projectWithArg(
                        relation("t"),
                        defaultFieldsArgs(),
                        field("f"), field("g")
                ));
    }

    @Test
    public void testRenameCommand() {
        assertEqual("source=t | rename f as g",
                project(
                        relation("t"),
                        map("f", "g")
                ));
    }

    @Test
    public void testStatsCommand() {
        assertEqual("source=t | stats count(a)",
                agg(
                        relation("t"),
                        exprList(
                                map(aggregate("count", field("a")), null)
                        ),
                        null,
                        null,
                        defaultStatsArgs()
                ));
    }

    @Test
    public void testStatsCommandWithByClause() {
        assertEqual("source=t | stats count(a) by b DEDUP_SPLITVALUES=false",
                agg(
                        relation("t"),
                        exprList(
                                map(aggregate("count", field("a")), null)
                        ),
                        null,
                        exprList(field("b")),
                        defaultStatsArgs()
                ));
    }


    @Test
    public void testStatsCommandWithAlias() {
        assertEqual("source=t | stats count(a) as alias",
                agg(
                        relation("t"),
                        exprList(
                                map(
                                        aggregate("count", field("a")),
                                        field("alias")
                                )
                        ),
                        null,
                        null,
                        defaultStatsArgs()
                ));
    }

    @Test
    public void testDedupCommand() {
        assertEqual("source=t | dedup f1, f2",
                agg(
                        relation("t"),
                        exprList(field("f1"), field("f2")),
                        null,
                        null,
                        defaultDedupArgs()
                ));
    }

    @Test
    public void testDedupCommandWithSortby() {
        assertEqual("source=t | dedup f1, f2 sortby f3",
                agg(
                        relation("t"),
                        exprList(field("f1"), field("f2")),
                        exprList(field("f3", defaultSortFieldArgs())),
                        null,
                        defaultDedupArgs()
                ));
    }

    @Test
    public void testSortCommand() {
        assertEqual("source=t | sort f1, f2",
                agg(
                        relation("t"),
                        null,
                        exprList(
                                field("f1", defaultSortFieldArgs()),
                                field("f2", defaultSortFieldArgs())
                        ),
                        null,
                        defaultSortArgs()
                ));
    }

    @Test
    public void testEvalCommand() {
        assertEqual("source=t | eval r=abs(f)",
                project(
                        relation("t"),
                        equalTo(
                                field("r"),
                                function("abs", field("f"))
                        )
                ));
    }

    @Test
    public void testIndexName() {
        assertEqual("source=`log.2020.04.20.` a=1",
                filter(
                        relation("log.2020.04.20."),
                        compare("=", field("a"), intLiteral(1))
                ));
    }

    protected void assertEqual(String query, Node expectedPlan) {
        Node actualPlan = plan(query);
        assertEquals(expectedPlan, actualPlan);
    }

    protected void assertEqual(String query, String expected) {
        Node expectedPlan = plan(expected);
        assertEqual(query, expectedPlan);
    }

    private PPLSyntaxParser parser = new PPLSyntaxParser();
    private AstBuilder astBuilder = new AstBuilder(new AstExpressionBuilder());

    private Node plan(String query) {
        return astBuilder.visit(parser.analyzeSyntax(query));
    }
}
