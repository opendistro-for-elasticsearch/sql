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

package com.amazon.opendistroforelasticsearch.sql.utils;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher Utils.
 */
public class MatcherUtils {
  /**
   * Check {@link ExprValue} type equal to {@link ExprCoreType}.
   */
  public static TypeSafeMatcher<ExprValue> hasType(ExprCoreType type) {
    return new TypeSafeMatcher<ExprValue>() {
      @Override
      public void describeTo(Description description) {
        description.appendText(type.toString());
      }

      @Override
      protected boolean matchesSafely(ExprValue value) {
        return type == value.type();
      }
    };
  }

  /**
   * Check {@link ExprValue} value equal to {@link Object}.
   */
  public static TypeSafeMatcher<ExprValue> hasValue(Object object) {
    return new TypeSafeMatcher<ExprValue>() {
      @Override
      public void describeTo(Description description) {
        description.appendText(object.toString());
      }

      @Override
      protected boolean matchesSafely(ExprValue value) {
        return object.equals(value.value());
      }
    };
  }
}
