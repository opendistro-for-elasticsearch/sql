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

package com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.mapping;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import com.google.common.collect.ImmutableMap;
import org.elasticsearch.common.collect.ImmutableOpenMap;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * Mappings interface to provide default implementation (minimal set of Map methods) for subclass in hierarchy.
 *
 * @param <T> Type of nested mapping
 */
public interface Mappings<T> {

    default boolean has(String name) {
        return data().containsKey(name);
    }

    default Collection<String> allNames() {
        return data().keySet();
    }

    default T mapping(String name) {
        return data().get(name);
    }

    default T firstMapping() {
        return allMappings().iterator().next();
    }

    default Collection<T> allMappings() {
        return data().values();
    }

    default boolean isEmpty() {
        return data().isEmpty();
    }

    Map<String, T> data();

    /**
     * Convert ES ImmutableOpenMap<String, X> to JDK Map<String, Y> by applying function: Y func(X)
     */
    default <X, Y> Map<String, Y> buildMappings(ImmutableOpenMap<String, X> mappings, Function<X, Y> func) {
        ImmutableMap.Builder<String, Y> builder = ImmutableMap.builder();
        for (ObjectObjectCursor<String, X> mapping : mappings) {
            builder.put(mapping.key, func.apply(mapping.value));
        }
        return builder.build();
    }
}
