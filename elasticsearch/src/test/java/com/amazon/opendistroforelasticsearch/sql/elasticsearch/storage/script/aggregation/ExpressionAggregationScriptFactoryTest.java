/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.aggregation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.script.AggregationScript;
import org.elasticsearch.search.lookup.LeafSearchLookup;
import org.elasticsearch.search.lookup.SearchLookup;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class ExpressionAggregationScriptFactoryTest {

  @Mock
  private SearchLookup searchLookup;

  @Mock
  private LeafSearchLookup leafSearchLookup;

  @Mock
  private LeafReaderContext leafReaderContext;

  private final Expression expression = DSL.literal(true);

  private final Map<String, Object> params = Collections.emptyMap();

  private final AggregationScript.Factory factory =
      new ExpressionAggregationScriptFactory(expression);

  @Test
  void should_return_deterministic_result() {
    assertTrue(factory.isResultDeterministic());
  }

  @Test
  void can_initialize_expression_filter_script() throws IOException {
    when(searchLookup.getLeafSearchLookup(leafReaderContext)).thenReturn(leafSearchLookup);

    AggregationScript.LeafFactory leafFactory = factory.newFactory(params, searchLookup);
    assertFalse(leafFactory.needs_score());

    AggregationScript actualScript = leafFactory.newInstance(leafReaderContext);

    assertEquals(
        new ExpressionAggregationScript(expression, searchLookup, leafReaderContext, params),
        actualScript
    );
  }
}