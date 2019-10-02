package com.amazon.opendistroforelasticsearch.sql.unittest;

import com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils;
import com.amazon.opendistroforelasticsearch.sql.utils.JsonPrettyFormatter;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class JsonFormatterTest {

    @Test
    public void assertFormatterWithoutContentInside() throws IOException {
        String noContentInput = "{ }";
        String expectedOutput = "{ }";
        String result = new JsonPrettyFormatter().format(noContentInput);
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
        String result = new JsonPrettyFormatter().format(explainFormattedOneline);

        assertThat(result, equalTo(explainFormattedPretty));
    }

    @Test(expected = IOException.class)
    public void illegalInputOfNull() throws IOException {
        new JsonPrettyFormatter().format("");
    }

    @Test(expected = IOException.class)
    public void illegalInputOfUnpairedBrace() throws IOException {
        new JsonPrettyFormatter().format("{\"key\" : \"value\"");
    }

    @Test(expected = IOException.class)
    public void illegalInputOfWrongBraces() throws IOException {
        new JsonPrettyFormatter().format("<\"key\" : \"value\">");
    }
}
