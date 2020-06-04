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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.join;


import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Condition;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.maker.QueryMaker;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;

/**
 * Created by Eliran on 15/9/2015.
 */
public class NestedLoopsElasticRequestBuilder extends JoinRequestBuilder {

    private Where connectedWhere;
    private int multiSearchMaxSize;

    public NestedLoopsElasticRequestBuilder() {

        multiSearchMaxSize = 100;
    }

    @Override
    public String explain() {
        String conditions = "";

        try {
            Where where = (Where) this.connectedWhere.clone();
            setValueTypeConditionToStringRecursive(where);
            if (where != null) {
                conditions = QueryMaker.explain(where, false).toString();
            }
        } catch (CloneNotSupportedException | SqlParseException e) {
            conditions = "Could not parse conditions due to " + e.getMessage();
        }

        String desc = "Nested Loops run first query, and for each result run "
                + "second query with additional conditions as following.";
        String[] queries = explainNL();
        JSONStringer jsonStringer = new JSONStringer();
        jsonStringer.object().key("description").value(desc)
                .key("conditions").value(new JSONObject(conditions))
                .key("first query").value(new JSONObject(queries[0]))
                .key("second query").value(new JSONObject(queries[1])).endObject();
        return jsonStringer.toString();
    }

    public int getMultiSearchMaxSize() {
        return multiSearchMaxSize;
    }

    public void setMultiSearchMaxSize(int multiSearchMaxSize) {
        this.multiSearchMaxSize = multiSearchMaxSize;
    }

    public Where getConnectedWhere() {
        return connectedWhere;
    }

    public void setConnectedWhere(Where connectedWhere) {
        this.connectedWhere = connectedWhere;
    }

    private void setValueTypeConditionToStringRecursive(Where where) {
        if (where == null) {
            return;
        }
        if (where instanceof Condition) {
            Condition c = (Condition) where;
            c.setValue(c.getValue().toString());
            return;
        } else {
            for (Where innerWhere : where.getWheres()) {
                setValueTypeConditionToStringRecursive(innerWhere);
            }
        }
    }

    private String[] explainNL() {
        return new String[]{explainQuery(this.getFirstTable()), explainQuery(this.getSecondTable())};
    }

    private String explainQuery(TableInJoinRequestBuilder requestBuilder) {
        try {
            XContentBuilder xContentBuilder = XContentFactory.contentBuilder(XContentType.JSON).prettyPrint();
            requestBuilder.getRequestBuilder().request().source().toXContent(xContentBuilder, ToXContent.EMPTY_PARAMS);
            return BytesReference.bytes(xContentBuilder).utf8ToString();
        } catch (IOException e) {
            return e.getMessage();
        }
    }
}
