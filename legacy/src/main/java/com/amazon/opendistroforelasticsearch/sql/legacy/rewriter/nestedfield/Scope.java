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

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import static com.alibaba.druid.sql.ast.statement.SQLJoinTableSource.JoinType;

/**
 * Nested field information in current query being visited.
 */
class Scope {

    /** Join Type as passed in the actual SQL subquery */
    private JoinType actualJoinType;

    /** Alias of parent such as alias "t" of parent table "team" in "FROM team t, t.employees e" */

    private String parentAlias;

    /**
     * Mapping from nested field path alias to path full name in FROM.
     * eg. e in {e => employees} in "FROM t.employees e"
     */
    private Map<String, String> aliasFullPaths = new HashMap<>();

    /**
     * Mapping from binary operation condition (in WHERE) to nested
     * field tag (full path for nested, EMPTY for non-nested field)
     */
    private Map<SQLBinaryOpExpr, String> conditionTags = new IdentityHashMap<>();

    String getParentAlias() {
        return parentAlias;
    }

    void setParentAlias(String parentAlias) {
        this.parentAlias = parentAlias;
    }

    void addAliasFullPath(String alias, String path) {
        if (alias.isEmpty()) {
            aliasFullPaths.put(path, path);
        } else {
            aliasFullPaths.put(alias, path);
        }
    }

    String getFullPath(String alias) {
        return aliasFullPaths.getOrDefault(alias, "");
    }

    boolean isAnyNestedField() {
        return !aliasFullPaths.isEmpty();
    }

    Set<String> getAliases() {
        return aliasFullPaths.keySet();
    }

    String getConditionTag(SQLBinaryOpExpr expr) {
        return conditionTags.getOrDefault(expr, "");
    }

    void addConditionTag(SQLBinaryOpExpr expr, String tag) {
        conditionTags.put(expr, tag);
    }

    JoinType getActualJoinType() {
        return actualJoinType;
    }

    void setActualJoinType(JoinType joinType) {
        actualJoinType = joinType;
    }
}
