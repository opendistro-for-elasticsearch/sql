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

package com.amazon.opendistroforelasticsearch.sql.expression.function;

import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;

import java.util.List;

/**
 * The definition of function which create {@link FunctionExpression} from input {@link Expression} list.
 */
public interface FunctionExpressionBuilder {

    /**
     * Create {@link FunctionExpression} from input {@link Expression} list
     * @param arguments {@link Expression} list
     * @return {@link FunctionExpression}
     */
    FunctionExpression apply(List<Expression> arguments);
}
