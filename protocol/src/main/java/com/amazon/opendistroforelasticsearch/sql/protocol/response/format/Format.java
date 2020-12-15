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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Format {
  JDBC("jdbc"),
  CSV("csv");

  @Getter
  private final String formatName;

  private static final Map<String, Format> ALL_FORMATS;
  static {
    ImmutableMap.Builder<String, Format> builder = new ImmutableMap.Builder<>();
    for (Format format : Format.values()) {
      builder.put(format.formatName, format);
    }
    ALL_FORMATS = builder.build();
  }

  public static Optional<Format> of(String formatName) {
    String format = Strings.isNullOrEmpty(formatName) ? "jdbc" : formatName.toLowerCase();
    return Optional.ofNullable(ALL_FORMATS.getOrDefault(format, null));
  }
}
