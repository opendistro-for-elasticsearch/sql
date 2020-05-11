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

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TypeEnvironmentTest {

    /**
     * Use context class for push/pop
     */
    private AnalysisContext context = new AnalysisContext();

    @Test
    public void defineFieldSymbolInDifferentEnvironmentsShouldBeAbleToResolve() {
        // Root environment
        Expression age = DSL.ref("s.age");
        environment().define(age, ExprType.INTEGER);
        assertEquals(ExprType.INTEGER, environment().resolve(age));

        // New environment 1
        context.push();
        Expression city = DSL.ref("s.city");
        environment().define(city, ExprType.STRING);
        assertEquals(ExprType.INTEGER, environment().resolve(age));
        assertEquals(ExprType.STRING, environment().resolve(city));

        // New environment 2
        context.push();
        Expression manager = DSL.ref("s.manager");
        environment().define(manager, ExprType.STRUCT);
        assertEquals(ExprType.INTEGER, environment().resolve(age));
        assertEquals(ExprType.STRING, environment().resolve(city));
        assertEquals(ExprType.STRUCT, environment().resolve(manager));
    }

    @Test
    public void defineFieldSymbolInDifferentEnvironmentsShouldNotAbleToResolveOncePopped() {
        // Root environment
        Expression age = DSL.ref("s.age");
        environment().define(age, ExprType.INTEGER);

        // New environment
        context.push();
        Expression city = DSL.ref("s.city");
        environment().define(city, ExprType.STRING);
        Expression manager = DSL.ref("s.manager");
        environment().define(manager, ExprType.STRUCT);
        assertEquals(ExprType.INTEGER, environment().resolve(age));
        assertEquals(ExprType.STRING, environment().resolve(city));
        assertEquals(ExprType.STRUCT, environment().resolve(manager));

        context.pop();
        assertEquals(ExprType.INTEGER, environment().resolve(age));
        SemanticCheckException exception = assertThrows(SemanticCheckException.class, () -> environment().resolve(city));
        assertEquals("can't resolve expression s.city in type env", exception.getMessage());
        exception = assertThrows(SemanticCheckException.class, () -> environment().resolve(manager));
        assertEquals("can't resolve expression s.manager in type env", exception.getMessage());
    }

    @Test
    public void resolveLiteralInEnvFailed() {
        SemanticCheckException exception = assertThrows(SemanticCheckException.class,
                () -> environment().resolve(DSL.literal(ExprValueUtils.integerValue(1))));
        assertEquals("can't resolve expression 1 in type env", exception.getMessage());
    }


    @Test
    public void defineLiteralInEnvIsIllegal() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> environment().define(DSL.literal(ExprValueUtils.integerValue(1)), ExprType.INTEGER));
        assertEquals("only support define reference, unexpected expression 1", exception.getMessage());
    }

    private TypeEnvironment environment() {
        return context.peek();
    }

}
