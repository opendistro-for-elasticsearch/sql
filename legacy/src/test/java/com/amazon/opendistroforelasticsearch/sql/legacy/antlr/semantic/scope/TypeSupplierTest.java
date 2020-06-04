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

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.scope;

import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.SemanticAnalysisException;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class TypeSupplierTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void haveOneTypeShouldPass() {
        TypeSupplier age = new TypeSupplier("age", ESDataType.INTEGER);

        assertEquals(ESDataType.INTEGER, age.get());
    }

    @Test
    public void addSameTypeShouldPass() {
        TypeSupplier age = new TypeSupplier("age", ESDataType.INTEGER);
        age.add(ESDataType.INTEGER);

        assertEquals(ESDataType.INTEGER, age.get());
    }

    @Test
    public void haveTwoTypesShouldThrowException() {
        TypeSupplier age = new TypeSupplier("age", ESDataType.INTEGER);
        age.add(ESDataType.TEXT);

        exception.expect(SemanticAnalysisException.class);
        exception.expectMessage("Field [age] have conflict type");
        age.get();
    }
}