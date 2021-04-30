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

export default class TranslateService {
  private client: any;

  constructor(client: any) {
    this.client = client;
  }

  translateSQL = async (request: any) => {
    try {
      const queryRequest = {
        query: request.body.query,
      };

      const params = {
        body: JSON.stringify(queryRequest),
      };

      const queryResponse = await this.client
        .asScoped(request)
        .callAsCurrentUser('sql.translateSQL', params);
      const ret = {
        data: {
          ok: true,
          resp: queryResponse,
        },
      };
      return ret;
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

  translatePPL = async (request: any) => {
    try {
      const queryRequest = {
        query: request.body.query,
      };

      const params = {
        body: JSON.stringify(queryRequest),
      };

      const queryResponse = await this.client
        .asScoped(request)
        .callAsCurrentUser('sql.translatePPL', params);
      return {
        data: {
          ok: true,
          resp: queryResponse,
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
}
