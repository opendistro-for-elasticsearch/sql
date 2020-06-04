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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.utils;

import com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.Matchers.equalTo;

public class StringUtilsTest {

    private Locale originalLocale;

    @Before
    public void saveOriginalLocale() {
        originalLocale = Locale.getDefault();
    }

    @After
    public void restoreOriginalLocale() {
        Locale.setDefault(originalLocale);
    }

    @Test
    public void toLower() {
        final String input = "SAMPLE STRING";
        final String output = StringUtils.toLower(input);

        Assert.assertThat(output, equalTo("sample string"));

        // See https://docs.oracle.com/javase/10/docs/api/java/lang/String.html#toLowerCase(java.util.Locale)
        // for the choice of these characters and the locale.
        final String upper = "\u0130 \u0049";
        Locale.setDefault(Locale.forLanguageTag("tr"));

        Assert.assertThat(upper.toUpperCase(Locale.ROOT), equalTo(StringUtils.toUpper(upper)));
    }

    @Test
    public void toUpper() {
        final String input = "sample string";
        final String output = StringUtils.toUpper(input);

        Assert.assertThat(output, equalTo("SAMPLE STRING"));

        // See https://docs.oracle.com/javase/10/docs/api/java/lang/String.html#toUpperCase(java.util.Locale)
        // for the choice of these characters and the locale.
        final String lower = "\u0069 \u0131";
        Locale.setDefault(Locale.forLanguageTag("tr"));

        Assert.assertThat(lower.toUpperCase(Locale.ROOT), equalTo(StringUtils.toUpper(lower)));
    }

    @Test
    public void format() {
        Locale.setDefault(Locale.forLanguageTag("tr"));
        final String upper = "\u0130 \u0049";
        final String lower = "\u0069 \u0131";

        final String output = StringUtils.format("%s %s", upper, lower);
        Assert.assertThat(output, equalTo(String.format(Locale.ROOT, "%s %s", upper, lower)));
    }
}
