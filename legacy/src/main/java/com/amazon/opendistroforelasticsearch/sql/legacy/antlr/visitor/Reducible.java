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

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr.visitor;

import java.util.List;

/**
 * Abstraction for anything that can be reduced and used by {@link AntlrSqlParseTreeVisitor}.
 */
public interface Reducible {

    /**
     * Reduce current and others to generate a new one
     * @param others  others
     * @return      reduction
     */
    <T extends Reducible> T reduce(List<T> others);

}
