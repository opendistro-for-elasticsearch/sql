package com.amazon.opendistroforelasticsearch.ppl.parser;

import com.amazon.opendistroforelasticsearch.ppl.antlr.PPLSyntaxParser;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Node;
import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.equalTo;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.filter;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.intLiteral;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.project;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.relation;
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
    public void testSearchCommandWithouSearch() {
        assertEqual("source=t a=1",
                    filter(
                            relation("t"),
                            equalTo(unresolvedAttr("a"), intLiteral(1))
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

    public void assertEqual(String sql, Node expectedPlan) {
        Node actualPlan = plan(sql);
        assertEquals(actualPlan, expectedPlan);
    }

    private PPLSyntaxParser parser = new PPLSyntaxParser();
    private AstBuilder astBuilder = new AstBuilder(new AstExpressionBuilder());

    public Node plan(String sql) {
        return astBuilder.visit(parser.analyzeSyntax(sql));
    }
}
