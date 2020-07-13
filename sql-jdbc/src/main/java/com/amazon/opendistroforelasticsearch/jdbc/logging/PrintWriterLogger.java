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

import java.io.PrintWriter;
import java.io.StringWriter;

public class PrintWriterLogger implements Logger {

    protected final PrintWriter printWriter;
    private LogLevel logLevel;
    private Layout layout;

    public PrintWriterLogger(PrintWriter printWriter, LogLevel logLevel, Layout layout) {
        this.printWriter = printWriter;
        this.logLevel = logLevel == null ? LogLevel.OFF : logLevel;
        this.layout = layout;
    }

    @Override
    public void fatal(String message) {
        printMessage(LogLevel.FATAL, message);
    }

    @Override
    public void fatal(String message, Throwable t) {
        printMessage(LogLevel.FATAL, message, t);
    }

    @Override
    public void error(String message) {
        printMessage(LogLevel.ERROR, message);
    }

    @Override
    public void error(String message, Throwable t) {
        printMessage(LogLevel.ERROR, message, t);
    }

    @Override
    public void warn(String message) {
        printMessage(LogLevel.WARN, message);
    }

    @Override
    public void warn(String message, Throwable t) {
        printMessage(LogLevel.WARN, message, t);
    }

    private void printMessage(LogLevel severity, String message) {
        printWriter.println(layout.formatLogEntry(severity, message));
    }

    private void printMessage(LogLevel severity, String message, Throwable t) {
        String logMessage = buildMessageWithThrowable(message, t);
        printWriter.println(layout.formatLogEntry(severity, logMessage));
    }

    private String buildMessageWithThrowable(String message, Throwable t) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter pw = new PrintWriter(stringWriter);
        pw.println(message);
        t.printStackTrace(pw);
        pw.close();
        return stringWriter.toString();
    }
    @Override
    public void info(String message) {
        printMessage(LogLevel.INFO, message);
    }

    @Override
    public void info(String message, Throwable t) {
        printMessage(LogLevel.INFO, message, t);
    }

    @Override
    public void debug(String message) {
        printMessage(LogLevel.DEBUG, message);
    }

    @Override
    public void debug(String message, Throwable t) {
        printMessage(LogLevel.DEBUG, message, t);
    }

    @Override
    public void trace(String message) {
        printMessage(LogLevel.TRACE, message);
    }

    @Override
    public void trace(String message, Throwable t) {
        printMessage(LogLevel.TRACE, message, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logLevel.isGreaterThanOrEqualTo(LogLevel.DEBUG);
    }

    @Override
    public LogLevel getLevel() {
        return logLevel;
    }

    @Override
    public void close() {
        printWriter.flush();
    }
}
