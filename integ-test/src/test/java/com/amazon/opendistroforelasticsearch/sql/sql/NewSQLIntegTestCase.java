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

package com.amazon.opendistroforelasticsearch.sql.sql;

import com.amazon.opendistroforelasticsearch.sql.legacy.SQLIntegTestCase;
import com.amazon.opendistroforelasticsearch.sql.util.TestUtils;

/**
 * SQL integration test base class for new SQL query engine.
 * Old {@link SQLIntegTestCase} can be removed when legacy IT not needed.
 */
public abstract class NewSQLIntegTestCase extends SQLIntegTestCase {

  @Override
  protected void init() throws Exception {
    super.init();
    TestUtils.enableNewQueryEngine(client());
  }

}
