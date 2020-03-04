package com.amazon.opendistroforelasticsearch.ppl.parser;

import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Node;
import com.amazon.opendistroforelasticsearch.sql.antlr.OpenDistroSqlAnalyzer;
import com.amazon.opendistroforelasticsearch.sql.antlr.SqlAnalysisConfig;
import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.attr;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.equalTo;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.filter;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.intLiteral;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.project;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.relation;
import static org.junit.Assert.assertEquals;

public class AstBuilderTest {

//    @Test
//    public void testSearchCommand() {
//        Node plan = plan("search index=t AND a=1");
//        System.out.println("done");
//    }


//    @Test
//    public void testSFW() {
//        assertEqual("SELECT a FROM t WHERE a = 1",
//                    project(
//                            filter(
//                                    relation("t"),
//                                    equalTo(attr("a"), intLiteral(1))
//                            ),
//                            attr("a")
//                    )
//        );
//    }

//    public void assertEqual(String sql, Node expectedPlan) {
//        Node actualPlan = plan(sql);
//        assertEquals(actualPlan, expectedPlan);
//    }
//
//    private OpenDistroSqlAnalyzer analyzer = new OpenDistroSqlAnalyzer(new SqlAnalysisConfig(true, true, 1000));
//    private AstBuilder astBuilder = new AstBuilder();
//
//    public Node plan(String sql) {
//        return astBuilder.visit(analyzer.analyzeSyntax(sql));
//    }
}
