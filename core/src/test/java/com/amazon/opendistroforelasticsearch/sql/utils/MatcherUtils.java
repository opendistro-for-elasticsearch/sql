/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.utils;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Map;

public class MatcherUtils {
    public static TypeSafeMatcher<ExprValue> tuple(Map<String, Object> map) {
        return new TypeSafeMatcher<ExprValue>() {
            @Override
            public void describeTo(Description description) {
                description.appendText(map.toString());
            }

            @Override
            protected boolean matchesSafely(ExprValue value) {
                if (ExprType.STRUCT == value.type()) {
                    ExprTupleValue tupleValue = (ExprTupleValue) value;
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        if (!tupleValue.bindingTuples().resolve(DSL.ref(entry.getKey()))
                                .equals(ExprValueUtils.fromObjectValue(entry.getValue()))) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            }
        };
    }

    public static TypeSafeMatcher<ExprValue> hasType(ExprType type) {
        return new TypeSafeMatcher<ExprValue>() {
            @Override
            public void describeTo(Description description) {
                description.appendText(type.toString());
            }

            @Override
            protected boolean matchesSafely(ExprValue value) {
                return type == value.type();
            }
        };
    }

    public static TypeSafeMatcher<ExprValue> hasValue(Object object) {
        return new TypeSafeMatcher<ExprValue>() {
            @Override
            public void describeTo(Description description) {
                description.appendText(object.toString());
            }

            @Override
            protected boolean matchesSafely(ExprValue value) {
                return object.equals(value.value());
            }
        };
    }
}
