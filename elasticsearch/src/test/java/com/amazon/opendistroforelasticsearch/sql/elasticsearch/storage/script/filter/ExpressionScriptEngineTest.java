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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.ExpressionScriptEngine;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.serialization.ExpressionSerializer;
import org.elasticsearch.script.ScriptEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class ExpressionScriptEngineTest {

  @Mock
  private ExpressionSerializer serializer;

  private ScriptEngine scriptEngine;

  @BeforeEach
  void setUp() {
    scriptEngine = new ExpressionScriptEngine(serializer);
  }

  @Test
  void can_compile_script_code_to_expression() {

  }

  @Test
  void should_support_at_least_one_push_down_optimizations() {
    assertThat(
        scriptEngine.getSupportedContexts(),
        not(empty())
    );
  }

}