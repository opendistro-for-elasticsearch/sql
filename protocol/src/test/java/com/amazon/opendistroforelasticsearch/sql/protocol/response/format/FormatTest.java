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

package com.amazon.opendistroforelasticsearch.sql.protocol.response.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link Format}.
 */
public class FormatTest {

  @Test
  void defaultFormat() {
    Optional<Format> format = Format.of("");
    assertTrue(format.isPresent());
    assertEquals(Format.JDBC, format.get());
  }

  @Test
  void jdbc() {
    Optional<Format> format = Format.of("jdbc");
    assertTrue(format.isPresent());
    assertEquals(Format.JDBC, format.get());
  }

  @Test
  void csv() {
    Optional<Format> format = Format.of("csv");
    assertTrue(format.isPresent());
    assertEquals(Format.CSV, format.get());
  }

  @Test
  void caseSensitive() {
    Optional<Format> format = Format.of("JDBC");
    assertTrue(format.isPresent());
    assertEquals(Format.JDBC, format.get());
  }

  @Test
  void unsupportedFormat() {
    Optional<Format> format = Format.of("raw");
    assertFalse(format.isPresent());
  }

}
