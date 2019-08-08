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

package com.amazon.opendistroforelasticsearch.sql.rewriter.nestedfield;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import com.amazon.opendistroforelasticsearch.sql.domain.Field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;

/**
 * Nested field projection class to make ES return matched rows in nested field.
 */
public class NestedFieldProjection {

    private final SearchRequestBuilder request;
    private static final String SEPARATOR = ".";
    private String lastFieldInPath = "";
    private List<String> lastFieldNames = new ArrayList<>();

    public NestedFieldProjection(SearchRequestBuilder request) {
        this.request = request;
    }

    /**
     * Project nested field in SELECT clause to InnerHit in NestedQueryBuilder
     * @param fields    list of field domain object
     */
    public void project(List<Field> fields) {
        if (isAnyNestedField(fields)) {
            initBoolQueryFilterIfNull();
            // Extract nested query from WHERE clause
            List<NestedQueryBuilder> nestedQueries = extractNestedQueries(query());
            groupFieldNamesByPath(fields).forEach(
                (path, fieldNames) -> buildInnerHit(fieldNames, findNestedQueryWithSamePath(nestedQueries, path, fieldNames))
            );
        }
    }

    /** Reset values of lastFieldInPath and lastFieldNames to prepare for next check */
    private void setLastValues(String path, List<String> fieldNames) {
        int pos = path.lastIndexOf(SEPARATOR);
        lastFieldInPath = path.substring(pos + 1);
        lastFieldNames = fieldNames;
    }

    /** Check via traditional for loop first to avoid lambda performance impact on all queries even though those without nested field */
    private boolean isAnyNestedField(List<Field> fields) {
        for (Field field : fields) {
            if (field.isNested() && !field.isReverseNested()) {
                return true;
            }
        }
        return false;
    }

    private void initBoolQueryFilterIfNull() {
        if (request.request().source() == null || query() == null) {
            request.setQuery(boolQuery());
        }
        if (query().filter().isEmpty()) {
            query().filter(boolQuery());
        }
    }

    private Map<String, List<String>> groupFieldNamesByPath(List<Field> fields) {
        return fields.stream().
                      filter(Field::isNested).
                      filter(not(Field::isReverseNested)).
                      collect(groupingBy(Field::getNestedPath, () -> new TreeMap<>(Collections.reverseOrder()), mapping(Field::getName, toList())));
    }

    /**
     * Why search for NestedQueryBuilder recursively?
     * Because 1) it was added and wrapped by BoolQuery when WHERE explained (far from here)
     *         2) InnerHit must be added to the NestedQueryBuilder related
     *
     * Either we store it to global data structure (which requires to be thread-safe or ThreadLocal)
     * or we peel off BoolQuery to find it (the way we followed here because recursion tree should be very thin).
     */
    private List<NestedQueryBuilder> extractNestedQueries(QueryBuilder query) {
        List<NestedQueryBuilder> result = new ArrayList<>();
        if (query instanceof NestedQueryBuilder) {
            result.add((NestedQueryBuilder) query);
        }
        else if (query instanceof BoolQueryBuilder) {
            BoolQueryBuilder boolQ = (BoolQueryBuilder) query;
            Stream.of(boolQ.filter(), boolQ.must(), boolQ.should()).
                   flatMap(Collection::stream).
                   forEach(q -> result.addAll(extractNestedQueries(q)));
        }
        return result;
    }

    private void buildInnerHit(List<String> fieldNames, NestedQueryBuilder query) {
        query.innerHit(new InnerHitBuilder().setFetchSourceContext(
            new FetchSourceContext(true, fieldNames.toArray(new String[0]), null)
        ));
    }

    /**
     * Why linear search? Because NestedQueryBuilder hides "path" field from any access.
     * Assumption: collected NestedQueryBuilder list should be very small or mostly only one.
     */
    private NestedQueryBuilder findNestedQueryWithSamePath(
            List<NestedQueryBuilder> nestedQueries, String path, List<String> fieldNames) {

        return nestedQueries.stream().
                filter(query -> isSamePath(path, query, fieldNames)).
                findAny().
                orElseGet(createNestedQuery(path, fieldNames));
    }

    /** Check against NestedQuery of WHERE condition */
    private boolean isSamePath(String path, NestedQueryBuilder query, List<String> fieldNames) {
        boolean isSamePath = nestedQuery(path, query.query(), query.scoreMode()).equals(query);
        if (isSamePath) {
           setLastValues(path, fieldNames);
        }
        return isSamePath;
    }

    /**
     * Since groupFieldNamesByPath() uses TreeMap to maintain reverse alphabetical order. Here we maintain previous
     * path and fieldName value to compare with current nested query.
     * */
    private boolean hasSameParent(String path, QueryBuilder query) {
        // query could also be termQueryBuilder
        if (!lastFieldInPath.isEmpty() && (query instanceof NestedQueryBuilder)) {
            NestedQueryBuilder nestedQuery = (NestedQueryBuilder) query;

            String finalPath = path + "." + lastFieldInPath;
            NestedQueryBuilder tmp = nestedQuery(finalPath, nestedQuery.query(), nestedQuery.scoreMode());
            buildInnerHit(lastFieldNames, tmp);
            return tmp.equals(query);
        }
        return false;
    }

    /** Create a nested query with match all filter to place inner hits */
    private Supplier<NestedQueryBuilder> createNestedQuery(String path, List<String> fieldNames) {
        return () -> {
            NestedQueryBuilder newNestedQuery;
            List<QueryBuilder> mustList = ((BoolQueryBuilder) query().filter().get(0)).must();
            int lastIndex = mustList.size() - 1;

            if (mustList.isEmpty() || !hasSameParent(path, mustList.get(lastIndex))) {
                // create empty nested query
                newNestedQuery = nestedQuery(path, matchAllQuery(), ScoreMode.None);
            } else {
                // create a nested query containing another nested query
                NestedQueryBuilder oldNestedQuery = (NestedQueryBuilder) mustList.get(lastIndex);
                newNestedQuery = nestedQuery(path, oldNestedQuery, ScoreMode.None);

                // remove outdated nested query
                mustList.remove(lastIndex);
            }

            setLastValues(path, fieldNames);
            // include newly-built nestedQuery in must
            ((BoolQueryBuilder) query().filter().get(0)).must(newNestedQuery);
            return newNestedQuery;
        };
    }

    private BoolQueryBuilder query() {
        return (BoolQueryBuilder) request.request().source().query();
    }

    private <T> Predicate<T> not(Predicate<T> predicate) {
        return predicate.negate();
    }

}
