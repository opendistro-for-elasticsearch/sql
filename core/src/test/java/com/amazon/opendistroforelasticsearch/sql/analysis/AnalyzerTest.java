/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.analysis;

import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.argument;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.booleanLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.field;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class AnalyzerTest extends AnalyzerTestBase {
    @Test
    public void filter_relation() {
        assertAnalyzeEqual(
                LogicalPlanDSL.filter(
                        LogicalPlanDSL.relation("schema"),
                        dsl.equal(typeEnv, DSL.ref("integer_value"), DSL.literal(integerValue(1)))
                ),
                AstDSL.filter(
                        AstDSL.relation("schema"),
                        AstDSL.equalTo(AstDSL.field("integer_value"), AstDSL.intLiteral(1))
                )
        );
    }

    @Test
    public void rename_relation() {
        assertAnalyzeEqual(
                LogicalPlanDSL.rename(
                        LogicalPlanDSL.relation("schema"),
                        ImmutableMap.of(DSL.ref("integer_value"), DSL.ref("ivalue"))
                ),
                AstDSL.rename(
                        AstDSL.relation("schema"),
                        AstDSL.map(AstDSL.field("integer_value"), AstDSL.field("ivalue"))
                )
        );
    }


    @Test
    public void rename_stats_source() {
        assertAnalyzeEqual(
                LogicalPlanDSL.rename(
                        LogicalPlanDSL.aggregation(
                                LogicalPlanDSL.relation("schema"),
                                ImmutableList.of(dsl.avg(typeEnv, DSL.ref("integer_value"))),
                                ImmutableList.of()
                        ),
                        ImmutableMap.of(DSL.ref("avg(integer_value)"), DSL.ref("ivalue"))
                ),
                AstDSL.rename(
                        AstDSL.agg(
                                AstDSL.relation("schema"),
                                AstDSL.exprList(
                                        AstDSL.aggregate("avg", field("integer_value"))
                                ),
                                null,
                                ImmutableList.of(),
                                AstDSL.defaultStatsArgs()
                        ),
                        AstDSL.map(AstDSL.aggregate("avg", field("integer_value")), field("ivalue"))
                )
        );
    }

    @Test
    public void stats_source() {
        assertAnalyzeEqual(
                LogicalPlanDSL.aggregation(
                        LogicalPlanDSL.relation("schema"),
                        ImmutableList.of(dsl.avg(typeEnv, DSL.ref("integer_value"))),
                        ImmutableList.of(DSL.ref("string_value"))
                ),
                AstDSL.agg(
                        AstDSL.relation("schema"),
                        AstDSL.exprList(
                                AstDSL.aggregate("avg", field("integer_value"))
                        ),
                        null,
                        ImmutableList.of(field("string_value")),
                        AstDSL.defaultStatsArgs()
                )
        );
    }

    @Test
    public void rename_to_invalid_expression() {
        SemanticCheckException exception = assertThrows(SemanticCheckException.class, () -> analyze(
                AstDSL.rename(
                        AstDSL.agg(
                                AstDSL.relation("schema"),
                                AstDSL.exprList(
                                        AstDSL.aggregate("avg", field("integer_value"))
                                ),
                                null,
                                ImmutableList.of(),
                                AstDSL.defaultStatsArgs()
                        ),
                        AstDSL.map(AstDSL.aggregate("avg", field("integer_value")), AstDSL.aggregate("avg", field("integer_value")))
                )
        ));
        assertEquals("the target expected to be field, but is avg(Field(field=integer_value, fieldArgs=null))", exception.getMessage());
    }

    @Test
    public void project_source() {
        assertAnalyzeEqual(
                LogicalPlanDSL.project(
                        LogicalPlanDSL.relation("schema"),
                        DSL.ref("integer_value"), DSL.ref("double_value")
                ),
                AstDSL.projectWithArg(
                        AstDSL.relation("schema"),
                        AstDSL.defaultFieldsArgs(),
                        AstDSL.field("integer_value"), AstDSL.field("double_value")
                )
        );
    }

    @Test
    public void remove_source() {
        assertAnalyzeEqual(
                LogicalPlanDSL.remove(
                        LogicalPlanDSL.relation("schema"),
                        DSL.ref("integer_value"), DSL.ref("double_value")
                ),
                AstDSL.projectWithArg(
                        AstDSL.relation("schema"),
                        Collections.singletonList(argument("exclude", booleanLiteral(true))),
                        AstDSL.field("integer_value"), AstDSL.field("double_value")
                )
        );
    }

    @Disabled("the project/remove command should shrink the type env")
    @Test
    public void project_source_change_type_env() {
        SemanticCheckException exception = assertThrows(SemanticCheckException.class, () -> analyze(
                AstDSL.projectWithArg(
                        AstDSL.projectWithArg(
                                AstDSL.relation("schema"),
                                AstDSL.defaultFieldsArgs(),
                                AstDSL.field("integer_value"), AstDSL.field("double_value")
                        ),
                        AstDSL.defaultFieldsArgs(),
                        AstDSL.field("float_value")
                )
        ));
    }

    protected void assertAnalyzeEqual(LogicalPlan expected, UnresolvedPlan unresolvedPlan) {
        assertEquals(expected, analyze(unresolvedPlan));
    }

    protected LogicalPlan analyze(UnresolvedPlan unresolvedPlan) {
        return analyzer.analyze(unresolvedPlan, analysisContext);
    }
}