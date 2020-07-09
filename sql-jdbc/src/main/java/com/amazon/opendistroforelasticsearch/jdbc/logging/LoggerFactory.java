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

import java.io.IOException;
import java.io.PrintWriter;

public class LoggerFactory {

    public static Logger getLogger(String filePath, LogLevel logLevel) {
        return getLogger(filePath, logLevel, StandardLayout.INSTANCE);
    }

    public static Logger getLogger(String filePath, LogLevel logLevel, Layout layout) {
        try {
            return new FilePrintWriterLogger(filePath, logLevel, layout);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static Logger getLogger(PrintWriter printWriter, LogLevel logLevel) {
        return getLogger(printWriter, logLevel, StandardLayout.INSTANCE);
    }

    public static Logger getLogger(PrintWriter printWriter, LogLevel logLevel, Layout layout) {
        return new PrintWriterLogger(printWriter, logLevel, layout);
    }
}
