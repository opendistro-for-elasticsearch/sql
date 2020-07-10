package com.amazon.opendistroforelasticsearch.jdbc.protocol.http;

import com.amazon.opendistroforelasticsearch.jdbc.protocol.JdbcQueryRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class JsonCursorQueryRequestTests {

    @Test
    public void testCursorRequestBody() {
        JdbcQueryRequest jdbcQueryRequest = new JdbcQueryRequest("abcde12345");
        JsonCursorQueryRequest jsonCursorQueryRequest = new JsonCursorQueryRequest(jdbcQueryRequest);
        ObjectMapper mapper = new ObjectMapper();
        String expectedRequestBody = "{\"cursor\":\"abcde12345\"}";
        String actual = assertDoesNotThrow(() -> mapper.writeValueAsString(jsonCursorQueryRequest));
        assertEquals(expectedRequestBody, actual);

        assertEquals(0, jsonCursorQueryRequest.getFetchSize());
        assertEquals(null, jsonCursorQueryRequest.getParameters());

    }
}
