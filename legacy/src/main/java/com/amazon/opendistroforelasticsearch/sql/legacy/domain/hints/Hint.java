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

package com.amazon.opendistroforelasticsearch.sql.legacy.domain.hints;

/**
 * Created by Eliran on 5/9/2015.
 */
public class Hint {
    private HintType type;
    private Object[] params;

    public Hint(HintType type, Object[] params) {
        this.type = type;
        this.params = params;
    }

    public HintType getType() {
        return type;
    }

    public Object[] getParams() {
        return params;
    }
}
