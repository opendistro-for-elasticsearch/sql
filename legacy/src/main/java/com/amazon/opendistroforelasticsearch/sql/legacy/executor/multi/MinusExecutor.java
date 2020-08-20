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

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Condition;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.hints.Hint;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.hints.HintType;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.ElasticHitsExecutor;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.join.ElasticUtils;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.DefaultQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.multi.MultiQueryRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.Util;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.TotalHits.Relation;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * Created by Eliran on 26/8/2016.
 */
public class MinusExecutor implements ElasticHitsExecutor {
    private Client client;
    private MultiQueryRequestBuilder builder;
    private SearchHits minusHits;
    private boolean useTermsOptimization;
    private boolean termsOptimizationWithToLower;
    private boolean useScrolling;
    private int maxDocsToFetchOnFirstTable;
    private int maxDocsToFetchOnSecondTable;
    private int maxDocsToFetchOnEachScrollShard;
    private String[] fieldsOrderFirstTable;
    private String[] fieldsOrderSecondTable;
    private String seperator;

    public MinusExecutor(Client client, MultiQueryRequestBuilder builder) {
        this.client = client;
        this.builder = builder;
        this.useTermsOptimization = false;
        this.termsOptimizationWithToLower = false;
        this.useScrolling = false;
        parseHintsIfAny(builder.getOriginalSelect(true).getHints());
        fillFieldsOrder();
        seperator = UUID.randomUUID().toString();
    }

    @Override
    public void run() throws SqlParseException {
        if (this.useTermsOptimization && this.fieldsOrderFirstTable.length != 1) {
            throw new SqlParseException(
                    "Terms optimization failed: terms optimization for minus execution is supported with one field");
        }
        if (this.useTermsOptimization && !this.useScrolling) {
            throw new SqlParseException(
                    "Terms optimization failed: using scrolling is required for terms optimization");
        }
        if (!this.useScrolling || !this.useTermsOptimization) {
            Set<ComperableHitResult> comperableHitResults;
            if (!this.useScrolling) {
                //1. get results from first search , put in set
                //2. get reults from second search
                //2.1 for each result remove from set
                comperableHitResults = simpleOneTimeQueryEach();
            } else {
                //if scrolling
                //1. get all results in scrolls (till some limit) . put on set
                //2. scroll on second table
                //3. on each scroll result remove items from set
                comperableHitResults = runWithScrollings();
            }
            fillMinusHitsFromResults(comperableHitResults);
            return;
        } else {
            //if scrolling and optimization
            // 0. save the original second table where , init set
            // 1. on each scroll on first table , create miniSet
            //1.1 build where from all results (terms filter) , and run query
            //1.1.1 on each result remove from miniSet
            //1.1.2 add all results left from miniset to bigset
            Select firstSelect = this.builder.getOriginalSelect(true);
            MinusOneFieldAndOptimizationResult optimizationResult =
                    runWithScrollingAndAddFilter(fieldsOrderFirstTable[0], fieldsOrderSecondTable[0]);
            String fieldName = getFieldName(firstSelect.getFields().get(0));
            Set<Object> results = optimizationResult.getFieldValues();
            SearchHit someHit = optimizationResult.getSomeHit();
            fillMinusHitsFromOneField(fieldName, results, someHit);

        }

    }


    @Override
    public SearchHits getHits() {
        return this.minusHits;
    }

    private void fillMinusHitsFromOneField(String fieldName, Set<Object> fieldValues, SearchHit someHit) {
        List<SearchHit> minusHitsList = new ArrayList<>();
        int currentId = 1;
        for (Object result : fieldValues) {
            Map<String, DocumentField> fields = new HashMap<>();
            ArrayList<Object> values = new ArrayList<>();
            values.add(result);
            fields.put(fieldName, new DocumentField(fieldName, values));
            Map<String, DocumentField> documentFields = new HashMap<>();
            Map<String, DocumentField> metaFields = new HashMap<>();
            someHit.getFields().forEach((field, docField) ->
                (MapperService.META_FIELDS_BEFORE_7DOT8.contains(field) ? metaFields : documentFields).put(field, docField));
            SearchHit searchHit = new SearchHit(currentId, currentId + "", new Text(someHit.getType()),
                    documentFields, metaFields);
            searchHit.sourceRef(someHit.getSourceRef());
            searchHit.getSourceAsMap().clear();
            Map<String, Object> sourceAsMap = new HashMap<>();
            sourceAsMap.put(fieldName, result);
            searchHit.getSourceAsMap().putAll(sourceAsMap);
            currentId++;
            minusHitsList.add(searchHit);
        }
        int totalSize = currentId - 1;
        SearchHit[] unionHitsArr = minusHitsList.toArray(new SearchHit[totalSize]);
        this.minusHits = new SearchHits(unionHitsArr, new TotalHits(totalSize, Relation.EQUAL_TO), 1.0f);
    }

    private void fillMinusHitsFromResults(Set<ComperableHitResult> comperableHitResults) {
        int currentId = 1;
        List<SearchHit> minusHitsList = new ArrayList<>();
        for (ComperableHitResult result : comperableHitResults) {
            ArrayList<Object> values = new ArrayList<>();
            values.add(result);
            SearchHit originalHit = result.getOriginalHit();
            Map<String, DocumentField> documentFields = new HashMap<>();
            Map<String, DocumentField> metaFields = new HashMap<>();
            originalHit.getFields().forEach((fieldName, docField) ->
                (MapperService.META_FIELDS_BEFORE_7DOT8.contains(fieldName) ? metaFields : documentFields).put(fieldName, docField));
            SearchHit searchHit = new SearchHit(currentId, originalHit.getId(), new Text(originalHit.getType()),
                    documentFields, metaFields);
            searchHit.sourceRef(originalHit.getSourceRef());
            searchHit.getSourceAsMap().clear();
            Map<String, Object> sourceAsMap = result.getFlattenMap();
            for (Map.Entry<String, String> entry : this.builder.getFirstTableFieldToAlias().entrySet()) {
                if (sourceAsMap.containsKey(entry.getKey())) {
                    Object value = sourceAsMap.get(entry.getKey());
                    sourceAsMap.remove(entry.getKey());
                    sourceAsMap.put(entry.getValue(), value);
                }
            }

            searchHit.getSourceAsMap().putAll(sourceAsMap);
            currentId++;
            minusHitsList.add(searchHit);
        }
        int totalSize = currentId - 1;
        SearchHit[] unionHitsArr = minusHitsList.toArray(new SearchHit[totalSize]);
        this.minusHits = new SearchHits(unionHitsArr, new TotalHits(totalSize, Relation.EQUAL_TO), 1.0f);
    }

    private Set<ComperableHitResult> runWithScrollings() {

        SearchResponse scrollResp = ElasticUtils.scrollOneTimeWithHits(this.client,
                this.builder.getFirstSearchRequest(),
                builder.getOriginalSelect(true), this.maxDocsToFetchOnEachScrollShard);
        Set<ComperableHitResult> results = new HashSet<>();

        SearchHit[] hits = scrollResp.getHits().getHits();
        if (hits == null || hits.length == 0) {
            return new HashSet<>();
        }
        int totalDocsFetchedFromFirstTable = 0;

        //fetch from first table . fill set.
        while (hits != null && hits.length != 0) {
            totalDocsFetchedFromFirstTable += hits.length;
            fillComperableSetFromHits(this.fieldsOrderFirstTable, hits, results);
            if (totalDocsFetchedFromFirstTable > this.maxDocsToFetchOnFirstTable) {
                break;
            }
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
                    .setScroll(new TimeValue(600000)).execute().actionGet();
            hits = scrollResp.getHits().getHits();
        }
        scrollResp = ElasticUtils.scrollOneTimeWithHits(this.client, this.builder.getSecondSearchRequest(),
                builder.getOriginalSelect(false), this.maxDocsToFetchOnEachScrollShard);


        hits = scrollResp.getHits().getHits();
        if (hits == null || hits.length == 0) {
            return results;
        }
        int totalDocsFetchedFromSecondTable = 0;
        while (hits != null && hits.length != 0) {
            totalDocsFetchedFromSecondTable += hits.length;
            removeValuesFromSetAccordingToHits(this.fieldsOrderSecondTable, results, hits);
            if (totalDocsFetchedFromSecondTable > this.maxDocsToFetchOnSecondTable) {
                break;
            }
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
                    .setScroll(new TimeValue(600000)).execute().actionGet();
            hits = scrollResp.getHits().getHits();
        }

        return results;
    }

    private Set<ComperableHitResult> simpleOneTimeQueryEach() {
        SearchHit[] firstTableHits = this.builder.getFirstSearchRequest().get().getHits().getHits();
        if (firstTableHits == null || firstTableHits.length == 0) {
            return new HashSet<>();
        }

        Set<ComperableHitResult> result = new HashSet<>();
        fillComperableSetFromHits(this.fieldsOrderFirstTable, firstTableHits, result);
        SearchHit[] secondTableHits = this.builder.getSecondSearchRequest().get().getHits().getHits();
        if (secondTableHits == null || secondTableHits.length == 0) {
            return result;
        }
        removeValuesFromSetAccordingToHits(this.fieldsOrderSecondTable, result, secondTableHits);
        return result;
    }

    private void removeValuesFromSetAccordingToHits(String[] fieldsOrder,
                                                    Set<ComperableHitResult> set, SearchHit[] hits) {
        for (SearchHit hit : hits) {
            ComperableHitResult comperableHitResult = new ComperableHitResult(hit, fieldsOrder, this.seperator);
            if (!comperableHitResult.isAllNull()) {
                set.remove(comperableHitResult);
            }
        }
    }

    private void fillComperableSetFromHits(String[] fieldsOrder, SearchHit[] hits, Set<ComperableHitResult> setToFill) {
        for (SearchHit hit : hits) {
            ComperableHitResult comperableHitResult = new ComperableHitResult(hit, fieldsOrder, this.seperator);
            if (!comperableHitResult.isAllNull()) {
                setToFill.add(comperableHitResult);
            }
        }
    }

    private String getFieldName(Field field) {
        String alias = field.getAlias();
        if (alias != null && !alias.isEmpty()) {
            return alias;
        }
        return field.getName();
    }

    private boolean checkIfOnlyOneField(Select firstSelect, Select secondSelect) {
        return firstSelect.getFields().size() == 1 && secondSelect.getFields().size() == 1;
    }


    // 0. save the original second table where , init set
    // 1. on each scroll on first table , create miniSet
    //1.1 build where from all results (terms filter) , and run query
    //1.1.1 on each result remove from miniSet
    //1.1.2 add all results left from miniset to bigset
    private MinusOneFieldAndOptimizationResult runWithScrollingAndAddFilter(String firstFieldName,
                                                                            String secondFieldName)
            throws SqlParseException {
        SearchResponse scrollResp = ElasticUtils.scrollOneTimeWithHits(this.client,
                this.builder.getFirstSearchRequest(),
                builder.getOriginalSelect(true), this.maxDocsToFetchOnEachScrollShard);
        Set<Object> results = new HashSet<>();
        int currentNumOfResults = 0;
        SearchHit[] hits = scrollResp.getHits().getHits();
        SearchHit someHit = null;
        if (hits.length != 0) {
            //we need some hit for creating InnerResults.
            someHit = hits[0];
        }
        int totalDocsFetchedFromFirstTable = 0;
        int totalDocsFetchedFromSecondTable = 0;
        Where originalWhereSecondTable = this.builder.getOriginalSelect(false).getWhere();
        while (hits.length != 0) {
            totalDocsFetchedFromFirstTable += hits.length;
            Set<Object> currentSetFromResults = new HashSet<>();
            fillSetFromHits(firstFieldName, hits, currentSetFromResults);
            //fetch from second
            Select secondQuerySelect = this.builder.getOriginalSelect(false);
            Where where = createWhereWithOrigianlAndTermsFilter(secondFieldName, originalWhereSecondTable,
                    currentSetFromResults);
            secondQuerySelect.setWhere(where);
            DefaultQueryAction queryAction = new DefaultQueryAction(this.client, secondQuerySelect);
            queryAction.explain();
            if (totalDocsFetchedFromSecondTable > this.maxDocsToFetchOnSecondTable) {
                break;
            }
            SearchResponse responseForSecondTable = ElasticUtils.scrollOneTimeWithHits(this.client,
                    queryAction.getRequestBuilder(), secondQuerySelect, this.maxDocsToFetchOnEachScrollShard);
            SearchHits secondQuerySearchHits = responseForSecondTable.getHits();

            SearchHit[] secondQueryHits = secondQuerySearchHits.getHits();
            while (secondQueryHits.length > 0) {
                totalDocsFetchedFromSecondTable += secondQueryHits.length;
                removeValuesFromSetAccordingToHits(secondFieldName, currentSetFromResults, secondQueryHits);
                if (totalDocsFetchedFromSecondTable > this.maxDocsToFetchOnSecondTable) {
                    break;
                }
                responseForSecondTable = client.prepareSearchScroll(responseForSecondTable.getScrollId())
                        .setScroll(new TimeValue(600000)).execute().actionGet();
                secondQueryHits = responseForSecondTable.getHits().getHits();
            }
            results.addAll(currentSetFromResults);
            if (totalDocsFetchedFromFirstTable > this.maxDocsToFetchOnFirstTable) {
                System.out.println("too many results for first table, stoping at:" + totalDocsFetchedFromFirstTable);
                break;
            }

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
                    .setScroll(new TimeValue(600000)).execute().actionGet();
            hits = scrollResp.getHits().getHits();
        }
        return new MinusOneFieldAndOptimizationResult(results, someHit);


    }

    private void removeValuesFromSetAccordingToHits(String fieldName, Set<Object> setToRemoveFrom, SearchHit[] hits) {
        for (SearchHit hit : hits) {
            Object fieldValue = getFieldValue(hit, fieldName);
            if (fieldValue != null) {
                if (setToRemoveFrom.contains(fieldValue)) {
                    setToRemoveFrom.remove(fieldValue);
                }
            }
        }
    }

    private void fillSetFromHits(String fieldName, SearchHit[] hits, Set<Object> setToFill) {
        for (SearchHit hit : hits) {
            Object fieldValue = getFieldValue(hit, fieldName);
            if (fieldValue != null) {
                setToFill.add(fieldValue);
            }
        }
    }

    private Where createWhereWithOrigianlAndTermsFilter(String secondFieldName, Where originalWhereSecondTable,
                                                        Set<Object> currentSetFromResults) throws SqlParseException {
        Where where = Where.newInstance();
        where.setConn(Where.CONN.AND);
        where.addWhere(originalWhereSecondTable);
        where.addWhere(buildTermsFilterFromResults(currentSetFromResults, secondFieldName));
        return where;
    }

    private Where buildTermsFilterFromResults(Set<Object> results, String fieldName) throws SqlParseException {
        return new Condition(Where.CONN.AND, fieldName, null, Condition.OPERATOR.IN_TERMS, results.toArray(), null);
    }

    private Object getFieldValue(SearchHit hit, String fieldName) {
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        if (fieldName.contains(".")) {
            String[] split = fieldName.split("\\.");
            return Util.searchPathInMap(sourceAsMap, split);
        } else if (sourceAsMap.containsKey(fieldName)) {
            return sourceAsMap.get(fieldName);
        }
        return null;
    }

    private void fillFieldsOrder() {
        List<String> fieldsOrAliases = new ArrayList<>();
        Map<String, String> firstTableFieldToAlias = this.builder.getFirstTableFieldToAlias();
        List<Field> firstTableFields = this.builder.getOriginalSelect(true).getFields();

        for (Field field : firstTableFields) {
            if (firstTableFieldToAlias.containsKey(field.getName())) {
                fieldsOrAliases.add(field.getAlias());
            } else {
                fieldsOrAliases.add(field.getName());
            }
        }
        Collections.sort(fieldsOrAliases);

        int fieldsSize = fieldsOrAliases.size();
        this.fieldsOrderFirstTable = new String[fieldsSize];
        fillFieldsArray(fieldsOrAliases, firstTableFieldToAlias, this.fieldsOrderFirstTable);
        this.fieldsOrderSecondTable = new String[fieldsSize];
        fillFieldsArray(fieldsOrAliases, this.builder.getSecondTableFieldToAlias(), this.fieldsOrderSecondTable);
    }

    private void fillFieldsArray(List<String> fieldsOrAliases, Map<String, String> fieldsToAlias, String[] fields) {
        Map<String, String> aliasToField = inverseMap(fieldsToAlias);
        for (int i = 0; i < fields.length; i++) {
            String field = fieldsOrAliases.get(i);
            if (aliasToField.containsKey(field)) {
                field = aliasToField.get(field);
            }
            fields[i] = field;
        }
    }

    private Map<String, String> inverseMap(Map<String, String> mapToInverse) {
        Map<String, String> inversedMap = new HashMap<>();
        for (Map.Entry<String, String> entry : mapToInverse.entrySet()) {
            inversedMap.put(entry.getValue(), entry.getKey());
        }
        return inversedMap;
    }

    private void parseHintsIfAny(List<Hint> hints) {
        if (hints == null) {
            return;
        }
        for (Hint hint : hints) {
            if (hint.getType() == HintType.MINUS_USE_TERMS_OPTIMIZATION) {
                Object[] params = hint.getParams();
                if (params != null && params.length == 1) {
                    this.termsOptimizationWithToLower = (boolean) params[0];
                }
            } else if (hint.getType() == HintType.MINUS_FETCH_AND_RESULT_LIMITS) {
                Object[] params = hint.getParams();
                this.useScrolling = true;
                this.maxDocsToFetchOnFirstTable = (int) params[0];
                this.maxDocsToFetchOnSecondTable = (int) params[1];
                this.maxDocsToFetchOnEachScrollShard = (int) params[2];
            }
        }
    }

}
