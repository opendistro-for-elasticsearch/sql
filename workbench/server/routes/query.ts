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
import { ROUTE_PATH_SQL_QUERY, ROUTE_PATH_PPL_QUERY, ROUTE_PATH_SQL_CSV, ROUTE_PATH_SQL_JSON, ROUTE_PATH_SQL_TEXT, ROUTE_PATH_PPL_CSV, ROUTE_PATH_PPL_JSON, ROUTE_PATH_PPL_TEXT } from "../utils/constants";

export default function query(server: Server, service: QueryService) {
  server.route({
    path: ROUTE_PATH_SQL_QUERY,
    method: 'POST',
    handler: service.describeSQLQuery
  });

  server.route({
    path: ROUTE_PATH_PPL_QUERY,
    method: 'POST',
    handler: service.describePPLQuery
  });

  server.route({
    path: ROUTE_PATH_SQL_CSV,
    method: 'POST',
    handler: service.describeSQLCsv
  });

  server.route({
    path: ROUTE_PATH_PPL_CSV,
    method: 'POST',
    handler: service.describePPLCsv
  });

  server.route({
    path: ROUTE_PATH_SQL_JSON,
    method: 'POST',
    handler: service.describeSQLJson
  });

  server.route({
    path: ROUTE_PATH_PPL_JSON,
    method: 'POST',
    handler: service.describePPLJson
  });

  server.route({
    path: ROUTE_PATH_SQL_TEXT,
    method: 'POST',
    handler: service.describeSQLText
  });

  server.route({
    path: ROUTE_PATH_PPL_TEXT,
    method: 'POST',
    handler: service.describePPLText
  });
}
