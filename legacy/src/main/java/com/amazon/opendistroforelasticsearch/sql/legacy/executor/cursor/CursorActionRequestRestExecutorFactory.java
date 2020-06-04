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

package com.amazon.opendistroforelasticsearch.sql.legacy.executor.cursor;

import com.amazon.opendistroforelasticsearch.sql.legacy.executor.Format;
import org.elasticsearch.rest.RestRequest;

public class CursorActionRequestRestExecutorFactory {

    public static CursorAsyncRestExecutor createExecutor(RestRequest request, String cursorId, Format format) {

        if (isCursorCloseRequest(request)) {
            return new CursorAsyncRestExecutor(new CursorCloseExecutor(cursorId));
        } else {
            return new CursorAsyncRestExecutor(new CursorResultExecutor(cursorId, format));
        }
    }

    private static boolean isCursorCloseRequest(final RestRequest request) {
        return request.path().endsWith("/_sql/close");
    }
}