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

import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESIndex;
import org.junit.Ignore;
import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.ES_TYPE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.KEYWORD;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.NESTED;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.NUMBER;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.SHORT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.TEXT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.UNKNOWN;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESIndex.IndexType.NESTED_FIELD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test base type compatibility
 */
public class BaseTypeTest {

    @Test
    public void unknownTypeNameShouldReturnUnknown() {
        assertEquals(UNKNOWN, ESDataType.typeOf("this_is_a_new_es_type_we_arent_aware"));
    }

    @Test
    public void typeOfShouldIgnoreCase() {
        assertEquals(INTEGER, ESDataType.typeOf("Integer"));
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

    @Test
    public void nestedIndexTypeShouldBeCompatibleWithNestedDataType() {
        assertTrue(NESTED.isCompatible(new ESIndex("test", NESTED_FIELD)));
        assertTrue(ES_TYPE.isCompatible(new ESIndex("test", NESTED_FIELD)));
    }

}
