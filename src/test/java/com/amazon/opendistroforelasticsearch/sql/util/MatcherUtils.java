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

package com.amazon.opendistroforelasticsearch.sql.util;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

public class MatcherUtils {

    /**
     * Assert field value in object by a custom matcher and getter to access the field.
     *
     * @param name          description
     * @param subMatcher    sub-matcher for field
     * @param getter        getter function to access the field
     * @param <T>           type of outer object
     * @param <U>           type of inner field
     * @return              matcher
     */
    public static <T, U> FeatureMatcher<T, U> featureValueOf(String name,
                                                             Matcher<U> subMatcher,
                                                             Function<T, U> getter) {
        return new FeatureMatcher<T, U>(subMatcher, name, name) {
            @Override
            protected U featureValueOf(T actual) {
                return getter.apply(actual);
            }
        };
    }

    @SafeVarargs
    public static Matcher<SearchHits> hits(Matcher<SearchHit>... hitMatchers) {
        if (hitMatchers.length == 0) {
            return featureValueOf("SearchHits", emptyArray(), SearchHits::getHits);
        }
        return featureValueOf("SearchHits", arrayContainingInAnyOrder(hitMatchers), SearchHits::getHits);
    }

    @SafeVarargs
    public static Matcher<SearchHits> hitsInOrder(Matcher<SearchHit>... hitMatchers) {
        if (hitMatchers.length == 0) {
            return featureValueOf("SearchHits", emptyArray(), SearchHits::getHits);
        }
        return featureValueOf("SearchHits", arrayContaining(hitMatchers), SearchHits::getHits);
    }

    @SuppressWarnings("unchecked")
    public static Matcher<SearchHit> hit(Matcher<Map<String, Object>>... entryMatchers) {
        return featureValueOf("SearchHit", allOf(entryMatchers), SearchHit::getSourceAsMap);
    }

    @SuppressWarnings("unchecked")
    public static Matcher<Map<String, Object>> kv(String key, Object value) {
        // Use raw type to avoid generic type problem from Matcher<Map<K,V>> to Matcher<String,Object>
        return (Matcher) hasEntry(key, value);
    }

    public static Matcher<JSONObject> hitAny(String query, Matcher<JSONObject>... matcher) {
        return featureValueOf("SearchHits", hasItems(matcher), actual -> {
            JSONArray array = (JSONArray) (actual.query(query));
            List<JSONObject> results = new ArrayList<>(array.length());
            for (Object element : array) {
                results.add((JSONObject)element);
            }
            return results;
        });
    }

    public static Matcher<JSONObject> hitAny(Matcher<JSONObject>... matcher) {
        return hitAny("/hits/hits", matcher);
    }

    public static Matcher<JSONObject> hitAll(Matcher<JSONObject>... matcher) {
        return featureValueOf("SearchHits", containsInAnyOrder(matcher), actual -> {
            JSONArray array = (JSONArray) (actual.query("/hits/hits"));
            List<JSONObject> results = new ArrayList<>(array.length());
            for (Object element : array) {
                results.add((JSONObject)element);
            }
            return results;
        });
    }

    public static Matcher<JSONObject> kvString(String key, Matcher<String> matcher) {
        return featureValueOf("Json Match", matcher, actual -> (String) actual.query(key));
    }

    public static Matcher<JSONObject> kvDouble(String key, Matcher<Double> matcher) {
        return featureValueOf("Json Match", matcher, actual -> (Double) actual.query(key));
    }

    public static Matcher<JSONObject> kvInt(String key, Matcher<Integer> matcher) {
        return featureValueOf("Json Match", matcher, actual -> (Integer) actual.query(key));
    }

    public static <K, V> Matcher<Map<? extends K, ? extends V>> aMapWithSize(int expectedSize) {
        return featureValueOf("Map size match", is(expectedSize), Map::size);
    }
}
