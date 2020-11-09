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
// import { schema } from 'packages/kbn-config-schema/target/types';
import { schema } from '@kbn/config-schema';
import { IKibanaResponse, IRouter, ResponseError } from '../../../../src/core/server';
import QueryService from '../services/QueryService';
import { ROUTE_PATH_SQL_QUERY, ROUTE_PATH_PPL_QUERY, ROUTE_PATH_SQL_CSV, ROUTE_PATH_SQL_JSON, ROUTE_PATH_SQL_TEXT, ROUTE_PATH_PPL_CSV, ROUTE_PATH_PPL_JSON, ROUTE_PATH_PPL_TEXT } from "../utils/constants";

export default function query(server: IRouter, service: QueryService) {
  // server.route({
  //   path: ROUTE_PATH_SQL_QUERY,
  //   method: 'POST',
  //   handler: service.describeSQLQuery
  // });
  server.post({
    path: ROUTE_PATH_SQL_QUERY,
    validate: {
      body: schema.any()
    }
  }, async(
    context,
    request,
    response
  ): Promise <IKibanaResponse<any | ResponseError>> => {
    console.log("request is", request.url.query);
    const queryString = convertQueryToString(request.url.query);
    console.log('query string is', queryString);
    // console.log("respnose is", response);
    // console.log(response.ok);
    // console.log(context.core.elasticsearch);

    const retVal = await service.describeSQLQuery(queryString, response);
    console.log('described sql query. retval is', retVal);
    return retVal;
  })

  // server.route({
  //   path: ROUTE_PATH_PPL_QUERY,
  //   method: 'POST',
  //   handler: service.describePPLQuery
  // });

  // server.route({
  //   path: ROUTE_PATH_SQL_CSV,
  //   method: 'POST',
  //   handler: service.describeSQLCsv
  // });

  // server.route({
  //   path: ROUTE_PATH_PPL_CSV,
  //   method: 'POST',
  //   handler: service.describePPLCsv
  // });

  // server.route({
  //   path: ROUTE_PATH_SQL_JSON,
  //   method: 'POST',
  //   handler: service.describeSQLJson
  // });

  // server.route({
  //   path: ROUTE_PATH_PPL_JSON,
  //   method: 'POST',
  //   handler: service.describePPLJson
  // });

  // server.route({
  //   path: ROUTE_PATH_SQL_TEXT,
  //   method: 'POST',
  //   handler: service.describeSQLText
  // });

  // server.route({
  //   path: ROUTE_PATH_PPL_TEXT,
  //   method: 'POST',
  //   handler: service.describePPLText
  // });
}

const convertQueryToString = (query) => {
  console.log('in query function, query is', query);
  console.log(query['0']);
  let index;
  var queryString = "";
  for (index = 0; index < Object.keys(query).length; ++index) {
    // queryString.concat(query[index.toString()]);
    console.log('in loop, queryString is', queryString);
    console.log('index to string is', query[index.toString()]);
    queryString += query[index.toString()];
  };
  return queryString;
}