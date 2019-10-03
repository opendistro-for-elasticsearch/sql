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

package com.amazon.opendistroforelasticsearch.sql.antlr.semantic.visitor;

import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.scope.SemanticContext;
import com.amazon.opendistroforelasticsearch.sql.antlr.visitor.GenericSqlParseTreeVisitor;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;

/**
 * Load index and nested field mapping into semantic context
 */
public class ESMappingLoader implements GenericSqlParseTreeVisitor<Void> {

    private final SemanticContext context;

    private final LocalClusterState clusterState;

    public ESMappingLoader(SemanticContext context, LocalClusterState clusterState) {
        this.context = context;
        this.clusterState = clusterState;
    }

    @Override
    public Void visitIndexName(String indexName, String alias) {
        return null;
    }
}
