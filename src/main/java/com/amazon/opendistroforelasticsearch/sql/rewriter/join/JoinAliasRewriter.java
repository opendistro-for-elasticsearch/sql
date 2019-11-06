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

package com.amazon.opendistroforelasticsearch.sql.rewriter.join;

import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.HashMap;
import java.util.Map;

public class JoinAliasRewriter extends MySqlASTVisitorAdapter {

    private final LocalClusterState clusterState;
    private final Multimap<String, Table> tableByFieldName = ArrayListMultimap.create();

    public JoinAliasRewriter(LocalClusterState clusterState) {
        this.clusterState = clusterState;
    }

    @Override
    public boolean visit(MySqlSelectQueryBlock query) {
        Map<String, String> indexToType = new HashMap<>();
//        collect(query.getFrom(), indexToType);
        return false;
    }





    private static class Table {

        public String getName() {
            return name;
        }

        public String getAlias() {
            return alias;
        }

        /**
         * Table Name.
         */
        private String name;

        /**
         * Table Alias.
         */
        private String alias;

        Table(String name, String alias) {
            this.name = name;
            this.alias = alias;
        }
    }
}
