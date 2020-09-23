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
import _ from "lodash";
// @ts-ignore
import {
  EuiPanel,
  EuiButton,
  EuiText,
  EuiFlexGroup,
  EuiFlexItem,
  EuiCodeEditor,
  EuiOverlayMask,
  EuiModal,
  EuiModalHeader,
  EuiModalHeaderTitle,
  EuiModalBody,
  EuiModalFooter,
  EuiCodeBlock
} from "@elastic/eui";
// @ts-ignore
import { htmlIdGenerator } from "@elastic/eui/lib/services";
import "brace/mode/sql";
import "brace/mode/json";
import "../../ace-themes/sql_console";
import { ResponseDetail, TranslateResult } from "../Main/main";

interface QueryEditorProps {
  onRun: (queriesString: string) => void;
  onTranslate: (queriesString: string) => void;
  onClear: () => void;
  queryTranslations: ResponseDetail<TranslateResult>[];
  sqlQueriesString: string;
}

interface QueryEditorState {
  sqlQueriesString: string;
  translationResult: string;
  isModalVisible: boolean
}

class QueryEditor extends React.Component<QueryEditorProps, QueryEditorState> {
  constructor(props: QueryEditorProps) {
    super(props);
    this.state = {
      sqlQueriesString: this.props.sqlQueriesString ? this.props.sqlQueriesString : "SHOW tables LIKE %;\n" + "DESCRIBE tables LIKE %;",
      translationResult: "",
      isModalVisible: false
    };

    this.updateSQLQueries = _.debounce(this.updateSQLQueries, 250).bind(this);
  }

  updateSQLQueries(newQueriesString: string): void {
    this.setState({ sqlQueriesString: newQueriesString });
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
              <EuiModalHeaderTitle>JSON Translation</EuiModalHeaderTitle>
            </EuiModalHeader>

            <EuiModalBody>
              <EuiCodeBlock
                language="json"
                fontSize="m"
                isCopyable
              >
                {this.props.queryTranslations.map((queryTranslation: any) => JSON.stringify(queryTranslation.data, null, 2)).join("\n")}
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
      <EuiPanel className="sql-console-query-editor container-panel"
        paddingSize="none"
      >
        <EuiFlexGroup gutterSize="s">
          <EuiFlexItem grow={1} className="sql-query-panel">
            <EuiText className="sql-query-panel-header">Query Editor</EuiText>
            <EuiCodeEditor
              mode="sql"
              theme="sql_console"
              width="100%"
              height="18.5rem"
              value={this.state.sqlQueriesString}
              onChange={this.updateSQLQueries}
              showPrintMargin={false}
              setOptions={{
                fontSize: "12px",
                enableBasicAutocompletion: true,
                enableSnippets: true,
                enableLiveAutocompletion: true
              }}
              aria-label="Code Editor"
            />
          </EuiFlexItem>
        </EuiFlexGroup>
        <div>
          <EuiFlexGroup className="action-container" gutterSize="m">
            <EuiFlexItem
              grow={false}
              onClick={() => this.props.onRun(this.state.sqlQueriesString)}
            >
              <EuiButton fill={true} className="sql-editor-button" >
                Run
              </EuiButton>
            </EuiFlexItem>
            <EuiFlexItem
              grow={false}
              onClick={() => {
                this.updateSQLQueries("");
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
                this.props.onTranslate(this.state.sqlQueriesString)
              }
            >
              <EuiButton className="sql-editor-button" onClick={showModal}>
                JSON Translation
              </EuiButton>
              {modal}
            </EuiFlexItem>
          </EuiFlexGroup>
        </div>
      </EuiPanel>
    );
  }
}

export default QueryEditor;
