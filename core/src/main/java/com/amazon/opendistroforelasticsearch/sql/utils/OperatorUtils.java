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

package com.amazon.opendistroforelasticsearch.sql.utils;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprBooleanValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OperatorUtils {
  /**
   * Wildcard pattern matcher util.
   * Percent (%) character for wildcard,
   * Underscore (_) character for a single character match.
   * @param pattern string pattern to match.
   * @return if text matches pattern returns true; else return false.
   */
  public static ExprBooleanValue matches(ExprValue text, ExprValue pattern) {
    return ExprBooleanValue
        .of(Pattern.compile(patternToRegex(pattern.stringValue())).matcher(text.stringValue())
            .matches());
  }

  /**
   * Checks if text matches regular expression pattern.
   * @param pattern string pattern to match.
   * @return if text matches pattern returns true; else return false.
   */
  public static ExprIntegerValue matchesRegexp(ExprValue text, ExprValue pattern) {
    return new ExprIntegerValue(Pattern.compile(pattern.stringValue()).matcher(text.stringValue())
                    .matches() ? 1 : 0);
  }

  private static final char DEFAULT_ESCAPE = '\\';

  private static String patternToRegex(String patternString) {
    StringBuilder regex = new StringBuilder(patternString.length() * 2);
    regex.append('^');
    boolean escaped = false;
    for (char currentChar : patternString.toCharArray()) {
      if (!escaped && currentChar == DEFAULT_ESCAPE) {
        escaped = true;
      } else {
        switch (currentChar) {
          case '%':
            if (escaped) {
              regex.append("%");
            } else {
              regex.append(".*");
            }
            escaped = false;
            break;
          case '_':
            if (escaped) {
              regex.append("_");
            } else {
              regex.append('.');
            }
            escaped = false;
            break;
          default:
            switch (currentChar) {
              case '\\':
              case '^':
              case '$':
              case '.':
              case '*':
              case '[':
              case ']':
              case '(':
              case ')':
              case '|':
              case '+':
                regex.append('\\');
                break;
              default:
            }

            regex.append(currentChar);
            escaped = false;
        }
      }
    }
    regex.append('$');
    return regex.toString();
  }
}
