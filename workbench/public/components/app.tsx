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

import React from 'react';
import { I18nProvider } from '@kbn/i18n/react';
import { BrowserRouter as Router, Route } from 'react-router-dom';

import { EuiPage, EuiPageBody } from '@elastic/eui';

import { CoreStart } from '../../../../src/core/public';
import { NavigationPublicPluginStart } from '../../../../src/plugins/navigation/public';

import { Main } from './Main';

interface WorkbenchAppDeps {
  basename: string;
  notifications: CoreStart['notifications'];
  http: CoreStart['http'];
  navigation: NavigationPublicPluginStart;
}

const onChange = () => {};

export const WorkbenchApp = ({ basename, notifications, http, navigation }: WorkbenchAppDeps) => {
  return (
    <Router basename={'/' + basename}>
      <I18nProvider>
        <div>
          <EuiPage>
            <EuiPageBody>
              <Route path="/" render={(props) => <Main httpClient={http} {...props} />} />
            </EuiPageBody>
          </EuiPage>
        </div>
      </I18nProvider>
    </Router>
  );
};
