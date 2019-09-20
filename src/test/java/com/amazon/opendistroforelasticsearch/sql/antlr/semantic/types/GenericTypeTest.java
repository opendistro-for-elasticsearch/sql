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

import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.KEYWORD;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.NUMBER;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.TEXT;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.ScalarFunction.LOG;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

/**
 * Generic type test
 */
public class GenericTypeTest {

    @Test
    public void passNumberArgToLogShouldReturnNumber() {
        assertEquals(NUMBER, LOG.construct(singletonList(NUMBER)));
    }

    @Test
    public void passIntegerArgToLogShouldReturnInteger() {
        assertEquals(INTEGER, LOG.construct(singletonList(INTEGER)));
    }

    @Test
    public void passLongArgToLogShouldReturnLong() {
        assertEquals(LONG, LOG.construct(singletonList(LONG)));
    }

    @Test
    public void passTextArgToLogShouldReturnText() {
        assertEquals(TEXT, LOG.construct(singletonList(TEXT)));
    }

    @Test
    public void passKeywordArgToLogShouldReturnKeyword() {
        assertEquals(KEYWORD, LOG.construct(singletonList(KEYWORD)));
    }

}
