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

package com.amazon.opendistroforelasticsearch.sql.legacy.executor.multi;

import com.alibaba.druid.sql.ast.statement.SQLUnionOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.SemanticAnalysisException;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.ElasticHitsExecutor;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.multi.MultiQueryRequestBuilder;
import org.elasticsearch.client.Client;

/**
 * Created by Eliran on 21/8/2016.
 */
public class MultiRequestExecutorFactory {
    public static ElasticHitsExecutor createExecutor(Client client, MultiQueryRequestBuilder builder) {
        SQLUnionOperator relation = builder.getRelation();
        switch (relation) {
            case UNION_ALL:
            case UNION:
                return new UnionExecutor(client, builder);
            case MINUS:
                return new MinusExecutor(client, builder);
            default:
                throw new SemanticAnalysisException("Unsupported operator: " + relation);
        }
    }
}
