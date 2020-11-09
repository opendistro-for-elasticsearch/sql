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
import { IKibanaResponse, KibanaRequest } from "kibana/server";

export default class QueryService {
  private client: any;
  constructor(client: any) {
    this.client = client;
  }

  describeQueryInternal = async (request: string, response, format: string, responseFormat: string) => {
    try {
      const queryRequest = {
        query: request
      };
      const params = {
        body: JSON.stringify(queryRequest),
      };
      // console.log('request is', request);
      console.log("about to call get cluster");
      // console.log('client is', this.client);
      // const { callWithRequest } = await client.callAsCurrentUser(
      //   'search',
      //   {
      //     index: CLUSTER.SQL
      //   }
      // );    // client.getCluster(CLUSTER.SQL);
      const callWithRequest = await this.client.asScoped(request).callAsCurrentUser('sql.sqlQuery', params);
      console.log('callwithrequest is', callWithRequest);
      // return;
      // const createResponse = await callWithRequest(request, format, params);
      // return response.ok({
      //    resp:
      //     _.isEqual(responseFormat, "json") ? JSON.stringify(createResponse) : createResponse
      // });
      return response.ok({
        data: JSON.stringify(callWithRequest)
      })
    } catch (err) {
      console.log(err);
      // return h.response({ ok: false, resp: err.message });
    }
    // return h.response({ ok: false, resp: err.message });
  };

  describeSQLQuery = async (request: string, h) => {
    return this.describeQueryInternal(request, h, "sql.sqlQuery", "json");
  };

  // describePPLQuery = async (request: Request, h: ResponseToolkit, err?: Error) => {
  //   return this.describeQueryInternal(request, h, "sql.pplQuery", "json")
  // };

  // describeSQLCsv = async (request: Request, h: ResponseToolkit, err?: Error) => {
  //   return this.describeQueryInternal(request, h, "sql.sqlCsv", null)
  // };

  // describePPLCsv = async (request: Request, h: ResponseToolkit, err?: Error) => {
  //   return this.describeQueryInternal(request, h, "sql.pplCsv", null)
  // };

  // describeSQLJson = async (request: Request, h: ResponseToolkit, err?: Error) => {
  //   return this.describeQueryInternal(request, h, "sql.sqlJson", "json")
  // };

  // describePPLJson = async (request: Request, h: ResponseToolkit, err?: Error) => {
  //   return this.describeQueryInternal(request, h, "sql.pplJson", "json")
  // };

  // describeSQLText = async (request: Request, h: ResponseToolkit, err?: Error) => {
  //   return this.describeQueryInternal(request, h, "sql.sqlText", null)
  // };

  // describePPLText = async (request: Request, h: ResponseToolkit, err?: Error) => {
  //   return this.describeQueryInternal(request, h, "sql.pplText", null)
  // };
}
