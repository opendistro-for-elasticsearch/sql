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

import { AppMountParameters, CoreSetup, CoreStart, Plugin } from '../../../src/core/public';
import { WorkbenchPluginSetup, WorkbenchPluginStart, AppPluginStartDependencies } from './types';
import { PLUGIN_NAME } from '../common';

export class WorkbenchPlugin implements Plugin<WorkbenchPluginSetup, WorkbenchPluginStart> {
  public setup(core: CoreSetup): WorkbenchPluginSetup {
    // Register an application into the side navigation menu
    core.application.register({
      id: 'opendistro-query-workbench',
      title: PLUGIN_NAME,
      category: {
        id: 'odfe',
        label: 'Open Distro for Elasticsearch',
        euiIconType: 'logoKibana',
        order: 2000,
      },
      order: 1000,
      async mount(params: AppMountParameters) {
        // Load application bundle
        const { renderApp } = await import('./application');
        // Get start services as specified in kibana.json
        const [coreStart, depsStart] = await core.getStartServices();
        // Render the application
        return renderApp(coreStart, depsStart as AppPluginStartDependencies, params);
      },
    });

    // Return methods that should be available to other plugins
    return {};
  }

  public start(core: CoreStart): WorkbenchPluginStart {
    return {};
  }

  public stop() {}
}
