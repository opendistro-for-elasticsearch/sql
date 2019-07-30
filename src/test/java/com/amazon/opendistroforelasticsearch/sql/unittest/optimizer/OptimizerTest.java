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

package com.amazon.opendistroforelasticsearch.sql.unittest.optimizer;

import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.optimizer.OptimizeRule;
import com.amazon.opendistroforelasticsearch.sql.optimizer.Optimizer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLFeatureNotSupportedException;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OptimizerTest {
    @Mock
    private OptimizeRule<SQLQueryExpr> optimizeRule;
    @Mock
    private SQLQueryExpr expr;

    private Optimizer<SQLQueryExpr> optimizer;

    @Before
    public void setup() {
        optimizer = Optimizer.<SQLQueryExpr>builder().withRule(optimizeRule).build();
    }

    @Test
    public void optimize() throws SQLFeatureNotSupportedException {
        when(optimizeRule.match(expr)).thenReturn(true);

        optimizer.optimize(expr);
        verify(optimizeRule, times(1)).optimize(expr);
    }

    @Test
    public void noOptimize() throws SQLFeatureNotSupportedException {
        when(optimizeRule.match(expr)).thenReturn(false);

        optimizer.optimize(expr);
        verify(optimizeRule, never()).optimize(expr);
    }
}