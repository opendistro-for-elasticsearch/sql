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

package com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.nestedfield;

import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;

import java.util.List;

/**
 * Column list in SELECT statement.
 */
class Select extends SQLClause<List<SQLSelectItem>> {

    Select(List<SQLSelectItem> expr) {
        super(expr);
    }

    /**
     * Rewrite by adding nested field to SELECT in the case of 'SELECT *'.
     * <p>
     * Ex. 'SELECT *' => 'SELECT *, employees.*'
     * So that NestedFieldProjection will add 'employees.*' to includes list in inner_hits.
     */
    @Override
    void rewrite(Scope scope) {
        if (isSelectAllOnly()) {
            addSelectAllForNestedField(scope);
        }
    }

    private boolean isSelectAllOnly() {
        return expr.size() == 1 && expr.get(0).getExpr() instanceof SQLAllColumnExpr;
    }

    private void addSelectAllForNestedField(Scope scope) {
        for (String alias : scope.getAliases()) {
            expr.add(createSelectItem(alias + ".*"));
        }
    }

    private SQLSelectItem createSelectItem(String name) {
        return new SQLSelectItem(new SQLIdentifierExpr(name));
    }
}
