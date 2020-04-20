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

package com.amazon.opendistroforelasticsearch.sql.expression.scalar.conversion;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.scalar.DSL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ToStringFunctionTest {
    private static BuiltinFunctionRepository builtinFunctionRepository;

    @BeforeAll
    public static void setup() {
        builtinFunctionRepository = new BuiltinFunctionRepository();
        ToStringFunction.register(builtinFunctionRepository);
    }

    @Test
    public void tostring() {
        FunctionExpression hex = builtinFunctionRepository.resolve(BuiltinFunctionName.TOSTRING.getName(),
                Arrays.asList(DSL.literal(ExprValueUtils.integerValue(15)),
                        DSL.literal(ExprValueUtils.stringValue("hex"))));
        assertEquals(ExprType.STRING, hex.type());
        assertEquals("0xF", hex.valueOf().value());
    }
}