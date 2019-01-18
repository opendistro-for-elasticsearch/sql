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

package com.amazon.opendistro.sql.executor.multi;

import com.amazon.opendistro.sql.executor.ElasticHitsExecutor;
import org.elasticsearch.client.Client;
import com.amazon.opendistro.sql.exception.SqlParseException;
import com.amazon.opendistro.sql.query.multi.MultiQueryRequestBuilder;

/**
 * Created by Eliran on 21/8/2016.
 */
public class MultiRequestExecutorFactory {
     public static ElasticHitsExecutor createExecutor(Client client, MultiQueryRequestBuilder builder) throws SqlParseException {
         switch (builder.getRelation()){
             case UNION_ALL:
             case UNION:
                 return new UnionExecutor(client,builder);
             case MINUS:
                 return new MinusExecutor(client,builder);
             default:
                 throw new SqlParseException("only supports union, and minus operations");
         }
     }
}
