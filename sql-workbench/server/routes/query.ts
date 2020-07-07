/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import { Server } from 'hapi-latest';
import QueryService from '../services/QueryService';
import {ROUTE_PATH_QUERY, ROUTE_PATH_QUERY_CSV, ROUTE_PATH_QUERY_JSON, ROUTE_PATH_QUERY_JDBC, ROUTE_PATH_QUERY_TEXT} from "../utils/constants";

export default function query(server: Server, service: QueryService) {
  server.route({
    path: ROUTE_PATH_QUERY,
    method: 'POST',
    handler: service.describeQuery
  });
  server.route({
    path: ROUTE_PATH_QUERY_CSV,
    method: 'POST',
    handler: service.describeQueryCsv
  });
  server.route({
    path: ROUTE_PATH_QUERY_JSON,
    method: 'POST',
    handler: service.describeQueryJson
  });
  server.route({
    path: ROUTE_PATH_QUERY_JDBC,
    method: 'POST',
    handler: service.describeQueryJdbc
  });
  server.route({
    path: ROUTE_PATH_QUERY_TEXT,
    method: 'POST',
    handler: service.describeQueryText
  });
}
