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

package com.amazon.opendistroforelasticsearch.sql.legacy.executor.format;

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Delete;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DeleteResultSet extends ResultSet {
    private Delete query;
    private Object queryResult;

    public static final String DELETED = "deleted_rows";

    public DeleteResultSet(Client client, Delete query, Object queryResult) {
        this.client = client;
        this.query = query;
        this.queryResult = queryResult;
        this.schema = new Schema(loadColumns());
        this.dataRows = new DataRows(loadRows());
    }

    private List<Schema.Column> loadColumns() {
        return Collections.singletonList(new Schema.Column(DELETED, null, Schema.Type.LONG));
    }

    private List<DataRows.Row> loadRows() {
        return Collections.singletonList(new DataRows.Row(loadDeletedData()));
    }

    private Map<String, Object> loadDeletedData(){
        return Collections.singletonMap(DELETED, ((BulkByScrollResponse) queryResult).getDeleted());
    }
}
