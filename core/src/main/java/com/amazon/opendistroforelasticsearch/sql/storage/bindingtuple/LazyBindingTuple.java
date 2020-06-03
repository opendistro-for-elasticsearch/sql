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

package com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

/**
 * Lazy Implementation of {@link BindingTuple}.
 */
@RequiredArgsConstructor
public class LazyBindingTuple extends BindingTuple {
  private final Function<String, ExprValue> lazyBinding;

  @Override
  public ExprValue resolve(ReferenceExpression ref) {
    return lazyBinding.apply(ref.getAttr());
  }
}
