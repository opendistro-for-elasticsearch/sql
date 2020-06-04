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

package com.amazon.opendistroforelasticsearch.sql.legacy.query;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;

/**
 * Created by Eliran on 19/8/2015.
 */
public class SqlElasticDeleteByQueryRequestBuilder implements SqlElasticRequestBuilder {
    DeleteByQueryRequestBuilder deleteByQueryRequestBuilder;

    public SqlElasticDeleteByQueryRequestBuilder(DeleteByQueryRequestBuilder deleteByQueryRequestBuilder) {
        this.deleteByQueryRequestBuilder = deleteByQueryRequestBuilder;
    }

    @Override
    public ActionRequest request() {
        return deleteByQueryRequestBuilder.request();
    }

    @Override
    public String explain() {
        try {
            SearchRequestBuilder source = deleteByQueryRequestBuilder.source();
            return source.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ActionResponse get() {

        return this.deleteByQueryRequestBuilder.get();
    }

    @Override
    public ActionRequestBuilder getBuilder() {
        return deleteByQueryRequestBuilder;
    }

}
