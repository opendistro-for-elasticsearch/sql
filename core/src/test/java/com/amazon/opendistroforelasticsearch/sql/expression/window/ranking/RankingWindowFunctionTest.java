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

package com.amazon.opendistroforelasticsearch.sql.expression.window.ranking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowFrame;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class RankingWindowFunctionTest extends ExpressionTestBase {

  @Mock
  private WindowFrame windowFrame;

  @Test
  void test_row_number() {
    RankingWindowFunction rowNumber = dsl.rowNumber();

    when(windowFrame.isNewPartition()).thenReturn(true);
    assertEquals(1, rowNumber.rank(windowFrame));

    when(windowFrame.isNewPartition()).thenReturn(false);
    assertEquals(2, rowNumber.rank(windowFrame));
    assertEquals(3, rowNumber.rank(windowFrame));

    when(windowFrame.isNewPartition()).thenReturn(true);
    assertEquals(1, rowNumber.rank(windowFrame));
  }

}