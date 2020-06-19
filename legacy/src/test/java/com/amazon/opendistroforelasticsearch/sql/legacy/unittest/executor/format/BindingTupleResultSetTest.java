/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.executor.format;

import com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.BindingTupleResultSet;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.DataRows;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.Schema;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.ColumnNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.legacy.util.MatcherUtils.featureValueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;

public class BindingTupleResultSetTest {

    @Test
    public void buildDataRowsFromBindingTupleShouldPass() {
        assertThat(row(
            Arrays.asList(
                ColumnNode.builder().name("age").type(Schema.Type.INTEGER).build(),
                ColumnNode.builder().name("gender").type(Schema.Type.TEXT).build()),
            Arrays.asList(BindingTuple.from(ImmutableMap.of("age", 31, "gender", "m")),
                BindingTuple.from(ImmutableMap.of("age", 31, "gender", "f")),
                BindingTuple.from(ImmutableMap.of("age", 39, "gender", "m")),
                BindingTuple.from(ImmutableMap.of("age", 39, "gender", "f")))),
            containsInAnyOrder(rowContents(allOf(hasEntry("age", 31), hasEntry("gender", (Object) "m"))),
                rowContents(allOf(hasEntry("age", 31), hasEntry("gender", (Object) "f"))),
                rowContents(allOf(hasEntry("age", 39), hasEntry("gender", (Object) "m"))),
                rowContents(allOf(hasEntry("age", 39), hasEntry("gender", (Object) "f")))));
    }

    @Test
    public void buildDataRowsFromBindingTupleIncludeLongValueShouldPass() {
        assertThat(row(
            Arrays.asList(
                ColumnNode.builder().name("longValue").type(Schema.Type.LONG).build(),
                ColumnNode.builder().name("gender").type(Schema.Type.TEXT).build()),
            Arrays.asList(
                BindingTuple.from(ImmutableMap.of("longValue", Long.MAX_VALUE, "gender", "m")),
                BindingTuple.from(ImmutableMap.of("longValue", Long.MIN_VALUE, "gender", "f")))),
            containsInAnyOrder(
                rowContents(allOf(hasEntry("longValue", Long.MAX_VALUE), hasEntry("gender", (Object) "m"))),
                rowContents(allOf(hasEntry("longValue", Long.MIN_VALUE), hasEntry("gender", (Object) "f")))));
    }

    @Test
    public void buildDataRowsFromBindingTupleIncludeDateShouldPass() {
        assertThat(row(
            Arrays.asList(
                ColumnNode.builder().alias("dateValue").type(Schema.Type.DATE).build(),
                ColumnNode.builder().alias("gender").type(Schema.Type.TEXT).build()),
            Arrays.asList(
                BindingTuple.from(ImmutableMap.of("dateValue", 1529712000000L, "gender", "m")))),
            containsInAnyOrder(
                rowContents(allOf(hasEntry("dateValue", "2018-06-23 00:00:00.000"), hasEntry("gender", (Object) "m")))));
    }

    private static Matcher<DataRows.Row> rowContents(Matcher<Map<String, Object>> matcher) {
        return featureValueOf("DataRows.Row", matcher, DataRows.Row::getContents);
    }

    private List<DataRows.Row> row(List<ColumnNode> columnNodes, List<BindingTuple> bindingTupleList) {
        return ImmutableList.copyOf(BindingTupleResultSet.buildDataRows(columnNodes, bindingTupleList).iterator());
    }
}
