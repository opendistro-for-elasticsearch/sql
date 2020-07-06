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

package com.amazon.opendistroforelasticsearch.jdbc.test;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * A Factory class for building key-value pair objects
 */
public class KeyValuePairs {

    public static StringKvp skvp(String key, String value) {
        return new StringKvp(key, value);
    }

    /**
     * Models a key-value pair where both key and value are Strings
     */
    public static class StringKvp extends AbstractMap.SimpleImmutableEntry<String, String> {

        public StringKvp(String key, String value) {
            super(key, value);
        }
    }

    public static Properties toProperties(final StringKvp... kvps) {
        Properties props = new Properties();
        Arrays.stream(kvps).forEach(kvp -> props.setProperty(kvp.getKey(), kvp.getValue()));
        return props;
    }

    public static Map<String, String> toMap(final StringKvp... kvps) {
        return Arrays.stream(kvps).collect(Collectors.toMap(StringKvp::getKey, StringKvp::getValue));
    }
}


