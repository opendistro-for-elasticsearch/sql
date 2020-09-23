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

import { Request, ResponseToolkit } from 'hapi-latest';
import { CLUSTER } from './utils/constants';

export default class TranslateService {
  private client: any;

  constructor(client: any) {
    this.client = client;
  }

  translateSQL = async (request: Request, h: ResponseToolkit, err?: Error) => {
    try {
      const params = {
        body: JSON.stringify(request.payload),
      };
      const { callWithRequest } = await this.client.getCluster(CLUSTER.SQL);
      const createResponse = await callWithRequest(request, 'sql.translateSQL', params);
      return h.response({ ok: true, resp: createResponse });

    } catch (err) {
      return h.response({ ok: false, resp: err.message });
    }
  };

  translatePPL = async (request: Request, h: ResponseToolkit, err?: Error) => {
    try {
      const params = {
        body: JSON.stringify(request.payload),
      };
      const { callWithRequest } = await this.client.getCluster(CLUSTER.SQL);
      const createResponse = await callWithRequest(request, 'sql.translatePPL', params);
      return h.response({ ok: true, resp: createResponse });

    } catch (err) {
      return h.response({ ok: false, resp: err.message });
    }
  };
}
