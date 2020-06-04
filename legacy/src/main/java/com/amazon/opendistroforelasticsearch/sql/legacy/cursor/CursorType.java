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

package com.amazon.opendistroforelasticsearch.sql.legacy.cursor;

import java.util.HashMap;
import java.util.Map;

/**
 * Different types queries for which cursor is supported.
 * The result execution, and cursor genreation/parsing will depend on the cursor type.
 * NullCursor is the placeholder implementation in case of non-cursor query.
 */
public enum CursorType {
    NULL(null),
    DEFAULT("d"),
    AGGREGATION("a"),
    JOIN("j");

    public String id;

    CursorType(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public static final Map<String, CursorType> LOOKUP = new HashMap<>();

    static {
        for (CursorType type : CursorType.values()) {
            LOOKUP.put(type.getId(), type);
        }
    }

    public static CursorType getById(String id) {
        return LOOKUP.getOrDefault(id, NULL);
    }
}