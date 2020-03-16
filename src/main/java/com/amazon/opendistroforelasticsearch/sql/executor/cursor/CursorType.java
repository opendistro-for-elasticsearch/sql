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

package com.amazon.opendistroforelasticsearch.sql.executor.cursor;

import java.util.HashMap;
import java.util.Map;

public enum CursorType {
    DEFAULT(0),
    AGGREGATION(10),
    JOIN(20);

    private int value;

    private static final Map<Integer, CursorType> NUMERIC_CURSOR_MAP = new HashMap<>();
    static {
        for (CursorType type : values()) {
            NUMERIC_CURSOR_MAP.put(type.getValue(), type);
        }
    }

    CursorType (int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * Return one of the choice of the enum by its value.
     * May return null if there is no choice for this value.
     * @param value value
     * @return CursorType
     */
    public static CursorType cursorTypeFromValue(int value) {
        return NUMERIC_CURSOR_MAP.get(value);
    }
}