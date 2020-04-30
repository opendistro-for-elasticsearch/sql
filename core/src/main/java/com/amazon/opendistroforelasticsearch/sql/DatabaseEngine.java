/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql;

import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.query.QueryEngine;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Environment within which query is parsed, analyzed and executed.
 */
@RequiredArgsConstructor
@Getter
public class DatabaseEngine { //TODO: duplicate as PPLService

    private final QueryEngine queryEngine;

    private final StorageEngine storageEngine;

    private final ExecutionEngine executionEngine;

}
