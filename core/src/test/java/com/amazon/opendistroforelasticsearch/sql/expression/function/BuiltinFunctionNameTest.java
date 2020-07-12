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

package com.amazon.opendistroforelasticsearch.sql.expression.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BuiltinFunctionNameTest {

  private static Stream<Arguments> ofArguments() {
    Stream.Builder<Arguments> builder = Stream.builder();
    return Arrays.asList(BuiltinFunctionName.values())
        .stream()
        .map(functionName -> Arguments.of(functionName.getName().getFunctionName(), functionName));
  }

  @ParameterizedTest
  @MethodSource("ofArguments")
  public void of(String name, BuiltinFunctionName expected) {
    assertTrue(BuiltinFunctionName.of(name).isPresent());
    assertEquals(expected, BuiltinFunctionName.of(name).get());
  }

  @Test
  public void caseInsensitive() {
    assertTrue(BuiltinFunctionName.of("aBs").isPresent());
    assertEquals(BuiltinFunctionName.of("aBs").get(), BuiltinFunctionName.ABS);
  }
}