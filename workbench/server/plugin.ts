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

import {
  PluginInitializerContext,
  CoreSetup,
  CoreStart,
  Plugin,
  Logger,
  ILegacyClusterClient,
} from '../../../src/core/server';

import { WorkbenchPluginSetup, WorkbenchPluginStart } from './types';
import defineRoutes from './routes';
import sqlPlugin from './clusters/sql/sqlPlugin';


export class WorkbenchPlugin implements Plugin<WorkbenchPluginSetup, WorkbenchPluginStart> {
  private readonly logger: Logger;

  constructor(initializerContext: PluginInitializerContext) {
    this.logger = initializerContext.logger.get();
  }

  public setup(core: CoreSetup) {
    this.logger.debug('temp: Setup');
    const router = core.http.createRouter();
    const client: ILegacyClusterClient = core.elasticsearch.legacy.createClient(
      'query_workbench',
      {
        plugins: [sqlPlugin]
      }
    )

    // Register server side APIs
    defineRoutes(router, client);

    return {};
  }

  public start(core: CoreStart) {
    this.logger.debug('temp: Started');
    return {};
  }

  public stop() {}
}
