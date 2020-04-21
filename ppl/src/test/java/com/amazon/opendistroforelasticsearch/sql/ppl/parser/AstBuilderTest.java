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

import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.PPLSyntaxParser;
import com.amazon.opendistroforelasticsearch.sql.ppl.node.Node;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static com.amazon.opendistroforelasticsearch.sql.ppl.plans.dsl.DSL.agg;
import static com.amazon.opendistroforelasticsearch.sql.ppl.plans.dsl.DSL.aggregate;
import static com.amazon.opendistroforelasticsearch.sql.ppl.plans.dsl.DSL.booleanLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ppl.plans.dsl.DSL.equalTo;
import static com.amazon.opendistroforelasticsearch.sql.ppl.plans.dsl.DSL.filter;
import static com.amazon.opendistroforelasticsearch.sql.ppl.plans.dsl.DSL.function;
import static com.amazon.opendistroforelasticsearch.sql.ppl.plans.dsl.DSL.intLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ppl.plans.dsl.DSL.map;
import static com.amazon.opendistroforelasticsearch.sql.ppl.plans.dsl.DSL.project;
import static com.amazon.opendistroforelasticsearch.sql.ppl.plans.dsl.DSL.relation;
import static com.amazon.opendistroforelasticsearch.sql.ppl.plans.dsl.DSL.stringLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ppl.plans.dsl.DSL.unresolvedAttr;
import static org.junit.Assert.assertEquals;

public class AstBuilderTest {

    @Test
    public void testSearchCommand() {
        assertEqual("search source=t a=1",
                filter(
                        relation("t"),
                        equalTo(unresolvedAttr("a"), intLiteral(1))
                )
        );
    }

    @Test
    public void testSearchCommandString() {
        assertEqual("search source=t a=\"a\"",
                filter(
                        relation("t"),
                        equalTo(unresolvedAttr("a"), stringLiteral("a"))
                )
        );
    }

    @Test
    public void testSearchCommandWithoutSearch() {
        assertEqual("source=t a=1",
                filter(
                        relation("t"),
                        equalTo(unresolvedAttr("a"), intLiteral(1))
                )
        );
    }

    @Test
    public void testWhereCommand() {
        assertEqual("search source=t | where a=1",
                filter(
                        relation("t"),
                        equalTo(unresolvedAttr("a"), intLiteral(1))
                )
        );
    }

    @Test
    public void testFieldsCommand() {
        assertEqual("source=t | fields f, g",
                project(
                        relation("t"),
                        unresolvedAttr("f"), unresolvedAttr("g")
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
        assertEqual("source=t | stats count(a) by b",
                agg(
                        relation("t"),
                        Collections.singletonList(
                                aggregate(
                                        unresolvedAttr("count"), unresolvedAttr("a")
                                )),
                        null,
                        Collections.singletonList(unresolvedAttr("b"))
                ));
    }

    @Test
    public void testDedupCommand() {
        assertEqual("source=t | dedup f1, f2 sortby f3",
                agg(
                        relation("t"),
                        Arrays.asList(unresolvedAttr("f1"), unresolvedAttr("f2")),
                        Collections.singletonList(unresolvedAttr("f3")),
                        null
                ));
    }

    @Test
    public void testSortCommand() {
        assertEqual("source=t | sort f1, f2",
                agg(
                        relation("t"),
                        null,
                        Arrays.asList(unresolvedAttr("f1"), unresolvedAttr("f2")),
                        null
                ));
    }

    @Test
    public void testEvalCommand() {
        assertEqual("source=t | eval r=abs(f)",
                project(
                        relation("t"),
                        equalTo(
                                unresolvedAttr("r"),
                                function("abs", unresolvedAttr("f"))
                        )
                ));
    }

    @Test
    public void testIndexName() {
        assertEqual("source=\"log.2020.04.20.\" a=1",
                filter(
                        relation("log.2020.04.20."),
                        equalTo(unresolvedAttr("a"), intLiteral(1))
                ));
    }

    protected void assertEqual(String query, Node expectedPlan) {
        Node actualPlan = plan(query);
        assertEquals(expectedPlan, actualPlan);
    }

    private PPLSyntaxParser parser = new PPLSyntaxParser();
    private AstBuilder astBuilder = new AstBuilder(new AstExpressionBuilder());

    private Node plan(String query) {
        return astBuilder.visit(parser.analyzeSyntax(query));
    }
}
