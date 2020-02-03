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

package com.amazon.opendistroforelasticsearch.sql.executor.format;

import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.ShardSearchFailure;

public class ElasticsearchErrorMessage extends ErrorMessage {

    private static final Logger LOG = LogManager.getLogger();

    ElasticsearchErrorMessage(ElasticsearchException exception, int status) {
        super(exception, status);
    }

    @Override
    protected String fetchReason() {
        String detailedMsg = "";
        try {
            detailedMsg = ": " + exception.getMessage();
        } catch (Exception e) {
            LOG.error("Error occurred when fetching ES exception reasons", e);
        }
        return "Error occurred in Elasticsearch engine" + detailedMsg;
    }

    /** Currently Sql-Jdbc plugin only supports string type as reason and details in the error messages */
    @Override
    protected String fetchDetails() {
        StringBuilder details = new StringBuilder();
        if (exception instanceof SearchPhaseExecutionException) {
            String detail = fetchSearchPhaseExecutionExceptionDetails((SearchPhaseExecutionException) exception);
            details.append(detail);
        } else {
            details.append(defaultDetails((ElasticsearchException) exception));
        }
        details.append("\nFor more details, please send request for Json format to see the raw response from "
                + "elasticsearch engine.");
        return details.toString();
    }

    private String defaultDetails(ElasticsearchException exception) {
        String detailedMsg = "";
        try {
            detailedMsg = exception.getDetailedMessage();
        } catch (Exception e) {
            LOG.error("Error occurred when fetching ES exception details", e);
        }
        return detailedMsg;
    }

    /**
     * Could not deliver the exactly same error messages due to the limit of JDBC types.
     * Currently our cases occurred only SearchPhaseExecutionException instances among all types of ES exceptions
     * according to the survey, see all types: ElasticsearchException.ElasticsearchExceptionHandle.
     * Either add methods of fetching details for different types, or re-make a consistent message by not giving
     * detailed messages/root causes but only a suggestion message.
     */
    private String fetchSearchPhaseExecutionExceptionDetails(SearchPhaseExecutionException exception) {
        StringBuilder details = new StringBuilder();
        try {
            ShardSearchFailure[] shardFailures = exception.shardFailures();
            for (ShardSearchFailure failure : shardFailures) {
                details.append(StringUtils.format("Shard[%d]: %s\n", failure.shardId(), failure.getCause().toString()));
            }
        } catch (Exception e) {
            LOG.error("Error occurred when fetching shards failure details", e);
        }
        return details.toString();
    }
}
