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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.expression.core;

import com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation;
import org.junit.Test;


import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.ExpressionFactory.literal;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueFactory.doubleValue;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueFactory.integerValue;
import static org.junit.Assert.assertEquals;

public class CompoundExpressionTest extends ExpressionTest {

    @Test
    public void absAndAddShouldPass() {
        assertEquals(2.0d, apply(ScalarOperation.ABS, of(ScalarOperation.ADD,
                                                         literal(doubleValue(-1.0d)),
                                                         literal(integerValue(-1)))));
    }
}
