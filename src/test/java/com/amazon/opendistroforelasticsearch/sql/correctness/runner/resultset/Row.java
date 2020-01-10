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

package com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Row in result set.
 */
@EqualsAndHashCode
@ToString
@Getter
public class Row {

    private final Collection<Object> values;

    public Row() {
        this(new ArrayList<>()); // values in order by default
    }

    public Row(Collection<Object> values) {
        this.values = values;
    }

    public void add(Object value) {
        values.add(roundFloatNum(value));
    }

    private Object roundFloatNum(Object value) {
        if (value instanceof Float) {
            BigDecimal decimal = BigDecimal.valueOf((Float) value).setScale(2, RoundingMode.CEILING);
            value = decimal.doubleValue(); // Convert to double too
        } else if (value instanceof Double) {
            BigDecimal decimal = BigDecimal.valueOf((Double) value).setScale(2, RoundingMode.CEILING);
            value = decimal.doubleValue();
        }
        return value;
    }

}
