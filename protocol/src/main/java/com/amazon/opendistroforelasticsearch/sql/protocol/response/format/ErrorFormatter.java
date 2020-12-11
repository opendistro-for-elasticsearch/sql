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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorFormatter {

  private static final Gson PRETTY_PRINT_GSON =
      AccessController.doPrivileged(
          (PrivilegedAction<Gson>) () -> new GsonBuilder().setPrettyPrinting().create());
  private static final Gson GSON = AccessController.doPrivileged(
      (PrivilegedAction<Gson>) () -> new GsonBuilder().create());

  /**
   * Util method to format {@link Throwable} response to JSON string in compact printing.
   */
  public static String compactFormat(Throwable t) {
    JsonError error = new ErrorFormatter.JsonError(t.getClass().getSimpleName(),
        t.getMessage());
    return compactJsonify(error);
  }

  /**
   * Util method to format {@link Throwable} response to JSON string in pretty printing.
   */
  public static  String prettyFormat(Throwable t) {
    JsonError error = new ErrorFormatter.JsonError(t.getClass().getSimpleName(),
        t.getMessage());
    return prettyJsonify(error);
  }

  public static String compactJsonify(Object jsonObject) {
    return AccessController.doPrivileged(
        (PrivilegedAction<String>) () -> GSON.toJson(jsonObject));
  }

  public static String prettyJsonify(Object jsonObject) {
    return AccessController.doPrivileged(
        (PrivilegedAction<String>) () -> PRETTY_PRINT_GSON.toJson(jsonObject));
  }

  @RequiredArgsConstructor
  @Getter
  public static class JsonError {
    private final String type;
    private final String reason;
  }
}
