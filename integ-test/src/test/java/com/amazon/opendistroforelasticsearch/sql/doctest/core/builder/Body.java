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

package com.amazon.opendistroforelasticsearch.sql.doctest.core.builder;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Request body.
 */
class Body {

    private final String[] fieldValues;

    /**
     * Request body built from field value pairs.
     * @param fieldValues   field and values in "'field": 'value'" format that can assemble to JSON directly
     */
    Body(String... fieldValues) {
        this.fieldValues = fieldValues;
    }

    @Override
    public String toString() {
        return Arrays.stream(fieldValues).collect(Collectors.joining(",", "{", "}"));
    }
}
