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

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types;

import org.junit.Test;

import java.util.Arrays;

import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.GEO_POINT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.NUMBER;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.TYPE_ERROR;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.UNKNOWN;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.special.Generic.T;
import static org.junit.Assert.assertEquals;

/**
 * Test cases for default implementation methods in interface TypeExpression
 */
public class TypeExpressionTest {

    private final TypeExpression test123 = new TypeExpression() {

        @Override
        public String getName() {
            return "TEST123";
        }

        @Override
        public TypeExpressionSpec[] specifications() {
            return new TypeExpressionSpec[] {
                new TypeExpressionSpec().map(T(NUMBER)).to(T),
                new TypeExpressionSpec().map(STRING, BOOLEAN).to(DATE)
            };
        }
    };

    @Test
    public void emptySpecificationShouldAlwaysReturnUnknown() {
        TypeExpression expr = new TypeExpression() {
            @Override
            public TypeExpressionSpec[] specifications() {
                return new TypeExpressionSpec[0];
            }

            @Override
            public String getName() {
                return "Temp type expression with empty specification";
            }
        };
        assertEquals(UNKNOWN, expr.construct(Arrays.asList(NUMBER)));
        assertEquals(UNKNOWN, expr.construct(Arrays.asList(STRING, BOOLEAN)));
        assertEquals(UNKNOWN, expr.construct(Arrays.asList(INTEGER, DOUBLE, GEO_POINT)));
    }

    @Test
    public void compatibilityCheckShouldPassIfAnySpecificationCompatible() {
        assertEquals(DOUBLE, test123.construct(Arrays.asList(DOUBLE)));
        assertEquals(DATE, test123.construct(Arrays.asList(STRING, BOOLEAN)));
    }

    @Test
    public void compatibilityCheckShouldFailIfNoSpecificationCompatible() {
        assertEquals(TYPE_ERROR, test123.construct(Arrays.asList(BOOLEAN)));
    }

    @Test
    public void usageShouldPrintAllSpecifications() {
        assertEquals("TEST123(NUMBER T) -> T or TEST123(STRING, BOOLEAN) -> DATE", test123.usage());
    }

}
