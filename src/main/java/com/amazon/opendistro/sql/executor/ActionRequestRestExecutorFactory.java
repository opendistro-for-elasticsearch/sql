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

package com.amazon.opendistro.sql.executor;

import com.amazon.opendistro.sql.executor.csv.CSVResultRestExecutor;
import com.amazon.opendistro.sql.executor.format.PrettyFormatRestExecutor;
import com.amazon.opendistro.sql.query.QueryAction;
import com.amazon.opendistro.sql.query.join.ESJoinQueryAction;
import com.amazon.opendistro.sql.query.multi.MultiQueryAction;

import java.util.stream.Stream;

/**
 * Created by Eliran on 26/12/2015.
 */
public class ActionRequestRestExecutorFactory {

    /**
     * Create executor based on the format and wrap with AsyncRestExecutor to async blocking execute() call if necessary.
     *
     * @param format    format of response
     * @param queryAction query action
     * @return          executor
     */
    public static RestExecutor createExecutor(String format, QueryAction queryAction) {
        if (format == null || format.equals("")) {
            return new AsyncRestExecutor(
                new ElasticDefaultRestExecutor(queryAction),
                action -> isJoin(action) || isUnionMinus(action)
            );
        }

        if (format.equalsIgnoreCase("csv")) {
            return new AsyncRestExecutor(new CSVResultRestExecutor());
        }

        if (Stream.of("jdbc", "table", "raw").anyMatch(format::equalsIgnoreCase)) {
            return new AsyncRestExecutor(new PrettyFormatRestExecutor(format));
        }

        throw new IllegalArgumentException("Failed to create executor due to unknown response format: " + format);
    }

    private static boolean isJoin(QueryAction queryAction) {
        return queryAction instanceof ESJoinQueryAction;
    }

    private static boolean isUnionMinus(QueryAction queryAction) {
        return queryAction instanceof MultiQueryAction;
    }

}
