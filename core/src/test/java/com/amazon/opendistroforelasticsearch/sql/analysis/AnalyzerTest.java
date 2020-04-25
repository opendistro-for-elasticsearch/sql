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

import com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL;
import org.junit.jupiter.api.Test;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalyzerTest extends AnalyzerTestBase {

    @Test
    public void filter_relation() {
        assertAnalyzeEqual(
                LogicalPlanDSL.filter(
                        dsl.equal(typeEnv(), DSL.ref("integer_value"), DSL.literal(integerValue(1))),
                        LogicalPlanDSL.relation("schema")
                ),
                AstDSL.filter(
                        AstDSL.relation("schema"),
                        AstDSL.equalTo(AstDSL.unresolvedAttr("integer_value"), AstDSL.intLiteral(1))
                )
        );
    }

    protected void assertAnalyzeEqual(LogicalPlan expected, UnresolvedPlan unresolvedPlan) {
        assertEquals(expected, analyze(unresolvedPlan));
    }

    protected LogicalPlan analyze(UnresolvedPlan unresolvedPlan) {
        return analyzer.analyze(unresolvedPlan, analysisContext);
    }
}