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

package com.amazon.opendistroforelasticsearch.sql.legacy.executor;

import com.amazon.opendistroforelasticsearch.sql.legacy.executor.csv.CSVResultRestExecutor;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.PrettyFormatRestExecutor;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.join.ESJoinQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.multi.MultiQueryAction;

/**
 * Created by Eliran on 26/12/2015.
 */
public class ActionRequestRestExecutorFactory {
    /**
     * Create executor based on the format and wrap with AsyncRestExecutor
     * to async blocking execute() call if necessary.
     *
     * @param format      format of response
     * @param queryAction query action
     * @return executor
     */
    public static RestExecutor createExecutor(Format format, QueryAction queryAction) {
        switch (format) {
            case CSV:
                return new AsyncRestExecutor(new CSVResultRestExecutor());
            case JSON:
                return new AsyncRestExecutor(
                        new ElasticDefaultRestExecutor(queryAction),
                        action -> isJoin(action) || isUnionMinus(action)
                );
            case JDBC:
            case RAW:
            case TABLE:
            default:
                return new AsyncRestExecutor(new PrettyFormatRestExecutor(format.getFormatName()));
        }
    }

    private static boolean isJoin(QueryAction queryAction) {
        return queryAction instanceof ESJoinQueryAction;
    }

    private static boolean isUnionMinus(QueryAction queryAction) {
        return queryAction instanceof MultiQueryAction;
    }

}
