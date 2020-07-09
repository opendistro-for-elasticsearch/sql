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

package com.amazon.opendistroforelasticsearch.sql.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AnalysisContextTest {

  private final AnalysisContext context = new AnalysisContext();

  @Test
  public void rootEnvironmentShouldBeThereInitially() {
    assertNotNull(context.peek());
  }

  @Test
  public void pushAndPopEnvironmentShouldPass() {
    context.push();
    context.pop();
  }

  @Test
  public void popRootEnvironmentShouldPass() {
    context.pop();
  }

  @Test
  public void popEmptyEnvironmentStackShouldFail() {
    context.pop();
    NullPointerException exception = assertThrows(NullPointerException.class, () -> context.pop());
    assertEquals("Fail to pop context due to no environment present", exception.getMessage());
  }
}