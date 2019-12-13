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

package com.amazon.opendistroforelasticsearch.sql.correctness.tests;

import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.DBResult;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.Row;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link DBResult} and {@link Row}
 */
public class DBResultTest {

    @Test
    public void rowShouldBeCloseToOtherRowWithSimilarFloat() {
        Row row1 = new Row(Arrays.asList(1.000001));
        Row row2 = new Row(Arrays.asList(1.000002));
        assertTrue(row1.isCloseTo(row2));
        assertTrue(row2.isCloseTo(row1));
    }

    @Test
    public void rowShouldNotBeCloseToOtherRowWithDifferentString() {
        Row row1 = new Row(Arrays.asList("hello"));
        Row row2 = new Row(Arrays.asList("hello1"));
        assertFalse(row1.isCloseTo(row2));
        assertFalse(row2.isCloseTo(row1));
    }

}
