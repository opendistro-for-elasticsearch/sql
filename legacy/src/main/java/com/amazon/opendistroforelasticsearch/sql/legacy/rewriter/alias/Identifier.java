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

package com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.alias;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;

/**
 * Util class for identifier expression parsing
 */
class Identifier {

    private final SQLIdentifierExpr idExpr;

    Identifier(SQLIdentifierExpr idExpr) {
        this.idExpr = idExpr;
    }

    String name() {
        return idExpr.getName();
    }

    boolean hasPrefix() {
        return firstDotIndex() != -1;
    }

    /** Assumption: identifier has prefix */
    String prefix() {
        return name().substring(0, firstDotIndex());
    }

    /** Assumption: identifier has prefix */
    void removePrefix() {
        String nameWithoutPrefix = name().substring(prefix().length() + 1);
        idExpr.setName(nameWithoutPrefix);
    }

    private int firstDotIndex() {
        return name().indexOf('.', 1);
    }
}
