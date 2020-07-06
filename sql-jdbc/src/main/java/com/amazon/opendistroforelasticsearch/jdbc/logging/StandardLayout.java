/*
 * Copyright <2019> Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.jdbc.logging;

/**
 * The default log entry layout for driver emitted logs
 *
 * Formats log entries with [timestamp][severity][thread-name] message
 *
 * Timestamp uses ISO format date and a a 24 hour clock value upto
 * milliseconds: [YYYY-mm-dd HH:MM:SS.mmm]
 */
public class StandardLayout implements Layout {
    public static final StandardLayout INSTANCE = new StandardLayout();

    private StandardLayout() {
        // singleton
    }

    @Override
    public String formatLogEntry(LogLevel severity, String message)  {
        long time = System.currentTimeMillis();
        return String.format("[%tF %tT.%tL][%-5s][Thread-%s]%s",
                time, time, time, severity, Thread.currentThread().getName(), message);
    }
}
