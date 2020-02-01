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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;

public class ElasticsearchErrorMessage extends ErrorMessage {

    private ElasticsearchException exception;
    private static final Logger LOG = LogManager.getLogger();

    ElasticsearchErrorMessage(ElasticsearchException exception, int status) {
        super(exception, status);
        this.exception = exception;
    }

    @Override
    protected String fetchReason() {
        String detailedMsg = "";
        try {
            detailedMsg = ": " + exception.getDetailedMessage();
        } catch (Exception e) {
            LOG.error("Error occurred when fetching ES exception reasons", e);
        }
        return "Error occurred in Elasticsearch engine" + detailedMsg;
    }

    /** Currently Sql-Jdbc plugin only supports string type as reason and details in the error messages */
    @Override
    protected String fetchDetails() {
        StringBuilder details = new StringBuilder();
        try {
            ElasticsearchException[] rootCauses = ElasticsearchException.guessRootCauses(exception);
            for (ElasticsearchException e : rootCauses) {
                details.append(e.getClass().getName()).append(": ").append(e.getMessage()).append("; ");
            }
        } catch (Exception e) {
            LOG.error("Error occurred when fetching ES exception details", e);
        }
        return details.toString();
    }
}
