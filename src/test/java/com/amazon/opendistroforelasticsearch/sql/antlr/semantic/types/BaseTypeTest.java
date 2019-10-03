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

package com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types;

import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType;
import org.junit.Ignore;
import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.KEYWORD;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.NUMBER;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.SHORT;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.TEXT;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.UNKNOWN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test base type compatibility
 */
public class BaseTypeTest {

    @Test
    public void unknownTypeNameShouldReturnUnknown() {
        assertEquals(UNKNOWN, BaseType.typeOf("this_is_a_new_es_type_we_arent_aware"));
    }

    @Test
    public void typeOfShouldIgnoreCase() {
        assertEquals(INTEGER, BaseType.typeOf("Integer"));
    }

    @Test
    public void sameBaseTypeShouldBeCompatible() {
        assertTrue(INTEGER.isCompatible(INTEGER));
        assertTrue(BOOLEAN.isCompatible(BOOLEAN));
    }

    @Test
    public void parentBaseTypeShouldBeCompatibleWithSubBaseType() {
        assertTrue(NUMBER.isCompatible(DOUBLE));
        assertTrue(DOUBLE.isCompatible(FLOAT));
        assertTrue(FLOAT.isCompatible(INTEGER));
        assertTrue(INTEGER.isCompatible(SHORT));
        assertTrue(INTEGER.isCompatible(LONG));
        assertTrue(STRING.isCompatible(TEXT));
        assertTrue(STRING.isCompatible(KEYWORD));
        assertTrue(DATE.isCompatible(STRING));
    }

    @Test
    public void ancestorBaseTypeShouldBeCompatibleWithSubBaseType() {
        assertTrue(NUMBER.isCompatible(LONG));
        assertTrue(NUMBER.isCompatible(DOUBLE));
        assertTrue(DOUBLE.isCompatible(INTEGER));
        assertTrue(INTEGER.isCompatible(SHORT));
        assertTrue(INTEGER.isCompatible(LONG));
    }

    @Ignore("Two way compatibility is not necessary")
    @Test
    public void subBaseTypeShouldBeCompatibleWithParentBaseType() {
        assertTrue(KEYWORD.isCompatible(STRING));
    }

    @Test
    public void nonRelatedBaseTypeShouldNotBeCompatible() {
        assertFalse(SHORT.isCompatible(TEXT));
        assertFalse(DATE.isCompatible(BOOLEAN));
    }

    @Test
    public void unknownBaseTypeShouldBeCompatibleWithAnyBaseType() {
        assertTrue(UNKNOWN.isCompatible(INTEGER));
        assertTrue(UNKNOWN.isCompatible(KEYWORD));
        assertTrue(UNKNOWN.isCompatible(BOOLEAN));
    }

    @Test
    public void anyBaseTypeShouldBeCompatibleWithUnknownBaseType() {
        assertTrue(LONG.isCompatible(UNKNOWN));
        assertTrue(TEXT.isCompatible(UNKNOWN));
        assertTrue(DATE.isCompatible(UNKNOWN));
    }

}
