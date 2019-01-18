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

package com.amazon.opendistro.sql.executor.csv;

import com.amazon.opendistro.sql.executor.QueryActionElasticExecutor;
import com.amazon.opendistro.sql.executor.RestExecutor;
import com.google.common.base.Joiner;
import org.elasticsearch.client.Client;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestStatus;
import com.amazon.opendistro.sql.query.QueryAction;

import java.util.*;

/**
 * Created by Eliran on 26/12/2015.
 */
public class CSVResultRestExecutor implements RestExecutor {

    @Override
    public void execute(Client client, Map<String, String> params, QueryAction queryAction, RestChannel channel) throws Exception {
        Object queryResult = QueryActionElasticExecutor.executeAnyAction(client, queryAction);

        boolean flat = getBooleanOrDefault(params,"flat",false);
        String separator = ",";
        if(params.containsKey("separator")){
         separator = params.get("separator");
        }
        boolean includeScore = getBooleanOrDefault(params,"_score",false);
        boolean includeType = getBooleanOrDefault(params,"_type",false);
        boolean includeId = getBooleanOrDefault(params,"_id",false);
        CSVResult result  = new CSVResultsExtractor(includeScore,includeType,includeId).extractResults(queryResult,flat,separator);
        String newLine = "\n";
        if(params.containsKey("newLine")){
         newLine = params.get("newLine");
        }
        String csvString = buildString(separator, result, newLine);
        BytesRestResponse bytesRestResponse = new BytesRestResponse(RestStatus.OK, csvString);
        channel.sendResponse(bytesRestResponse);
    }

    @Override
    public String execute(Client client, Map<String, String> params, QueryAction queryAction) throws Exception {
        Object queryResult = QueryActionElasticExecutor.executeAnyAction(client, queryAction);

        boolean flat = getBooleanOrDefault(params,"flat",false);
        String separator = ",";
        if(params.containsKey("separator")){
            separator = params.get("separator");
        }
        boolean includeScore = getBooleanOrDefault(params,"_score",false);
        boolean includeType = getBooleanOrDefault(params,"_type",false);
        boolean includeId = getBooleanOrDefault(params,"_id",false);
        CSVResult result  = new CSVResultsExtractor(includeScore,includeType,includeId).extractResults(queryResult,flat,separator);
        String newLine = "\n";
        if(params.containsKey("newLine")){
            newLine = params.get("newLine");
        }
        String csvString = buildString(separator, result, newLine);
        return csvString;
    }

    private boolean getBooleanOrDefault(Map<String, String> params, String param, boolean defaultValue) {
        boolean flat = defaultValue;
        if(params.containsKey(param)){
            flat = Boolean.parseBoolean(params.get(param));
        }
        return flat;
    }

    private String buildString(String separator, CSVResult result, String newLine) {
        StringBuilder csv = new StringBuilder();
        csv.append(Joiner.on(separator).join(result.getHeaders()));
        csv.append(newLine);
        csv.append(Joiner.on(newLine).join(result.getLines()));
        return csv.toString();
    }

}
