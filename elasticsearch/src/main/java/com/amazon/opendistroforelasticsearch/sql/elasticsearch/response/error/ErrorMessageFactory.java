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

import lombok.experimental.UtilityClass;
import org.elasticsearch.ElasticsearchException;

@UtilityClass
public class ErrorMessageFactory {
  /**
   * Create error message based on the exception type.
   * Exceptions of ES exception type and exceptions with wrapped ES exception causes
   * should create {@link ElasticsearchErrorMessage}
   *
   * @param e      exception to create error message
   * @param status exception status code
   * @return error message
   */
  public static ErrorMessage createErrorMessage(Exception e, int status) {
    Throwable cause = unwrapCause(e);
    if (cause instanceof ElasticsearchException) {
      ElasticsearchException exception = (ElasticsearchException) cause;
      return new ElasticsearchErrorMessage(exception, exception.status().getStatus());
    }
    return new ErrorMessage(e, status);
  }

  protected static Throwable unwrapCause(Throwable t) {
    Throwable result = t;
    if (result instanceof ElasticsearchException) {
      return result;
    }
    if (result.getCause() == null) {
      return result;
    }
    result = unwrapCause(result.getCause());
    return result;
  }
}
