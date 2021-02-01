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

package com.amazon.opendistroforelasticsearch.sql.correctness.tests;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.correctness.runner.connection.ESConnection;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicStatusLine;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for {@link ESConnection}
 */
@RunWith(MockitoJUnitRunner.class)
public class ESConnectionTest {

  @Mock
  private RestClient client;

  private ESConnection conn;

  @Before
  public void setUp() throws IOException {
    conn = new ESConnection("jdbc:elasticsearch://localhost:12345", client);

    Response response = mock(Response.class);
    when(client.performRequest(any(Request.class))).thenReturn(response);
    when(response.getStatusLine())
        .thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 2, 0), 200, ""));
  }

  @Test
  public void testCreateTable() throws IOException {
    conn.create("test", "mapping");

    Request actual = captureActualArg();
    assertEquals("PUT", actual.getMethod());
    assertEquals("/test", actual.getEndpoint());
    assertEquals("mapping", getBody(actual));
  }

  @Test
  public void testInsertData() throws IOException {
    conn.insert("test", new String[] {"name"},
        Arrays.asList(new String[] {"John"}, new String[] {"Hank"}));

    Request actual = captureActualArg();
    assertEquals("POST", actual.getMethod());
    assertEquals("/test/_bulk?refresh=true", actual.getEndpoint());
    assertEquals(
        "{\"index\":{}}\n"
            + "{\"name\":\"John\"}\n"
            + "{\"index\":{}}\n"
            + "{\"name\":\"Hank\"}\n",
        getBody(actual)
    );
  }

  @Test
  public void testInsertNullData() throws IOException {
    conn.insert("test", new String[] {"name", "age"},
        Arrays.asList(new Object[] {null, 30}, new Object[] {"Hank", null}));

    Request actual = captureActualArg();
    assertEquals("POST", actual.getMethod());
    assertEquals("/test/_bulk?refresh=true", actual.getEndpoint());
    assertEquals(
        "{\"index\":{}}\n"
            + "{\"age\":30}\n"
            + "{\"index\":{}}\n"
            + "{\"name\":\"Hank\"}\n",
        getBody(actual)
    );
  }

  @Test
  public void testDropTable() throws IOException {
    conn.drop("test");

    Request actual = captureActualArg();
    assertEquals("DELETE", actual.getMethod());
    assertEquals("/test", actual.getEndpoint());
  }

  private Request captureActualArg() throws IOException {
    ArgumentCaptor<Request> argCap = ArgumentCaptor.forClass(Request.class);
    verify(client).performRequest(argCap.capture());
    return argCap.getValue();
  }

  private String getBody(Request request) throws IOException {
    InputStream inputStream = request.getEntity().getContent();
    return CharStreams.toString(new InputStreamReader(inputStream));
  }

}
