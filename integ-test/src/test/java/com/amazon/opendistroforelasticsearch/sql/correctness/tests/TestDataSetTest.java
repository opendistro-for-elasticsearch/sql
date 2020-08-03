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

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.amazon.opendistroforelasticsearch.sql.correctness.testset.TestDataSet;
import org.junit.Test;

/**
 * Tests for {@link TestDataSet}
 */
public class TestDataSetTest {

  @Test
  public void testDataSetWithSingleColumnData() {
    String mappings =
        "{\n"
            + "  \"mappings\": {\n"
            + "    \"properties\": {\n"
            + "      \"field\": {\n"
            + "        \"type\": \"text\"\n"
            + "      }\n"
            + "    }\n"
            + "  }\n"
            + "}";

    TestDataSet dataSet = new TestDataSet("test", mappings, "field\nhello\nworld\n123");
    assertEquals("test", dataSet.getTableName());
    assertEquals(mappings, dataSet.getSchema());
    assertThat(
        dataSet.getDataRows(),
        contains(
            new Object[] {"field"},
            new Object[] {"hello"},
            new Object[] {"world"},
            new Object[] {"123"}
        )
    );
  }

  @Test
  public void testDataSetWithMultiColumnsData() {
    String mappings =
        "{\n"
            + "  \"mappings\": {\n"
            + "    \"properties\": {\n"
            + "      \"field1\": {\n"
            + "        \"type\": \"text\"\n"
            + "      },\n"
            + "      \"field2\": {\n"
            + "        \"type\": \"integer\"\n"
            + "      }\n"
            + "    }\n"
            + "  }\n"
            + "}";

    TestDataSet dataSet = new TestDataSet("test", mappings,
        "field1,field2\nhello,123\nworld,456");
    assertThat(
        dataSet.getDataRows(),
        contains(
            new Object[] {"field1", "field2"},
            new Object[] {"hello", 123},
            new Object[] {"world", 456}
        )
    );
  }

  @Test
  public void testDataSetWithEscapedComma() {
    String mappings =
        "{\n"
            + "  \"mappings\": {\n"
            + "    \"properties\": {\n"
            + "      \"field\": {\n"
            + "        \"type\": \"text\"\n"
            + "      }\n"
            + "    }\n"
            + "  }\n"
            + "}";

    TestDataSet dataSet = new TestDataSet("test", mappings,
        "field\n\"hello,world,123\"\n123\n\"[abc,def,ghi]\"");
    assertThat(
        dataSet.getDataRows(),
        contains(
            new Object[] {"field"},
            new Object[] {"hello,world,123"},
            new Object[] {"123"},
            new Object[] {"[abc,def,ghi]"}
        )
    );
  }

}
