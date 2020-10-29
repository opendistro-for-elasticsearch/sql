/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.jdbc.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class BinaryTypeTests {

  @ParameterizedTest
  @CsvSource(value = {
      "U29tZSBiaW5hcnkgYmxvYg==, U29tZSBiaW5hcnkgYmxvYg==",
      "TWFuIGlzIGRpc3Rpbmd1aXN, TWFuIGlzIGRpc3Rpbmd1aXN",
      "YW55IGNhcm5hbCBwbGVhc3VyZS4=, YW55IGNhcm5hbCBwbGVhc3VyZS4="
  })
  void testTimeFromString(String inputString, String result) {
    String binary = Assertions.assertDoesNotThrow(
        () -> BinaryType.INSTANCE.fromValue(inputString, null));
    assertEquals(result, binary);
  }

}
