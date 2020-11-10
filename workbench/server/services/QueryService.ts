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

import 'core-js/stable';
import 'regenerator-runtime/runtime';
import _ from 'lodash';

export default class QueryService {
  private client: any;
  constructor(client: any) {
    this.client = client;
  }

  describeQueryInternal = async (request: string, format: string, responseFormat: string) => {
    try {
      const queryRequest = {
        query: request,
      };
      const params = {
        body: JSON.stringify(queryRequest),
      };

      const queryResponse = await this.client.asScoped(request).callAsCurrentUser(format, params);
      return {
        data: {
          ok: true,
          resp: _.isEqual(responseFormat, 'json') ? JSON.stringify(queryResponse) : queryResponse,
        },
      };
    } catch (err) {
      console.log(err);
      return {
        data: {
          ok: false,
          resp: err.message,
        },
      };
    }
  };

  describeSQLQuery = async (request: string) => {
    return this.describeQueryInternal(request, 'sql.sqlQuery', 'json');
  };

  describePPLQuery = async (request: string) => {
    return this.describeQueryInternal(request, 'sql.pplQuery', 'json');
  };

  describeSQLCsv = async (request: string) => {
    return this.describeQueryInternal(request, 'sql.sqlCsv', null);
  };

  describePPLCsv = async (request: string) => {
    return this.describeQueryInternal(request, 'sql.pplCsv', null);
  };

  describeSQLJson = async (request: string) => {
    return this.describeQueryInternal(request, 'sql.sqlJson', 'json');
  };

  describePPLJson = async (request: string) => {
    return this.describeQueryInternal(request, 'sql.pplJson', 'json');
  };

  describeSQLText = async (request: string) => {
    return this.describeQueryInternal(request, 'sql.sqlText', null);
  };

  describePPLText = async (request: string) => {
    return this.describeQueryInternal(request, 'sql.pplText', null);
  };
}
