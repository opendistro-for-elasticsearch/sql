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

import com.amazon.opendistroforelasticsearch.sql.correctness.testset.TestDataSet;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link TestDataSet}
 */
public class TestDataSetTest {

    @Test
    public void testDataSetWithSingleColumnData() {
        TestDataSet dataSet = new TestDataSet("test", "mappings", "hello\nworld\n123");
        assertEquals("test", dataSet.getTableName());
        assertEquals("mappings", dataSet.getSchema());
        assertThat(
            dataSet.getDataRows(),
            contains(
                new String[]{"hello"},
                new String[]{"world"},
                new String[]{"123"}
            )
        );
    }

    @Test
    public void testDataSetWithMultiColumnsData() {
        TestDataSet dataSet = new TestDataSet("test", "mappings", "hello,world\n123");
        assertThat(
            dataSet.getDataRows(),
            contains(
                new String[]{"hello", "world"},
                new String[]{"123"}
            )
        );
    }

    @Test
    public void testDataSetWithEscapedComma() {
        TestDataSet dataSet = new TestDataSet("test", "mappings", "hello,\"hello,world,123\"\n123\n\"[abc,def,ghi]\",456");
        assertThat(
            dataSet.getDataRows(),
            contains(
                new String[]{"hello", "hello,world,123"},
                new String[]{"123"},
                new String[]{"[abc,def,ghi]", "456"}
            )
        );
    }

}
