/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.filter.lucene;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.filter.lucene.RangeQuery.Comparison;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RangeQueryTest {

  @Test
  void should_throw_exception_for_unsupported_comparison() {
    // Note that since we do switch check on enum comparison, this should'be impossible
    assertThrows(IllegalStateException.class, () ->
        new RangeQuery(Comparison.BETWEEN)
            .doBuild("name", STRING, ExprValueUtils.stringValue("John")));
  }

}