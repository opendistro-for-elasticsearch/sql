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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.node.scroll;

import com.amazon.opendistroforelasticsearch.sql.legacy.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.Row;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class BindingTupleRow implements Row<BindingTuple> {
    private final BindingTuple bindingTuple;

    @Override
    public RowKey key(String[] colNames) {
        return null;
    }

    @Override
    public Row<BindingTuple> combine(Row<BindingTuple> otherRow) {
        throw new RuntimeException("unsupported operation");
    }

    @Override
    public void retain(Map<String, String> colNameAlias) {
        // do nothing
    }

    @Override
    public BindingTuple data() {
        return bindingTuple;
    }
}
