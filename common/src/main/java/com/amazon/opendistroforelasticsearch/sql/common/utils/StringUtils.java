/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.common.utils;

import com.google.common.base.Strings;

public class StringUtils {
  /**
   * Unquote any string with mark specified.
   * @param text string
   * @param mark quotation mark
   * @return An unquoted string whose outer pair of (single/double/back-tick) quotes have been
   *     removed
   */
  public static String unquote(String text, String mark) {
    if (isQuoted(text, mark)) {
      return text.substring(mark.length(), text.length() - mark.length());
    }
    return text;
  }

  /**
   * Unquote Identifier which has " or ' or ` as mark.
   * @param text string
   * @return An unquoted string whose outer pair of (single/double/back-tick) quotes have been
   *     removed
   */
  public static String unquoteText(String text) {
    if (isQuoted(text, "\"") || isQuoted(text, "'") || isQuoted(text, "`")) {
      return text.substring(1, text.length() - 1);
    } else {
      return text;
    }
  }

  /**
   * Unquote Identifier which has " or ` as mark.
   * @param identifier identifier that possibly enclosed by double quotes or back ticks
   * @return An unquoted string whose outer pair of (double/back-tick) quotes have been
   *     removed
   */
  public static String unquoteIdentifier(String identifier) {
    if (isQuoted(identifier, "\"") || isQuoted(identifier, "`")) {
      return identifier.substring(1, identifier.length() - 1);
    } else {
      return identifier;
    }
  }

  private static boolean isQuoted(String text, String mark) {
    return !Strings.isNullOrEmpty(text) && text.startsWith(mark) && text.endsWith(mark);
  }
}
