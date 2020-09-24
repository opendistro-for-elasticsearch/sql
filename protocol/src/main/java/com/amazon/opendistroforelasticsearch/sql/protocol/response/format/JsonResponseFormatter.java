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

package com.amazon.opendistroforelasticsearch.sql.protocol.response.format;

import static com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter.Style.PRETTY;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Abstract class for all JSON formatter.
 *
 * @param <R> response generic type which could be DQL or DML response
 */
@RequiredArgsConstructor
public abstract class JsonResponseFormatter<R> implements ResponseFormatter<R> {

  /**
   * JSON format styles: pretty format or compact format without indent and space.
   */
  public enum Style {
    PRETTY, COMPACT
  }

  /**
   * JSON format style.
   */
  private final Style style;

  private static final Gson PRETTY_PRINT_GSON =
      AccessController.doPrivileged(
          (PrivilegedAction<Gson>) () -> new GsonBuilder().setPrettyPrinting().create());
  private static final Gson GSON = AccessController.doPrivileged(
      (PrivilegedAction<Gson>) () -> new GsonBuilder().create());

  @Override
  public String format(R response) {
    return jsonify(buildJsonObject(response));
  }

  @Override
  public String format(Throwable t) {
    JsonError error = new JsonError(t.getClass().getSimpleName(),
        t.getMessage());
    return jsonify(error);
  }

  /**
   * Build JSON object to generate response json string.
   *
   * @param response response
   * @return json object for response
   */
  protected abstract Object buildJsonObject(R response);

  private String jsonify(Object jsonObject) {
    return AccessController.doPrivileged((PrivilegedAction<String>) () ->
        (style == PRETTY) ? PRETTY_PRINT_GSON.toJson(jsonObject) : GSON.toJson(jsonObject));
  }

  @RequiredArgsConstructor
  @Getter
  public static class JsonError {
    private final String type;
    private final String reason;
  }
}
