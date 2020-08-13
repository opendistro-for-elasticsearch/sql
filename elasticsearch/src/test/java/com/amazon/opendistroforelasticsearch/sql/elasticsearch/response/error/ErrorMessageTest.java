/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.response.error;

import static org.elasticsearch.rest.RestStatus.BAD_REQUEST;
import static org.elasticsearch.rest.RestStatus.SERVICE_UNAVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ErrorMessageTest {

  @Test
  public void testToString() {
    ErrorMessage errorMessage =
        new ErrorMessage(new IllegalStateException("illegal state"),
            SERVICE_UNAVAILABLE.getStatus());
    assertEquals("{\n"
        + "  \"error\": {\n"
        + "    \"reason\": \"There was internal problem at backend\",\n"
        + "    \"details\": \"illegal state\",\n"
        + "    \"type\": \"IllegalStateException\"\n"
        + "  },\n"
        + "  \"status\": 503\n"
        + "}", errorMessage.toString());
  }

  @Test
  public void testBadRequestToString() {
    ErrorMessage errorMessage =
        new ErrorMessage(new IllegalStateException(),
            BAD_REQUEST.getStatus());
    assertEquals("{\n"
        + "  \"error\": {\n"
        + "    \"reason\": \"Invalid Query\",\n"
        + "    \"details\": \"\",\n"
        + "    \"type\": \"IllegalStateException\"\n"
        + "  },\n"
        + "  \"status\": 400\n"
        + "}", errorMessage.toString());
  }

  @Test
  public void testToStringWithEmptyErrorMessage() {
    ErrorMessage errorMessage =
        new ErrorMessage(new IllegalStateException(),
            SERVICE_UNAVAILABLE.getStatus());
    assertEquals("{\n"
        + "  \"error\": {\n"
        + "    \"reason\": \"There was internal problem at backend\",\n"
        + "    \"details\": \"\",\n"
        + "    \"type\": \"IllegalStateException\"\n"
        + "  },\n"
        + "  \"status\": 503\n"
        + "}", errorMessage.toString());
  }
}