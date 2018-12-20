package org.nlpcn.es4sql;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchModule;
import org.json.JSONException;
import org.json.JSONObject;
import org.nlpcn.es4sql.exception.SqlParseException;

import java.io.IOException;
import java.util.Collections;

public class SqlRequest {

    String sql;
    JSONObject jsonContent;

    public SqlRequest(String sql, JSONObject jsonContent) {
        this.sql = sql;
        this.jsonContent = jsonContent;
    }

    private boolean isValidJson(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    public String getSql() { return this.sql; }
    public JSONObject getJsonContent() {
        return this.jsonContent;
    }

    /**
     * JSONObject's getJSONObject method will return just the value, this helper method is to extract the key and
     * value of 'filter' and return the JSON as a string.
     */
    private String getFilterObjectAsString(JSONObject jsonContent) {
        String filterVal = jsonContent.getJSONObject("filter").toString();
        return "{" + "\"filter\":" + filterVal + "}";
    }

    private boolean hasFilterInRequest() {
        return jsonContent != null && jsonContent.has("filter");
    }

    /**
     * Takes 'filter' parameter from JSON request if JSON request and 'filter' were given and creates a QueryBuilder
     * object out of it to add to the filterClauses of the BoolQueryBuilder.
     */
    private void addFilterFromJson(BoolQueryBuilder boolQuery) throws SqlParseException {
        try {
            String filter = getFilterObjectAsString(jsonContent);
            SearchModule searchModule = new SearchModule(Settings.EMPTY, false, Collections.emptyList());
            XContentParser parser = XContentFactory.xContent(XContentType.JSON).
                    createParser(new NamedXContentRegistry(searchModule.getNamedXContents()),
                            LoggingDeprecationHandler.INSTANCE,
                            filter);

            // nextToken is called before passing the parser to fromXContent since the fieldName will be null if the
            // first token it parses is START_OBJECT resulting in an exception
            parser.nextToken();
            boolQuery.filter(BoolQueryBuilder.fromXContent(parser));
        } catch (IOException e) {
            throw new SqlParseException("Unable to parse 'filter' in JSON request: " + e.getMessage());
        }

    }

    public BoolQueryBuilder checkAndAddFilter(BoolQueryBuilder boolQuery) throws SqlParseException {
        if (hasFilterInRequest()) {
            // if WHERE was not given, create a new BoolQuery to add "filter" to
            boolQuery = boolQuery == null ? new BoolQueryBuilder() : boolQuery;
            addFilterFromJson(boolQuery);
        }
        return boolQuery;
    }
}
