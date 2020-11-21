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

import { schema } from '@kbn/config-schema';
import { IKibanaResponse, IRouter, ResponseError } from '../../../../src/core/server';
import QueryService from '../services/QueryService';
import { convertQueryToString } from '../services/utils/constants';
import {
  ROUTE_PATH_SQL_QUERY,
  ROUTE_PATH_PPL_QUERY,
  ROUTE_PATH_SQL_CSV,
  ROUTE_PATH_SQL_JSON,
  ROUTE_PATH_SQL_TEXT,
  ROUTE_PATH_PPL_CSV,
  ROUTE_PATH_PPL_JSON,
  ROUTE_PATH_PPL_TEXT,
} from '../utils/constants';

export default function query(server: IRouter, service: QueryService) {
  server.post(
    {
      path: ROUTE_PATH_SQL_QUERY,
      validate: {
        body: schema.any(),
      },
    },
    async (context, request, response): Promise<IKibanaResponse<any | ResponseError>> => {
      const queryString = convertQueryToString(request.url.query);

      const retVal = await service.describeSQLQuery(queryString);
      return response.ok({
        body: retVal,
      });
    }
  );

  server.post(
    {
      path: ROUTE_PATH_PPL_QUERY,
      validate: {
        body: schema.any(),
      },
    },
    async (context, request, response): Promise<IKibanaResponse<any | ResponseError>> => {
      const queryString = convertQueryToString(request.url.query);

      const retVal = await service.describePPLQuery(queryString);
      return response.ok({
        body: retVal,
      });
    }
  );

  server.post(
    {
      path: ROUTE_PATH_SQL_CSV,
      validate: {
        body: schema.any(),
      },
    },
    async (context, request, response): Promise<IKibanaResponse<any | ResponseError>> => {
      const queryString = convertQueryToString(request.url.query);

      const retVal = await service.describeSQLCsv(queryString);
      return response.ok({
        body: retVal,
      });
    }
  );

  server.post(
    {
      path: ROUTE_PATH_PPL_CSV,
      validate: {
        body: schema.any(),
      },
    },
    async (context, request, response): Promise<IKibanaResponse<any | ResponseError>> => {
      const queryString = convertQueryToString(request.url.query);

      const retVal = await service.describePPLCsv(queryString);
      return response.ok({
        body: retVal,
      });
    }
  );

  server.post(
    {
      path: ROUTE_PATH_SQL_JSON,
      validate: {
        body: schema.any(),
      },
    },
    async (context, request, response): Promise<IKibanaResponse<any | ResponseError>> => {
      const queryString = convertQueryToString(request.url.query);

      const retVal = await service.describeSQLJson(queryString);
      return response.ok({
        body: retVal,
      });
    }
  );

  server.post(
    {
      path: ROUTE_PATH_PPL_JSON,
      validate: {
        body: schema.any(),
      },
    },
    async (context, request, response): Promise<IKibanaResponse<any | ResponseError>> => {
      const queryString = convertQueryToString(request.url.query);

      const retVal = await service.describePPLJson(queryString);
      return response.ok({
        body: retVal,
      });
    }
  );

  server.post(
    {
      path: ROUTE_PATH_SQL_TEXT,
      validate: {
        body: schema.any(),
      },
    },
    async (context, request, response): Promise<IKibanaResponse<any | ResponseError>> => {
      const queryString = convertQueryToString(request.url.query);

      const retVal = await service.describeSQLText(queryString);
      return response.ok({
        body: retVal,
      });
    }
  );

  server.post(
    {
      path: ROUTE_PATH_PPL_TEXT,
      validate: {
        body: schema.any(),
      },
    },
    async (context, request, response): Promise<IKibanaResponse<any | ResponseError>> => {
      const queryString = convertQueryToString(request.url.query);

      const retVal = await service.describePPLText(queryString);
      return response.ok({
        body: retVal,
      });
    }
  );
}
