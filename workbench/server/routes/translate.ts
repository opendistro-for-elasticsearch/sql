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
import TranslateService from '../services/TranslateService';
import { CLUSTER } from '../services/utils/constants';

export default function translate(server: IRouter, service: TranslateService) {
  // server.post({
  //   path: '/api/sql_console/translatesql',
  //   method: 'POST',
  //   handler: service.translateSQL
  // });

  server.post({
    path: '/api/sql_console/translatesql',
    validate: {
      body: schema.any()
    }
  }, async (
    context,
    request,
    response
  ): Promise <IKibanaResponse<any | ResponseError>> => {
    service.translateSQL(request, response);
    return;
  });

  // server.route({
  //   path: '/api/sql_console/translateppl',
  //   method: 'POST',
  //   handler: service.translatePPL
  // });
  server.post({
    path: '/api/sql_console/translateppl',
    validate: {
      body: schema.any()
    }
  }, async (
    context,
    request,
    response
  ): Promise <IKibanaResponse<any | ResponseError>> => {
    service.translatePPL(request, response);
    return;
  })
}
