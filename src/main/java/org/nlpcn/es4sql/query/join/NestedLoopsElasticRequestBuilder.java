package org.nlpcn.es4sql.query.join;


import com.unboundid.util.json.JSONString;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.nlpcn.es4sql.domain.Condition;
import org.nlpcn.es4sql.domain.Field;
import org.nlpcn.es4sql.domain.Where;
import org.nlpcn.es4sql.exception.SqlParseException;

import org.nlpcn.es4sql.query.maker.Maker;
import org.nlpcn.es4sql.query.maker.QueryMaker;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            Where where = (Where)this.connectedWhere.clone();
            setValueTypeConditionToStringRecursive(where);
            if (where != null) {
                conditions = QueryMaker.explan(where, false).toString();
            }
        } catch (CloneNotSupportedException | SqlParseException e) {
            conditions = "Could not parse conditions due to " + e.getMessage();
        }

        String desc =  "Nested Loops run first query, and for each result run second query with additional conditions as following.";
        String[] queries = explainNL();
        JSONStringer jsonStringer = new JSONStringer();
        jsonStringer.object().key("description").value(desc).key("conditions").value(new JSONObject(conditions)).
                key("first query").value(new JSONObject(queries[0])).key("second query").value(new JSONObject(queries[1])).endObject();
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
        if (where == null) return;
        if (where instanceof Condition) {
            Condition c = (Condition) where;
            c.setValue(c.getValue().toString());
            return;
        }
        else {
            for (Where innerWhere : where.getWheres())
                setValueTypeConditionToStringRecursive(innerWhere);
        }
    }

    private String[] explainNL() {
        return new String[] {explainQuery(this.getFirstTable()), explainQuery(this.getSecondTable())};
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
