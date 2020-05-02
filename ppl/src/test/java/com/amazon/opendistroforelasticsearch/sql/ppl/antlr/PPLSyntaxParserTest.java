/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.ppl.antlr;

import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertNotEquals;

public class PPLSyntaxParserTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testSearchCommandShouldPass() {
        ParseTree tree = new PPLSyntaxParser().analyzeSyntax("search source=t a=1 b=2");
        assertNotEquals(null, tree);
    }

    @Test
    public void testSearchCommandIgnoreSearchKeywordShouldPass() {
        ParseTree tree = new PPLSyntaxParser().analyzeSyntax("source=t a=1 b=2");
        assertNotEquals(null, tree);
    }

    @Test
    public void testSearchFieldsCommandShouldPass() {
        ParseTree tree = new PPLSyntaxParser().analyzeSyntax("search source=t a=1 b=2 | fields a,b");
        assertNotEquals(null, tree);
    }

    @Test
    public void testSearchCommandWithoutSourceShouldFail() {
        exceptionRule.expect(RuntimeException.class);
        exceptionRule.expectMessage("Failed to parse query due to offending symbol");

        new PPLSyntaxParser().analyzeSyntax("search a=1");
    }
}