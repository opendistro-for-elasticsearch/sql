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

package com.amazon.opendistro.sql.query.planner.core;

import java.util.EnumMap;

/**
 * Parameters needed for physical operator execution.
 */
public class ExecuteParams {

    /** Mapping from type to parameters */
    private EnumMap<ExecuteParamType, Object> params = new EnumMap<>(ExecuteParamType.class);

    public <T> void add(ExecuteParamType type, T param) {
        params.put(type, param);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ExecuteParamType type) {
        return (T) params.get(type);
    }

    public enum ExecuteParamType {
        CLIENT, RESOURCE_MANAGER, EXTRA_QUERY_FILTER, TIMEOUT
    }

}
