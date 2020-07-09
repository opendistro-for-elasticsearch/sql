/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.common.response;

/**
 * Response listener for response post-processing callback. This is necessary because execution
 * engine may schedule and execute in different thread.
 *
 * @param <R> response class
 */
public interface ResponseListener<R> {

  /**
   * Handle successful response.
   *
   * @param response successful response
   */
  void onResponse(R response);

  /**
   * Handle failed response.
   *
   * @param e exception captured
   */
  void onFailure(Exception e);
}
