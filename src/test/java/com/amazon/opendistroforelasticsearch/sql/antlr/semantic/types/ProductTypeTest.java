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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.KEYWORD;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.NUMBER;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.STRING;

/**
 * Test cases fro product type
 */
public class ProductTypeTest {

    @Test
    public void singleSameTypeInTwoProductsShouldPass() {
        Product product1 = new Product(Arrays.asList(INTEGER));
        Product product2 = new Product(Arrays.asList(INTEGER));
        Assert.assertTrue(product1.isCompatible(product2));
        Assert.assertTrue(product2.isCompatible(product1));
    }

    @Test
    public void singleCompatibleTypeInTwoProductsShouldPass() {
        Product product1 = new Product(Arrays.asList(NUMBER));
        Product product2 = new Product(Arrays.asList(INTEGER));
        Assert.assertTrue(product1.isCompatible(product2));
        Assert.assertTrue(product2.isCompatible(product1));
    }

    @Test
    public void twoCompatibleTypesInTwoProductsShouldPass() {
        Product product1 = new Product(Arrays.asList(NUMBER, KEYWORD));
        Product product2 = new Product(Arrays.asList(INTEGER, STRING));
        Assert.assertTrue(product1.isCompatible(product2));
        Assert.assertTrue(product2.isCompatible(product1));
    }

    @Test
    public void inCompatibleTypesInTwoProductsShouldFail() {
        Product product1 = new Product(Arrays.asList(BOOLEAN));
        Product product2 = new Product(Arrays.asList(STRING));
        Assert.assertFalse(product1.isCompatible(product2));
        Assert.assertFalse(product2.isCompatible(product1));
    }

    @Test
    public void compatibleButDifferentTypeNumberInTwoProductsShouldFail() {
        Product product1 = new Product(Arrays.asList(KEYWORD, INTEGER));
        Product product2 = new Product(Arrays.asList(STRING));
        Assert.assertFalse(product1.isCompatible(product2));
        Assert.assertFalse(product2.isCompatible(product1));
    }

}
