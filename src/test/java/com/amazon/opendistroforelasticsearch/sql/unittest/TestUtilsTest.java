package com.amazon.opendistroforelasticsearch.sql.unittest;

import com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link TestUtils}
 */
public class TestUtilsTest {

    @Test
    public void typeNameInTestDataSetFileShouldBeRemoved() {
        List<String> lines = Arrays.asList(
            "{\"index\":{\"_type\": \"test\"}}",
            "{\"name\": \"John\"}",
            ""
        );

        String actual = TestUtils.readStringWithTypeNameRemoved(lines);
        String expected = "{\"index\":{}}\n" +
                          "{\"name\": \"John\"}\n" +
                          "\n";
        assertEquals(expected, actual);
    }

    @Test
    public void otherFieldsThanTypeInTestDataSetFileShouldBeUntouched() {
        List<String> lines = Arrays.asList(
            "{\"index\":{\"_type\": \"test\", \"_id\": 1}}",
            "{\"name\": \"John\"}",
            ""
        );

        String actual = TestUtils.readStringWithTypeNameRemoved(lines);
        String expected = "{\"index\":{\"_id\":1}}\n" + // no space after org.json format
                          "{\"name\": \"John\"}\n" +
                          "\n";
        assertEquals(expected, actual);
    }

}
