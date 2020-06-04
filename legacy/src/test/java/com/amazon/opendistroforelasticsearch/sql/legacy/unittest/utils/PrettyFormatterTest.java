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

import com.amazon.opendistroforelasticsearch.sql.legacy.util.TestUtils;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.JsonPrettyFormatter;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PrettyFormatterTest {

    @Test
    public void assertFormatterWithoutContentInside() throws IOException {
        String noContentInput = "{ }";
        String expectedOutput = "{ }";
        String result = JsonPrettyFormatter.format(noContentInput);
        assertThat(result, equalTo(expectedOutput));
    }

    @Test
    public void assertFormatterOutputsPrettyJson() throws IOException {
        String explainFormattedPrettyFilePath = TestUtils.getResourceFilePath(
                "/src/test/resources/expectedOutput/explain_format_pretty.json");
        String explainFormattedPretty = Files.toString(new File(explainFormattedPrettyFilePath), StandardCharsets.UTF_8);

        String explainFormattedOnelineFilePath = TestUtils.getResourceFilePath(
                "/src/test/resources/explain_format_oneline.json");
        String explainFormattedOneline = Files.toString(new File(explainFormattedOnelineFilePath), StandardCharsets.UTF_8);
        String result = JsonPrettyFormatter.format(explainFormattedOneline);

        assertThat(result, equalTo(explainFormattedPretty));
    }

    @Test(expected = IOException.class)
    public void illegalInputOfNull() throws IOException {
        JsonPrettyFormatter.format("");
    }

    @Test(expected = IOException.class)
    public void illegalInputOfUnpairedBrace() throws IOException {
        JsonPrettyFormatter.format("{\"key\" : \"value\"");
    }

    @Test(expected = IOException.class)
    public void illegalInputOfWrongBraces() throws IOException {
        JsonPrettyFormatter.format("<\"key\" : \"value\">");
    }
}
