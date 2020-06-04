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

import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.KEYWORD;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.NUMBER;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.TEXT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.TYPE_ERROR;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.function.ScalarFunction.LOG;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

/**
 * Generic type test
 */
public class GenericTypeTest {

    @Test
    public void passNumberArgToLogShouldReturnNumber() {
        assertEquals(DOUBLE, LOG.construct(singletonList(NUMBER)));
    }

    @Test
    public void passIntegerArgToLogShouldReturnDouble() {
        assertEquals(DOUBLE, LOG.construct(singletonList(INTEGER)));
    }

    @Test
    public void passLongArgToLogShouldReturnDouble() {
        assertEquals(DOUBLE, LOG.construct(singletonList(LONG)));
    }

    @Test
    public void passTextArgToLogShouldReturnTypeError() {
        assertEquals(TYPE_ERROR, LOG.construct(singletonList(TEXT)));
    }

    @Test
    public void passKeywordArgToLogShouldReturnTypeError() {
        assertEquals(TYPE_ERROR, LOG.construct(singletonList(KEYWORD)));
    }

}
