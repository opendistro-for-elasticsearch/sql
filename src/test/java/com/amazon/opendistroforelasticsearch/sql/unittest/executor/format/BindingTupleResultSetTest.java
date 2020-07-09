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

package com.amazon.opendistroforelasticsearch.sql.unittest.executor.format;

import com.amazon.opendistroforelasticsearch.sql.executor.format.BindingTupleResultSet;
import com.amazon.opendistroforelasticsearch.sql.executor.format.DataRows;
import com.amazon.opendistroforelasticsearch.sql.expression.domain.BindingTuple;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.featureValueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;

public class BindingTupleResultSetTest {

    @Test
    public void buildDataRowsFromBindingTupleShouldPass() {
        assertThat(row(Arrays.asList(BindingTuple.from(ImmutableMap.of("age", 31, "gender", "m")),
                                     BindingTuple.from(ImmutableMap.of("age", 31, "gender", "f")),
                                     BindingTuple.from(ImmutableMap.of("age", 39, "gender", "m")),
                                     BindingTuple.from(ImmutableMap.of("age", 39, "gender", "f")))),
                   containsInAnyOrder(rowContents(allOf(hasEntry("age", 31), hasEntry("gender", (Object) "m"))),
                                      rowContents(allOf(hasEntry("age", 31), hasEntry("gender", (Object) "f"))),
                                      rowContents(allOf(hasEntry("age", 39), hasEntry("gender", (Object) "m"))),
                                      rowContents(allOf(hasEntry("age", 39), hasEntry("gender", (Object) "f")))));
    }

    private static Matcher<DataRows.Row> rowContents(Matcher<Map<String, Object>> matcher) {
        return featureValueOf("DataRows.Row", matcher, DataRows.Row::getContents);
    }

    private List<DataRows.Row> row(List<BindingTuple> bindingTupleList) {
        return ImmutableList.copyOf(BindingTupleResultSet.buildDataRows(bindingTupleList).iterator());
    }
}
