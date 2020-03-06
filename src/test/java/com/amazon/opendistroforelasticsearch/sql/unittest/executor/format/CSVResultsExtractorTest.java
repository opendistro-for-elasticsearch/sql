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

import com.amazon.opendistroforelasticsearch.sql.executor.csv.CSVResult;
import com.amazon.opendistroforelasticsearch.sql.executor.csv.CSVResultsExtractor;
import com.amazon.opendistroforelasticsearch.sql.executor.csv.CsvExtractorException;
import com.amazon.opendistroforelasticsearch.sql.executor.format.DataRows;
import com.amazon.opendistroforelasticsearch.sql.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValue;
import com.google.common.collect.ImmutableMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class CSVResultsExtractorTest {
    private final CSVResultsExtractor csvResultsExtractor = new CSVResultsExtractor(false, false, false);

    @Test
    public void extractResultsFromBindingTupleListShouldPass() throws CsvExtractorException {
        CSVResult csvResult = csv(Arrays.asList(BindingTuple.from(ImmutableMap.of("age", 31, "gender", "m")),
                                                BindingTuple.from(ImmutableMap.of("age", 31, "gender", "f")),
                                                BindingTuple.from(ImmutableMap.of("age", 39, "gender", "m")),
                                                BindingTuple.from(ImmutableMap.of("age", 39, "gender", "f"))),
                                  Arrays.asList("age", "gender"));

        assertThat(csvResult.getHeaders(), contains("age", "gender"));
        assertThat(csvResult.getLines(), contains("31,m", "31,f", "39,m", "39,f"));
    }

    @Test
    public void extractJSON() {
        List<BindingTuple> bindingTuples = Arrays.asList(BindingTuple.from(ImmutableMap.of("age", 31, "gender", "m")),
                                                         BindingTuple.from(ImmutableMap.of("age", 31, "gender", "f")),
                                                         BindingTuple.from(ImmutableMap.of("age", 39, "gender", "m")),
                                                         BindingTuple.from(ImmutableMap.of("age", 39, "gender", "f")));

        List<Map<String, Object>> rowList = bindingTuples.stream().map(tuple -> {
            Map<String, ExprValue> bindingMap = tuple.getBindingMap();
            Map<String, Object> rowMap = new HashMap<>();
            for (String s : bindingMap.keySet()) {
                rowMap.put(s, bindingMap.get(s).value());
            }
            return rowMap;
        }).collect(Collectors.toList());

        System.out.println(new JSONArray(rowList));
    }

    private CSVResult csv(List<BindingTuple> bindingTupleList, List<String> fieldNames) throws CsvExtractorException {
        return csvResultsExtractor.extractResults(bindingTupleList, false, ",", fieldNames);
    }
}
