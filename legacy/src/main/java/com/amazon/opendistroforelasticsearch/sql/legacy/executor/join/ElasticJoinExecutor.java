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

package com.amazon.opendistroforelasticsearch.sql.legacy.executor.join;

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.ElasticHitsExecutor;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.SqlElasticRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.join.HashJoinElasticRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.join.JoinRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.join.NestedLoopsElasticRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.join.TableInJoinRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.HashJoinQueryPlanRequestBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.TotalHits.Relation;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Eliran on 15/9/2015.
 */
public abstract class ElasticJoinExecutor implements ElasticHitsExecutor {
    private static final Logger LOG = LogManager.getLogger();

    protected List<SearchHit> results; // Keep list to avoid copy to new array in SearchHits
    protected MetaSearchResult metaResults;
    protected final int MAX_RESULTS_ON_ONE_FETCH = 10000;
    private Set<String> aliasesOnReturn;
    private boolean allFieldsReturn;

    protected ElasticJoinExecutor(JoinRequestBuilder requestBuilder) {
        metaResults = new MetaSearchResult();
        aliasesOnReturn = new HashSet<>();
        List<Field> firstTableReturnedField = requestBuilder.getFirstTable().getReturnedFields();
        List<Field> secondTableReturnedField = requestBuilder.getSecondTable().getReturnedFields();
        allFieldsReturn = (firstTableReturnedField == null || firstTableReturnedField.size() == 0)
                && (secondTableReturnedField == null || secondTableReturnedField.size() == 0);
    }

    public void sendResponse(RestChannel channel) throws IOException {
        XContentBuilder builder = null;
        long len;
        try {
            builder = ElasticUtils.hitsAsStringResultZeroCopy(results, metaResults, this);
            BytesRestResponse bytesRestResponse = new BytesRestResponse(RestStatus.OK, builder);
            len = bytesRestResponse.content().length();
            channel.sendResponse(bytesRestResponse);
        } catch (IOException e) {
            try {
                if (builder != null) {
                    builder.close();
                }
            } catch (Exception ex) {
                // Ignore. Already logged in channel
            }
            throw e;
        }
        LOG.debug("[MCB] Successfully send response with size of {}. Thread id = {}", len,
                Thread.currentThread().getId());
    }

    public void run() throws IOException, SqlParseException {
        long timeBefore = System.currentTimeMillis();
        results = innerRun();
        long joinTimeInMilli = System.currentTimeMillis() - timeBefore;
        this.metaResults.setTookImMilli(joinTimeInMilli);
    }


    protected abstract List<SearchHit> innerRun() throws IOException, SqlParseException;

    public SearchHits getHits() {
        return new SearchHits(results.toArray(new SearchHit[results.size()]), new TotalHits(results.size(),
                Relation.EQUAL_TO), 1.0f);
    }

    public static ElasticJoinExecutor createJoinExecutor(Client client, SqlElasticRequestBuilder requestBuilder) {
        if (requestBuilder instanceof HashJoinQueryPlanRequestBuilder) {
            return new QueryPlanElasticExecutor((HashJoinQueryPlanRequestBuilder) requestBuilder);
        } else if (requestBuilder instanceof HashJoinElasticRequestBuilder) {
            HashJoinElasticRequestBuilder hashJoin = (HashJoinElasticRequestBuilder) requestBuilder;
            return new HashJoinElasticExecutor(client, hashJoin);
        } else if (requestBuilder instanceof NestedLoopsElasticRequestBuilder) {
            NestedLoopsElasticRequestBuilder nestedLoops = (NestedLoopsElasticRequestBuilder) requestBuilder;
            return new NestedLoopsElasticExecutor(client, nestedLoops);
        } else {
            throw new RuntimeException("Unsuported requestBuilder of type: " + requestBuilder.getClass());
        }
    }

    protected void mergeSourceAndAddAliases(Map<String, Object> secondTableHitSource, SearchHit searchHit,
                                            String t1Alias, String t2Alias) {
        Map<String, Object> results = mapWithAliases(searchHit.getSourceAsMap(), t1Alias);
        results.putAll(mapWithAliases(secondTableHitSource, t2Alias));
        searchHit.getSourceAsMap().clear();
        searchHit.getSourceAsMap().putAll(results);
    }

    protected Map<String, Object> mapWithAliases(Map<String, Object> source, String alias) {
        Map<String, Object> mapWithAliases = new HashMap<>();
        for (Map.Entry<String, Object> fieldNameToValue : source.entrySet()) {
            if (!aliasesOnReturn.contains(fieldNameToValue.getKey())) {
                mapWithAliases.put(alias + "." + fieldNameToValue.getKey(), fieldNameToValue.getValue());
            } else {
                mapWithAliases.put(fieldNameToValue.getKey(), fieldNameToValue.getValue());
            }
        }
        return mapWithAliases;
    }

    protected void onlyReturnedFields(Map<String, Object> fieldsMap, List<Field> required, boolean allRequired) {
        HashMap<String, Object> filteredMap = new HashMap<>();
        if (allFieldsReturn || allRequired) {
            filteredMap.putAll(fieldsMap);
            return;
        }
        for (Field field : required) {
            String name = field.getName();
            String returnName = name;
            String alias = field.getAlias();
            if (alias != null && alias != "") {
                returnName = alias;
                aliasesOnReturn.add(alias);
            }
            filteredMap.put(returnName, deepSearchInMap(fieldsMap, name));
        }
        fieldsMap.clear();
        fieldsMap.putAll(filteredMap);

    }

    protected Object deepSearchInMap(Map<String, Object> fieldsMap, String name) {
        if (name.contains(".")) {
            String[] path = name.split("\\.");
            Map<String, Object> currentObject = fieldsMap;
            for (int i = 0; i < path.length - 1; i++) {
                Object valueFromCurrentMap = currentObject.get(path[i]);
                if (valueFromCurrentMap == null) {
                    return null;
                }
                if (!Map.class.isAssignableFrom(valueFromCurrentMap.getClass())) {
                    return null;
                }
                currentObject = (Map<String, Object>) valueFromCurrentMap;
            }
            return currentObject.get(path[path.length - 1]);
        }

        return fieldsMap.get(name);
    }


    protected void addUnmatchedResults(List<SearchHit> combinedResults,
                                       Collection<SearchHitsResult> firstTableSearchHits,
                                       List<Field> secondTableReturnedFields, int currentNumOfIds, int totalLimit,
                                       String t1Alias, String t2Alias) {
        boolean limitReached = false;
        for (SearchHitsResult hitsResult : firstTableSearchHits) {
            if (!hitsResult.isMatchedWithOtherTable()) {
                for (SearchHit hit : hitsResult.getSearchHits()) {

                    //todo: decide which id to put or type. or maby its ok this way. just need to doc.
                    SearchHit unmachedResult = createUnmachedResult(secondTableReturnedFields, hit.docId(),
                            t1Alias, t2Alias, hit);
                    combinedResults.add(unmachedResult);
                    currentNumOfIds++;
                    if (currentNumOfIds >= totalLimit) {
                        limitReached = true;
                        break;
                    }

                }
            }
            if (limitReached) {
                break;
            }
        }
    }

    protected SearchHit createUnmachedResult(List<Field> secondTableReturnedFields, int docId, String t1Alias,
                                             String t2Alias, SearchHit hit) {
        String unmatchedId = hit.getId() + "|0";
        Text unamatchedType = new Text(hit.getType() + "|null");

        Map<String, DocumentField> documentFields = new HashMap<>();
        Map<String, DocumentField> metaFields = new HashMap<>();
        hit.getFields().forEach((fieldName, docField) ->
            (MapperService.META_FIELDS_BEFORE_7DOT8.contains(fieldName) ? metaFields : documentFields).put(fieldName, docField));
        SearchHit searchHit = new SearchHit(docId, unmatchedId, unamatchedType, documentFields, metaFields);

        searchHit.sourceRef(hit.getSourceRef());
        searchHit.getSourceAsMap().clear();
        searchHit.getSourceAsMap().putAll(hit.getSourceAsMap());
        Map<String, Object> emptySecondTableHitSource = createNullsSource(secondTableReturnedFields);

        mergeSourceAndAddAliases(emptySecondTableHitSource, searchHit, t1Alias, t2Alias);

        return searchHit;
    }

    protected Map<String, Object> createNullsSource(List<Field> secondTableReturnedFields) {
        Map<String, Object> nulledSource = new HashMap<>();
        for (Field field : secondTableReturnedFields) {
            if (!field.getName().equals("*")) {
                nulledSource.put(field.getName(), null);
            }
        }
        return nulledSource;
    }

    protected void updateMetaSearchResults(SearchResponse searchResponse) {
        this.metaResults.addSuccessfulShards(searchResponse.getSuccessfulShards());
        this.metaResults.addFailedShards(searchResponse.getFailedShards());
        this.metaResults.addTotalNumOfShards(searchResponse.getTotalShards());
        this.metaResults.updateTimeOut(searchResponse.isTimedOut());
    }

    protected SearchResponse scrollOneTimeWithMax(Client client, TableInJoinRequestBuilder tableRequest) {
        SearchRequestBuilder scrollRequest = tableRequest.getRequestBuilder()
                .setScroll(new TimeValue(60000)).setSize(MAX_RESULTS_ON_ONE_FETCH);
        boolean ordered = tableRequest.getOriginalSelect().isOrderdSelect();
        if (!ordered) {
            scrollRequest.addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC);
        }
        SearchResponse responseWithHits = scrollRequest.get();
        //on ordered select - not using SCAN , elastic returns hits on first scroll
        //es5.0 elastic always return docs on scan
        //  if(!ordered)
        //  responseWithHits = client.prepareSearchScroll(responseWithHits.getScrollId())
        //  .setScroll(new TimeValue(600000)).get();
        return responseWithHits;
    }


}
