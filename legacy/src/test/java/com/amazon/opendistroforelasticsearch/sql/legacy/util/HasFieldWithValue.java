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

package com.amazon.opendistroforelasticsearch.sql.legacy.util;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.lang.reflect.Field;

/**
 * A matcher for private field value extraction along with matcher to assert its value.
 *
 * @param <T>   Type of target (actual) object
 * @param <U>   Type of field member (feature) extracted from target object by reflection
 */
public class HasFieldWithValue<T, U> extends FeatureMatcher<T, U> {

    private final String fieldName;

    /**
     * Construct a matcher. Reordered the argument list.
     *
     * @param name      Identifying text for mismatch message
     * @param desc      Descriptive text to use in describeTo
     * @param matcher   The matcher to apply to the feature
     */
    private HasFieldWithValue(String name, String desc, Matcher<? super U> matcher) {
        super(matcher, desc, name);
        this.fieldName = name;
    }

    public static <T, U> HasFieldWithValue<T, U> hasFieldWithValue(String name, String desc, Matcher<? super U> matcher) {
        return new HasFieldWithValue<>(name, desc, matcher);
    }

    @Override
    protected U featureValueOf(T targetObj) {
        return getFieldValue(targetObj, fieldName);
    }

    @SuppressWarnings("unchecked")
    private U getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (U) field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
