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

import java.util.function.Supplier;

public interface Logger {

    LogLevel getLevel();

    void fatal(String message);

    void fatal(String message, Throwable t);

    default void fatal(Supplier<String> messageSupplier) {
        if (isFatalEnabled()) {
            fatal(messageSupplier.get());
        }
    }

    default void fatal(Supplier<String> messageSupplier, Throwable t) {
        if (isFatalEnabled()) {
            fatal(messageSupplier.get(), t);
        }
    }

    void error(String message);

    void error(String message, Throwable t);

    default void error(Supplier<String> messageSupplier) {
        if (isErrorEnabled()) {
            error(messageSupplier.get());
        }
    }

    default void error(Supplier<String> messageSupplier, Throwable t) {
        if (isErrorEnabled()) {
            error(messageSupplier.get(), t);
        }
    }

    void warn(String message);

    void warn(String message, Throwable t);

    default void warn(Supplier<String> messageSupplier) {
        if (isWarnEnabled()) {
            warn(messageSupplier.get());
        }
    }

    default void warn(Supplier<String> messageSupplier, Throwable t) {
        if (isWarnEnabled()) {
            warn(messageSupplier.get(), t);
        }
    }

    void info(String message);

    void info(String message, Throwable t);

    default void info(Supplier<String> messageSupplier) {
        if (isInfoEnabled()) {
            info(messageSupplier.get());
        }
    }

    default void info(Supplier<String> messageSupplier, Throwable t) {
        if (isInfoEnabled()) {
            info(messageSupplier.get(), t);
        }
    }

    void debug(String message);

    void debug(String message, Throwable t);

    default void debug(Supplier<String> messageSupplier) {
        if (isDebugEnabled()) {
            debug(messageSupplier.get());
        }
    }

    default void debug(Supplier<String> messageSupplier, Throwable t) {
        if (isDebugEnabled()) {
            debug(messageSupplier.get(), t);
        }
    }

    void trace(String message);

    void trace(String message, Throwable t);

    default void trace(Supplier<String> messageSupplier) {
        if (isTraceEnabled()) {
            trace(messageSupplier.get());
        }
    }

    default void trace(Supplier<String> messageSupplier, Throwable t) {
        if (isTraceEnabled()) {
            trace(messageSupplier.get(), t);
        }
    }

    default boolean isDebugEnabled() {
        return getLevel().isGreaterThanOrEqualTo(LogLevel.DEBUG);
    }

    default boolean isErrorEnabled() {
        return isLevelEnabled(LogLevel.ERROR);
    }

    default boolean isFatalEnabled() {
        return isLevelEnabled(LogLevel.FATAL);
    }

    default boolean isInfoEnabled() {
        return isLevelEnabled(LogLevel.INFO);
    }

    default boolean isTraceEnabled() {
        return isLevelEnabled(LogLevel.TRACE);
    }

    default boolean isWarnEnabled() {
        return isLevelEnabled(LogLevel.WARN);
    }

    default boolean isLevelEnabled(LogLevel level) {
        return getLevel().isGreaterThanOrEqualTo(level);
    }

    void close();
}
