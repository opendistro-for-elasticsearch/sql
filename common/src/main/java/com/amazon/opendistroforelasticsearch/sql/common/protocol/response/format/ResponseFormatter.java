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

package com.amazon.opendistroforelasticsearch.sql.common.protocol.response.format;

/**
 * Response formatter to format response to different formats.
 */
public interface ResponseFormatter<Response> {

    /**
     * Format response into string in expected format.
     * @param response  response
     * @return          string with response content formatted
     */
    String format(Response response);

    /**
     * Format an exception into string.
     * @param t     exception occurred
     * @return      string with exception content formatted
     */
    String format(Throwable t);

}
