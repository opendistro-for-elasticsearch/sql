package com.amazon.opendistroforelasticsearch.ppl.analysis;

import com.amazon.opendistroforelasticsearch.ppl.plans.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Node;
import com.amazon.opendistroforelasticsearch.ppl.spec.scope.Context;
import com.amazon.opendistroforelasticsearch.sql.antlr.OpenDistroSqlAnalyzer;
import com.amazon.opendistroforelasticsearch.sql.antlr.SqlAnalysisConfig;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.net.URL;

import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.attr;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.equalTo;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.filter;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.intLiteral;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.project;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.relation;
import static com.amazon.opendistroforelasticsearch.ppl.plans.dsl.DSL.unresolvedAttr;
import static com.amazon.opendistroforelasticsearch.sql.util.CheckScriptContents.mockLocalClusterState;
import static org.junit.Assert.assertEquals;

public class AnalyzerTest {

    private static final String TEST_MAPPING_FILE = "mappings/semantics.json";

    /**
     * public accessor is required by @Rule annotation
     */
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Analyzer analyzer;

    @SuppressWarnings("UnstableApiUsage")
    @BeforeClass
    public static void init() throws IOException {
        URL url = Resources.getResource(TEST_MAPPING_FILE);
        String mappings = Resources.toString(url, Charsets.UTF_8);
        LocalClusterState.state(null);
        mockLocalClusterState(mappings);
    }

    @Before
    public void setup() {
        analyzer = new Analyzer(new Context<>(), LocalClusterState.state(), 1000);
    }

    @Test
    public void testSearchCommand() {
        assertEqual(
                project(
                        filter(
                                relation("semantics"),
                                equalTo(unresolvedAttr("age"), intLiteral(1))
                        ),
                        unresolvedAttr("age"), unresolvedAttr("balance")),
                project(
                        filter(
                                relation("semantics"),
                                equalTo(attr("age"), intLiteral(1))
                        ),
                        attr("age"), attr("balance")
                ));
    }

    public void assertEqual(LogicalPlan testPlan, LogicalPlan expectedPlan) {
        assertEquals(analyzer.analyze(testPlan),
                     expectedPlan);
    }
}
