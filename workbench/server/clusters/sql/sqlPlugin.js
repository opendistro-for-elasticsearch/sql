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

import { SQL_TRANSLATE_ROUTE, SQL_QUERY_ROUTE, PPL_QUERY_ROUTE, PPL_TRANSLATE_ROUTE, FORMAT_CSV, FROMAT_JDBC, FORMAT_JSON, FORMAT_TEXT } from '../../services/utils/constants';

export default function sqlPlugin(Client, config, components) {
  const ca = components.clientAction.factory;

  Client.prototype.sql = components.clientAction.namespaceFactory();
  const sql = Client.prototype.sql.prototype;

  sql.translateSQL = ca({
    url: {
      fmt: `${SQL_TRANSLATE_ROUTE}`,
    },
    needBody: true,
    method: 'POST',
  });

  sql.translatePPL = ca({
    url: {
      fmt: `${PPL_TRANSLATE_ROUTE}`,
    },
    needBody: true,
    method: 'POST',
  });

  sql.sqlQuery = ca({
    url: {
      fmt: `${SQL_QUERY_ROUTE}`,
    },
    needBody: true,
    method: 'POST',
  }); //default: jdbc

  sql.pplQuery = ca({
    url: {
      fmt: `${PPL_QUERY_ROUTE}`,
    },
    needBody: true,
    method: 'POST',
  }); //default: jdbc

  sql.sqlJson = ca({
    url: {
      fmt: `${SQL_QUERY_ROUTE}?${FORMAT_JSON}`,
    },
    needBody: true,
    method: 'POST',
  });

  sql.pplJson = ca({
    url: {
      fmt: `${PPL_QUERY_ROUTE}?${FORMAT_JSON}`,
    },
    needBody: true,
    method: 'POST',
  });

  sql.sqlCsv = ca({
    url: {
      fmt: `${SQL_QUERY_ROUTE}?${FORMAT_CSV}`,
    },
    needBody: true,
    method: 'POST',
  });

  sql.pplCsv = ca({
    url: {
      fmt: `${PPL_QUERY_ROUTE}?${FORMAT_CSV}`,
    },
    needBody: true,
    method: 'POST',
  });

  sql.sqlText = ca({
    url: {
      fmt: `${SQL_QUERY_ROUTE}?${FORMAT_TEXT}`,
    },
    needBody: true,
    method: 'POST',
  });

  sql.pplText = ca({
    url: {
      fmt: `${PPL_QUERY_ROUTE}?${FORMAT_TEXT}`,
    },
    needBody: true,
    method: 'POST',
  });
}
