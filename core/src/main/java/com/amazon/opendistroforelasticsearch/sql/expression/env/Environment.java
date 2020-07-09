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

package com.amazon.opendistroforelasticsearch.sql.expression.env;

/**
 * The definition of the environment.
 * @param <E>  the type of expression
 * @param <V> the type of expression value
 */
public interface Environment<E, V> {

  /**
   * resolve the value of expression from the environment.
   */
  V resolve(E var);

  /**
   * Extend the environment.
   *
   * @param env     environment
   * @param expr    expression.
   * @param value   expression value.
   * @param <E>  the type of expression
   * @param <V> the type of expression value
   * @return extended environment.
   */
  static <E, V> Environment<E, V> extendEnv(
      Environment<E, V> env, E expr, V value) {
    return var -> {
      if (var.equals(expr)) {
        return value;
      } else {
        return env.resolve(var);
      }
    };
  }
}
