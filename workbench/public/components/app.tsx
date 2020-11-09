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

import React, { useState } from 'react';
import { i18n } from '@kbn/i18n';
import { FormattedMessage, I18nProvider } from '@kbn/i18n/react';
import { BrowserRouter as Router, Route } from 'react-router-dom';

import {
  EuiButton,
  EuiHorizontalRule,
  EuiPage,
  EuiPageBody,
  EuiPageContent,
  EuiPageContentBody,
  EuiPageContentHeader,
  EuiPageHeader,
  EuiTitle,
  EuiText,
} from '@elastic/eui';

import { CoreStart } from '../../../../src/core/public';
import { NavigationPublicPluginStart } from '../../../../src/plugins/navigation/public';

import { PLUGIN_ID, PLUGIN_NAME } from '../../common';
import { Main } from './Main';

interface WorkbenchAppDeps {
  basename: string;
  notifications: CoreStart['notifications'];
  http: CoreStart['http'];
  navigation: NavigationPublicPluginStart;
}

const onChange = () => {

}

export const WorkbenchApp = ({ basename, notifications, http, navigation }: WorkbenchAppDeps) => {
  // Use React hooks to manage state.


  // Render the application DOM.
  // Note that `navigation.ui.TopNavMenu` is a stateful component exported on the `navigation` plugin's start contract.
  return (
    <Router basename={'/' + basename}>
      <I18nProvider>
        <div>
          <EuiPage restrictWidth="1000px">
            <EuiPageBody>
              <EuiPageHeader>
                <EuiTitle size="l">
                  <h1>
                    <FormattedMessage
                      id="temp.helloWorldText"
                      defaultMessage="{name}"
                      values={{ name: PLUGIN_NAME }}
                    />
                  </h1>
                </EuiTitle>
              </EuiPageHeader>
              <Route
                path="/"
                render={(props) => (
                  <Main 
                    httpClient={http} 
                    {...props}
                    onChange={onChange}
                  />
                )}
              />
              {/* <EuiPageContent>
                <EuiPageContentHeader>
                  <EuiTitle>
                    <h2>
                      <FormattedMessage
                        id="temp.congratulationsTitle"
                        defaultMessage="Congratulations, you have successfully created a new Kibana Plugin!"
                      />
                    </h2>
                  </EuiTitle>
                </EuiPageContentHeader>
                <EuiPageContentBody>
                  <EuiText>
                    <p>
                      <FormattedMessage
                        id="temp.content"
                        defaultMessage="Look through the generated code and check out the plugin development documentation."
                      />
                    </p>
                    <EuiHorizontalRule />
                    <p>
                      <FormattedMessage
                        id="temp.timestampText"
                        defaultMessage="Last timestamp: {time}"
                        values={{ time: timestamp ? timestamp : 'Unknown' }}
                      />
                    </p>
                    <EuiButton type="primary" size="s" onClick={onClickHandler}>
                      <FormattedMessage id="temp.buttonText" defaultMessage="Get data" />
                    </EuiButton>
                  </EuiText>
                </EuiPageContentBody>
              </EuiPageContent> */}
            </EuiPageBody>
          </EuiPage>
        </div>
      </I18nProvider>
    </Router>
  );
};
