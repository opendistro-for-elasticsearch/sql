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

package com.amazon.opendistroforelasticsearch.jdbc.config;

import com.amazon.opendistroforelasticsearch.jdbc.logging.LogLevel;

import java.util.Locale;

public class LogLevelConnectionProperty extends ConnectionProperty<LogLevel> {

    public static final String KEY = "logLevel";

    public LogLevelConnectionProperty() {
        super(KEY);
    }

    @Override
    protected LogLevel parseValue(Object rawValue) throws ConnectionPropertyException {
        if (rawValue == null) {
            return getDefault();
        } else if (rawValue instanceof String) {
            String stringValue = (String) rawValue;
            try {
                return LogLevel.valueOf(stringValue.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException iae) {
                throw new ConnectionPropertyException(getKey(),
                        String.format("Invalid value specified for the property \"%s\". " +
                                "Unknown log level \"%s\".", getKey(), stringValue));
            }
        }

        throw new ConnectionPropertyException(getKey(),
                String.format("Property \"%s\" requires a valid String matching a known log level. " +
                        "Invalid value of type: %s specified.", getKey(), rawValue.getClass().getName()));
    }

    @Override
    public LogLevel getDefault() {
        return LogLevel.OFF;
    }
}
