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

package com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.alias;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for util class {@link Identifier}.
 */
public class IdentifierTest {

    @Test
    public void identifierWithWordBeforeFirstDotShouldBeConsideredHavePrefix() {
        Assert.assertTrue(identifier("accounts.age").hasPrefix());
    }

    @Test
    public void identifierWithoutDotShouldNotBeConsideredHavePrefix() {
        Assert.assertFalse(identifier("age").hasPrefix());
    }

    @Test
    public void identifierStartingWithDotShouldNotBeConsideredHavePrefix() {
        Assert.assertFalse(identifier(".age").hasPrefix());
    }

    @Test
    public void prefixOfIdentifierShouldBeWordBeforeFirstDot() {
        Assert.assertEquals("accounts", identifier("accounts.age").prefix());
    }

    @Test
    public void removePrefixShouldRemoveFirstWordAndDot() {
        Identifier identifier = identifier("accounts.age");
        identifier.removePrefix();
        Assert.assertEquals("age", identifier.name());
    }

    private Identifier identifier(String name) {
        return new Identifier(new SQLIdentifierExpr(name));
    }
}