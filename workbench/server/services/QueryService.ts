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

import "core-js/stable";
import "regenerator-runtime/runtime";
import { Request, ResponseToolkit } from 'hapi-latest';
import { CLUSTER } from './utils/constants';
import _ from "lodash";

export default class QueryService {
  private client: any;
  constructor(client: any) {
    this.client = client;
  }

  describeQueryInternal = async (request: Request, h: ResponseToolkit, format: string, responseFormat: string, err?: Error) => {
    try {
      const params = {
        body: JSON.stringify(request.payload),
      };
      const { callWithRequest } = await this.client.getCluster(CLUSTER.SQL);
      const createResponse = await callWithRequest(request, format, params);
      return h.response({
        ok: true, resp:
          _.isEqual(responseFormat, "json") ? JSON.stringify(createResponse) : createResponse
      });
    } catch (err) {
      console.log(err);
    }
    return h.response({ ok: false, resp: err.message });
  };

  describeSQLQuery = async (request: Request, h: ResponseToolkit, err?: Error) => {
    return this.describeQueryInternal(request, h, "sql.sqlQuery", "json", err)
  };

  describePPLQuery = async (request: Request, h: ResponseToolkit, err?: Error) => {
    return this.describeQueryInternal(request, h, "sql.pplQuery", "json", err)
  };

  describeSQLCsv = async (request: Request, h: ResponseToolkit, err?: Error) => {
    return this.describeQueryInternal(request, h, "sql.sqlCsv", null, err)
  };

  describePPLCsv = async (request: Request, h: ResponseToolkit, err?: Error) => {
    return this.describeQueryInternal(request, h, "sql.pplCsv", null, err)
  };

  describeSQLJson = async (request: Request, h: ResponseToolkit, err?: Error) => {
    return this.describeQueryInternal(request, h, "sql.sqlJson", "json", err)
  };

  describePPLJson = async (request: Request, h: ResponseToolkit, err?: Error) => {
    return this.describeQueryInternal(request, h, "sql.pplJson", "json", err)
  };

  describeSQLText = async (request: Request, h: ResponseToolkit, err?: Error) => {
    return this.describeQueryInternal(request, h, "sql.sqlText", null, err)
  };

  describePPLText = async (request: Request, h: ResponseToolkit, err?: Error) => {
    return this.describeQueryInternal(request, h, "sql.pplText", null, err)
  };
}
