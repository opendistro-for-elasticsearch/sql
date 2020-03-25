package com.amazon.opendistroforelasticsearch.ppl.parser;

import com.amazon.opendistroforelasticsearch.ppl.antlr.PPLSyntaxParser;
import com.amazon.opendistroforelasticsearch.ppl.node.Node;
import org.junit.Test;

import java.util.Arrays;

import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.agg;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.and;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.count;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.equalTo;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.filter;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.intLiteral;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.or;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.project;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.relation;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.stringLiteral;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.top;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.unresolvedAttr;
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
    public void testSearchCommandLogicalExpression() {
        assertEqual("search source=t a=1 b=2",
                    filter(
                            relation("t"),
                            and(equalTo(unresolvedAttr("a"), intLiteral(1)),
                                equalTo(unresolvedAttr("b"), intLiteral(2)))
                    )
        );

        assertEqual("search source=t a=1 AND b=2",
                    filter(
                            relation("t"),
                            and(equalTo(unresolvedAttr("a"), intLiteral(1)),
                                equalTo(unresolvedAttr("b"), intLiteral(2)))
                    )
        );

        assertEqual("search source=t a=1 OR b=2",
                    filter(
                            relation("t"),
                            or(equalTo(unresolvedAttr("a"), intLiteral(1)),
                                equalTo(unresolvedAttr("b"), intLiteral(2)))
                    )
        );
    }

    @Test
    public void testSearchAndFields() {
        assertEqual("search source=t a=1 | fields a,b",
                    project(
                            filter(
                                    relation("t"),
                                    equalTo(unresolvedAttr("a"), intLiteral(1))
                            ),
                            unresolvedAttr("a"), unresolvedAttr("b")
                    )
        );
    }

    @Test
    public void testStats() {
        assertEqual("search source=t a=1 | stats count(a) by b",
                    agg(
                            filter(
                                    relation("t"),
                                    equalTo(unresolvedAttr("a"), intLiteral(1))
                            ),
                            Arrays.asList(unresolvedAttr("b")),
                            Arrays.asList(count(unresolvedAttr("a")))
                    )
        );
    }

    @Test
    public void testTop() {
        assertEqual("search source=t a=1 | top 1 a by b",
                    top(
                            filter(
                                    relation("t"),
                                    equalTo(unresolvedAttr("a"), intLiteral(1))
                            ),
                            intLiteral(1),
                            Arrays.asList(unresolvedAttr("b")),
                            Arrays.asList(unresolvedAttr("a"))
                    )
        );
    }

    public void assertEqual(String sql, Node expectedPlan) {
        Node actualPlan = plan(sql);
        assertEquals(expectedPlan, actualPlan);
    }

    private PPLSyntaxParser parser = new PPLSyntaxParser();
    private AstBuilder astBuilder = new AstBuilder(new AstExpressionBuilder());

    public Node plan(String sql) {
        return astBuilder.visit(parser.analyzeSyntax(sql));
    }
}
