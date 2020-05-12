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

package com.amazon.opendistroforelasticsearch.sql.expression.aggregation;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprNullValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionImplementation;
import com.amazon.opendistroforelasticsearch.sql.storage.BindingTuple;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Average Aggregator
 */
@RequiredArgsConstructor
public class AvgAggregator implements Aggregator {
    private final List<Expression> expressionList;
    private int count;
    private double total;
    private boolean isNullResult = false;

    @Override
    public void open() {
        count = 0;
        total = 0d;
    }

    @Override
    public void iterate(BindingTuple tuple) {
        Expression expression = expressionList.get(0);
        ExprValue value = expression.valueOf(tuple);
        if (value.isNull() || value.isMissing()) {
            isNullResult = true;
        } else {
            count++;
            total += ExprValueUtils.getDoubleValue(value);
        }
    }

    @Override
    public ExprValue result() {
        return isNullResult ? ExprNullValue.of() : ExprValueUtils.doubleValue(total / count);
    }
}
