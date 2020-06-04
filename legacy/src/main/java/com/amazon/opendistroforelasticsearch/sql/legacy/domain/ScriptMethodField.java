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

package com.amazon.opendistroforelasticsearch.sql.legacy.domain;

import com.alibaba.druid.sql.ast.expr.SQLAggregateOption;

import java.util.List;

/**
 * Stores information about function name for script fields
 */
public class ScriptMethodField extends MethodField {
    private final String functionName;

    public ScriptMethodField(String functionName, List<KVValue> params, SQLAggregateOption option, String alias) {
        super("script", params, option, alias);
        this.functionName = functionName;
    }

    public String getFunctionName() {
        return functionName;
    }

    @Override
    public boolean isScriptField() {
        return true;
    }
}
