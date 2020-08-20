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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.node.scroll;

import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.Row;
import com.google.common.base.Strings;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.search.SearchHit;

import java.util.HashMap;
import java.util.Map;

/**
 * Search hit row that implements basic accessor for SearchHit.
 * Encapsulate all ES specific knowledge: how to parse source including nested path.
 * <p>
 * State transition:
 * for example, SELECT e.name.first AS firstName, e.age AS age FROM E e JOIN D d ON ... ORDER BY ...
 * <p>
 * Stage               | hit.source                                               | tableAlias | Passed in args
 * ----------------------------------------------------------------------------------------------------------------------
 * new in Scroll       | {"name":{"first": "Allen", "last": "Hank"}, "age": 30}   | "e"|  new(SearchHit, "e")
 * ----------------------------------------------------------------------------------------------------------------------
 * key()/combine()     |                                                          |    | key("name.first", "age")
 * in JoinAlgorithm    | {"e.name": {...}, "e.age": 30, "d..." } (after combined) | "" | combine(row of D)
 * ----------------------------------------------------------------------------------------------------------------------
 * key() in XXSort     | same                                                     | "" | key("e.name.first", "e.age")
 * ----------------------------------------------------------------------------------------------------------------------
 * retain() in Project | {"firstName": "Allen", "age": 30 }                       | "" | retain("e.name.first", "e.age")
 * ----------------------------------------------------------------------------------------------------------------------
 */
class SearchHitRow implements Row<SearchHit> {

    /**
     * Native ES data object for each row
     */
    private final SearchHit hit;

    /**
     * Column and value pairs
     */
    private final Map<String, Object> source;

    /**
     * Table alias owned the row. Empty if this row comes from combination of two other rows
     */
    private final String tableAlias;

    SearchHitRow(SearchHit hit, String tableAlias) {
        this.hit = hit;
        this.source = hit.getSourceAsMap();
        this.tableAlias = tableAlias;
    }

    @Override
    public RowKey key(String[] colNames) {
        if (colNames.length == 0) {
            return RowKey.NULL;
        }

        Object[] keys = new Object[colNames.length];
        for (int i = 0; i < colNames.length; i++) {
            keys[i] = getValueOfPath(colNames[i]);

            if (keys[i] == null) {
                return RowKey.NULL;
            }
        }
        return new RowKey(keys);
    }

    /**
     * Replace column name by full name to avoid naming conflicts.
     * For efficiency, this only happens here when matched rows found.
     * Create a new one to avoid mutating the original ones in hash table which impact subsequent match.
     */
    @Override
    public Row<SearchHit> combine(Row<SearchHit> other) {
        SearchHit combined = cloneHit(other);

        collectFullName(combined.getSourceAsMap(), this);
        if (other != NULL) {
            collectFullName(combined.getSourceAsMap(), (SearchHitRow) other);
        }
        return new SearchHitRow(combined, "");
    }

    @Override
    public void retain(Map<String, String> colNameAlias) {
        Map<String, Object> aliasSource = new HashMap<>();
        colNameAlias.forEach((colName, alias) -> {
            if (colName.endsWith(".*")) {
                String tableAlias = colName.substring(0, colName.length() - 2) + ".";
                retainAllFieldsFromTable(aliasSource, tableAlias);
            } else {
                retainOneField(aliasSource, colName, alias);
            }
        });
        resetSource(aliasSource);
    }

    @Override
    public SearchHit data() {
        return hit;
    }

    @Override
    public String toString() {
        return "SearchHitRow{" + "hit=" + source + '}';
    }

    private Object getValueOfPath(String path) {
        /*
         * If table alias is missing which means the row was generated by combine().
         * In this case, table alias is present and the first dot should be ignored, ex. "e.name.first"
         */
        return getValueOfPath(source, path, Strings.isNullOrEmpty(tableAlias));
    }

    /**
     * Recursively get value for field name path, such as object field a.b.c
     */
    private Object getValueOfPath(Object source, String path, boolean isIgnoreFirstDot) {
        if (!(source instanceof Map) || path.isEmpty()) {
            return source;
        }

        int dot = path.indexOf('.', (isIgnoreFirstDot ? path.indexOf('.') + 1 : 0));
        if (dot == -1) {
            return ((Map) source).get(path);
        }
        return getValueOfPath(
                ((Map) source).get(path.substring(0, dot)),
                path.substring(dot + 1),
                false
        );
    }

    private SearchHit cloneHit(Row<SearchHit> other) {
        Map<String, DocumentField> documentFields = new HashMap<>();
        Map<String, DocumentField> metaFields = new HashMap<>();
        hit.getFields().forEach((fieldName, docField) ->
            (MapperService.META_FIELDS_BEFORE_7DOT8.contains(fieldName) ? metaFields : documentFields).put(fieldName, docField));
        SearchHit combined = new SearchHit(
                hit.docId(),
                hit.getId() + "|" + (other == NULL ? "0" : ((SearchHitRow) other).hit.getId()),
                new Text(
                        hit.getType() + "|" + (other == NULL ? null : ((SearchHitRow) other).hit.getType())
                ),
                documentFields,
                metaFields
        );
        combined.sourceRef(hit.getSourceRef());
        combined.getSourceAsMap().clear();
        return combined;
    }

    private void collectFullName(Map<String, Object> newSource, SearchHitRow row) {
        row.source.forEach((colName, value) -> newSource.put(row.tableAlias + "." + colName, value));
    }

    private void retainAllFieldsFromTable(Map<String, Object> aliasSource, String tableAlias) {
        source.entrySet().
                stream().
                filter(e -> e.getKey().startsWith(tableAlias)).
                forEach(e -> aliasSource.put(e.getKey(), e.getValue()));
    }

    /**
     * Note that column here is already prefixed by table alias after combine().
     * <p>
     * Meanwhile check if column name with table alias prefix, ex. a.name, is property, namely a.name.lastname.
     * In this case, split by first second dot and continue searching for the final value in nested map
     * by getValueOfPath(source.get("a.name"), "lastname")
     */
    private void retainOneField(Map<String, Object> aliasSource, String colName, String alias) {
        aliasSource.put(
                Strings.isNullOrEmpty(alias) ? colName : alias,
                getValueOfPath(colName)
        );
    }

    private void resetSource(Map<String, Object> newSource) {
        source.clear();
        source.putAll(newSource);
    }
}
