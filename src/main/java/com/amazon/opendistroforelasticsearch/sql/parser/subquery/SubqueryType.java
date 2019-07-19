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

package com.amazon.opendistroforelasticsearch.sql.parser.subquery;

public enum SubqueryType {
    /**
     * SELECT * FROM A WHERE a IN (SELECT * FROM B)
     */
    IN(0),
    UNSUPPORTED(32);

    private int v;

    SubqueryType(int v) {
        this.v = v;
    }

    public static boolean isSupported(SubqueryType subqueryType) {
        return subqueryType.v < SubqueryType.UNSUPPORTED.v;
    }
}
