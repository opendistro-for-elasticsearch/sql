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

import {resolve} from 'path';
import {existsSync} from "fs";

import query from './server/routes/query';
import translate from './server/routes/translate';
import QueryService from './server/services/QueryService';
import TranslateService from './server/services/TranslateService';
import { createSqlCluster } from './server/clusters';
import { DEFAULT_APP_CATEGORIES } from '../../src/core/utils';

export const PLUGIN_NAME = 'opendistro-query-workbench';

export default function (kibana) {
  return new kibana.Plugin({
    require: ['elasticsearch'],
    name: PLUGIN_NAME,
    uiExports: {
      app: {
        title: 'SQL Workbench',
        description: 'SQL Workbench',
        main: 'plugins/' + PLUGIN_NAME + '/app',
        icon:'plugins/' + PLUGIN_NAME + '/icons/sql.svg',
        order: 9050,
        category: DEFAULT_APP_CATEGORIES.kibana,
      },
      styleSheetPaths: [resolve(__dirname, 'public/app.scss')].find(p => existsSync(p))
    },

    config(Joi) {
      return Joi.object({
        enabled: Joi.boolean().default(true),
      }).default();
    },

    init(server, options) { // eslint-disable-line no-unused-vars
      // Create Clusters
      createSqlCluster(server);
      const client = server.plugins.elasticsearch;

      // Add server routes and initialize the plugin here
      query(server, new QueryService(client));
      translate(server, new TranslateService(client));
    }
  });
}
