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

package com.amazon.opendistroforelasticsearch.sql.unittest.utils;

import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.amazon.opendistroforelasticsearch.sql.domain.KVValue;
import com.amazon.opendistroforelasticsearch.sql.utils.SQLFunctions;
import com.google.common.collect.ImmutableList;
import org.elasticsearch.common.collect.Tuple;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SQLFunctionsTest {

    @Test
    public void testAssign() {
        final SQLIntegerExpr sqlIntegerExpr = new SQLIntegerExpr(10);
        final Tuple<String, String> assign = SQLFunctions.function("assign",
                ImmutableList.of(new KVValue(null, sqlIntegerExpr)),
                null,
                true);

        assertTrue(assign.v1().matches("assign_[0-9]+"));
        assertTrue(assign.v2().matches("def assign_[0-9]+ = 10;return assign_[0-9]+;"));
    }
}