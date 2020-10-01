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

import React from "react";
import {
  EuiPanel,
  EuiButton,
  EuiFlexGroup,
  EuiFlexItem,
  EuiOverlayMask,
  EuiModal,
  EuiModalHeader,
  EuiModalHeaderTitle,
  EuiModalBody,
  EuiModalFooter,
  EuiCodeBlock, EuiText, EuiCodeEditor
} from "@elastic/eui";
import { ResponseDetail, TranslateResult } from '../Main/main';
import _ from 'lodash';

interface PPLPageProps {
  onRun: (query: string) => void,
  onTranslate: (query: string) => void,
  onClear: () => void,
  updatePPLQueries: (query: string) => void,
  pplQuery: string,
  pplTranslations: ResponseDetail<TranslateResult>[]
}

interface PPLPageState {
  pplQuery: string,
  translation: string,
  isModalVisible: boolean
}

export class PPLPage extends React.Component<PPLPageProps, PPLPageState> {
  constructor(props: PPLPageProps) {
    super(props);
    this.state = {
      pplQuery: this.props.pplQuery,
      translation: "",
      isModalVisible: false
    };
  }

  setIsModalVisible(visible: boolean): void {
    this.setState({
      isModalVisible: visible
    })
  }

  render() {

    const closeModal = () => this.setIsModalVisible(false);
    const showModal = () => this.setIsModalVisible(true);

    let modal;

    if (this.state.isModalVisible) {
      modal = (
        <EuiOverlayMask onClick={closeModal}>
          <EuiModal onClose={closeModal}>
            <EuiModalHeader>
              <EuiModalHeaderTitle>Explain</EuiModalHeaderTitle>
            </EuiModalHeader>

            <EuiModalBody>
              <EuiCodeBlock
                language="json"
                fontSize="m"
                isCopyable
              >
                {this.props.pplTranslations.map((queryTranslation: any) => JSON.stringify(queryTranslation.data, null, 2)).join("\n")}
              </EuiCodeBlock>
            </EuiModalBody>

            <EuiModalFooter>
              <EuiButton onClick={closeModal} fill>
                Close
            </EuiButton>
            </EuiModalFooter>
          </EuiModal>
        </EuiOverlayMask>
      );
    }

    return (
      <EuiPanel className="ppl-query-editor container-panel"
        paddingSize="none"
      >
        <div className="sql-console-query-editor">
          <EuiPanel className="sql-query-editor container-panel"
            paddingSize="none"
          >
            <EuiText className="sql-query-panel-header">Query Editor</EuiText>
            <EuiCodeEditor
              mode="sql"
              theme="sql_console"
              width="100%"
              height="15rem"
              value={this.props.pplQuery}
              onChange={this.props.updatePPLQueries}
              showPrintMargin={false}
              setOptions={{
                fontSize: "12px",
                enableBasicAutocompletion: true,
                enableSnippets: true,
                enableLiveAutocompletion: true
              }}
              aria-label="Code Editor"
            />
          </EuiPanel>
        </div>
        <div>
          <EuiFlexGroup className="action-container" gutterSize="m">
            <EuiFlexItem className="sql-editor-buttons"
              grow={false}
              onClick={() => this.props.onRun(this.props.pplQuery)}
            >
              <EuiButton fill={true} className="sql-editor-button" >
                Run
              </EuiButton>
            </EuiFlexItem>
            <EuiFlexItem
              grow={false}
              onClick={() => {
                this.props.updatePPLQueries("");
                this.props.onClear();
              }}
            >
              <EuiButton className="sql-editor-button">
                Clear
              </EuiButton>
            </EuiFlexItem>
            <EuiFlexItem
              grow={false}
              onClick={() =>
                this.props.onTranslate(this.props.pplQuery)
              }
            >
              <EuiButton className="sql-editor-button" onClick={showModal}>
                Explain
              </EuiButton>
              {modal}
            </EuiFlexItem>
          </EuiFlexGroup>
        </div>
      </EuiPanel>
    )
  }
}