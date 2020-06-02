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

package com.amazon.opendistroforelasticsearch.sql.esdomain.mapping;

import com.amazon.opendistroforelasticsearch.sql.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@code FieldMapping} with trivial methods ignored such as isSpecified, isMetaField etc.
 */
public class FieldMappingTest {

    @Test
    public void testFieldMatchesWildcardPatternSpecifiedInQuery() {
        assertThat(
            new FieldMapping("employee.first", emptyMap(), fieldsSpecifiedInQuery("employee.*")),
            isWildcardSpecified(true)
        );
    }

    @Test
    public void testFieldMismatchesWildcardPatternSpecifiedInQuery() {
        assertThat(
            new FieldMapping("employee.first", emptyMap(), fieldsSpecifiedInQuery("manager.*")),
            isWildcardSpecified(false)
        );
    }

    @Test
    public void testFieldIsProperty() {
        assertThat(
            new FieldMapping("employee.first"),
            isPropertyField(true)
        );
    }

    @Test
    public void testNestedMultiFieldIsProperty() {
        assertThat(
            new FieldMapping("employee.first.keyword"),
            isPropertyField(true)
        );
    }

    @Test
    public void testFieldIsNotProperty() {
        assertThat(
            new FieldMapping("employee"),
            isPropertyField(false)
        );
    }

    @Test
    public void testMultiFieldIsNotProperty() {
        assertThat(
            new FieldMapping("employee.keyword"),
            isPropertyField(false)
        );
    }

    private Matcher<FieldMapping> isWildcardSpecified(boolean isMatched) {
        return MatcherUtils.featureValueOf("is field match wildcard specified in query",
                                           is(isMatched),
                                           FieldMapping::isWildcardSpecified);
    }

    private Matcher<FieldMapping> isPropertyField(boolean isProperty) {
        return MatcherUtils.featureValueOf("isPropertyField",
                                           is(isProperty),
                                           FieldMapping::isPropertyField);
    }

    private Map<String, Field> fieldsSpecifiedInQuery(String...fieldNames) {
        return Arrays.stream(fieldNames).
                      collect(Collectors.toMap(name -> name,
                                               name -> new Field(name, "")));
    }

}