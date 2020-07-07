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
import {EuiSpacer} from "@elastic/eui";
import {IHttpResponse, IHttpService} from "angular";
import _ from "lodash";
import Header from "../Header/Header";
import QueryEditor from "../QueryEditor/QueryEditor";
import QueryResults from "../QueryResults/QueryResults";
import {getDefaultTabId, getDefaultTabLabel, getQueries, getSelectedResults, Tree} from "../../utils/utils";
import {MESSAGE_TAB_LABEL} from "../../utils/constants";

interface ResponseData {
  ok: boolean;
  resp: any;
}

export interface ResponseDetail<T> {
  fulfilled: boolean;
  errorMessage?: string;
  data?: T;
}

export type TranslateResult = { [key: string]: any };

export interface QueryMessage {
  text: any;
  className: string;
}

export type QueryResult = {
  fields: string[];
  records: { [key: string]: any }[];
  message: string;
};

export interface Tab {
  id: string;
  name: string;
  disabled: boolean;
}

export type ItemIdToExpandedRowMap = {[key: string]:{
  nodes: Tree;
  expandedRow?: {};
  selectedNodes?: {[key: string]: any};
}};

interface MainProps {
  httpClient: IHttpService;
  sqlQueriesString?: string;
}

interface MainState {
  queries: string[];
  queryTranslations: Array<ResponseDetail<TranslateResult>>;
  queryResultsTable: Array<ResponseDetail<QueryResult>>;
  queryResults: Array<ResponseDetail<string>>;
  queryResultsJSON: Array<ResponseDetail<string>>;
  queryResultsCSV: Array<ResponseDetail<string>>;
  queryResultsTEXT: Array<ResponseDetail<string>>;
  selectedTabName: string;
  selectedTabId: string;
  searchQuery: string;
  itemIdToExpandedRowMap: ItemIdToExpandedRowMap;
  messages: Array<QueryMessage>;
}

const SUCCESS_MESSAGE = "Success";

// It gets column names and row values to display in a Table from the json API response
export function getQueryResultsForTable(queryResults: ResponseDetail<string>[]): ResponseDetail<QueryResult>[] {
  return queryResults.map(
    ( queryResultResponseDetail: ResponseDetail<string> ): ResponseDetail<QueryResult> => {
      if (!queryResultResponseDetail.fulfilled) {
        return {
          fulfilled: queryResultResponseDetail.fulfilled,
          errorMessage: queryResultResponseDetail.errorMessage
        };
      } else {
        let databaseRecords: { [key: string]: any }[] = [];
        const responseObj = queryResultResponseDetail.data ? JSON.parse(queryResultResponseDetail.data) : '';
        let databaseFields: string[] = [];
        let fields: string[] = [];

        const schema: object[] = _.get(responseObj, 'schema');
        const datarows: any[][] = _.get(responseObj, 'datarows');
        let queryType = 'default';

        for (const column of schema.values()) {
          if (_.isEqual(_.get(column, 'name'), 'TABLE_NAME')) {
            queryType = 'show';
            for (const col of schema.values()) {
              if (_.isEqual(_.get(col, 'name'), 'DATA_TYPE'))
              queryType = 'describe';
            }
          }
        }

        switch (queryType) {
          case 'show':
            databaseFields[0] = 'TABLE_NAME';
            databaseFields.unshift('id');
            let index: number = -1;
            for (const [id, field] of schema.entries()) {
              if (_.eq(_.get(field, 'name'), 'TABLE_NAME')) {
                index = id;
                break;
              }
            }
            for (const [id, datarow] of datarows.entries()) {
              let databaseRecord: { [key: string]: any } = {};
              databaseRecord['id'] = id;
              databaseRecord['TABLE_NAME'] = datarow[index];
              databaseRecords.push(databaseRecord);
            }
            break;

          case 'describe':
          case 'default':
            for (const [id, field] of schema.entries()) {
              let alias: any = null;
              try {
                alias = _.get(field, 'alias');
              } catch (e) {
                console.log('No alias for field ' + field);
              } finally {
                fields[id] = !alias ?  _.get(field, 'name') : alias;
              }
            }
            databaseFields = fields;
            databaseFields.unshift("id");
            for (const [id, datarow] of datarows.entries()) {
              let databaseRecord: { [key: string]: any } = {};
              databaseRecord['id'] = id;
              for (const index of schema.keys()) {
                const fieldname = databaseFields[index+1];
                databaseRecord[fieldname] = datarow[index];
              }
              databaseRecords.push(databaseRecord);
            }
            break;

          default:
            let databaseRecord: { [key: string]: any } = {};
            databaseRecords.push(databaseRecord);
        }

        return {
          fulfilled: queryResultResponseDetail.fulfilled,
          data: {
            fields: databaseFields,
            records: databaseRecords,
            message: SUCCESS_MESSAGE
          }
        }
      }
    }
  );
}

export class Main extends React.Component<MainProps, MainState> {
  httpClient: IHttpService;
  // httpClient: any;

  constructor(props: MainProps) {
    super(props);

    this.state = {
      queries: [],
      queryTranslations: [],
      queryResultsTable: [],
      queryResults: [],
      queryResultsJSON: [],
      queryResultsCSV: [],
      queryResultsTEXT: [],
      selectedTabName: MESSAGE_TAB_LABEL,
      selectedTabId: MESSAGE_TAB_LABEL,
      searchQuery: "",
      itemIdToExpandedRowMap: {},
      messages: []
    };

    this.httpClient = this.props.httpClient;
  }

  processTranslateResponse(response: IHttpResponse<ResponseData>): ResponseDetail<TranslateResult> {
      if(!response){
        return{
          fulfilled: false,
          errorMessage: "no response",
          data: undefined
        }
      }
    if (!response.data.ok) {
      return {
        fulfilled: false,
        errorMessage: response.data.resp,
        data: undefined
      };
    }
    return {
      fulfilled: true,
      data: response.data.resp
    };
  }

  processQueryResponse(response: IHttpResponse<ResponseData>): ResponseDetail<string> {
    if(!response){
      return{
        fulfilled: false,
        errorMessage: "no response",
        data: ''
      }
    }
    if (!response.data.ok) {
      return {
        fulfilled: false,
        errorMessage: response.data.resp,
        data: ''
      };
    }

    return {
      fulfilled: true,
      data: response.data.resp
    };
  }

  onSelectedTabIdChange = (tab: Tab): void => {
    this.setState({
      selectedTabId: tab.id,
      selectedTabName: tab.name,
      searchQuery: " ",
      itemIdToExpandedRowMap: {}
    });
  };

  onQueryChange = ({query}: {query : any}) => {
    // Reset pagination state.
    this.setState({
      searchQuery: query,
      itemIdToExpandedRowMap: {},
    });
  };

  updateExpandedMap = (map: ItemIdToExpandedRowMap): void => {
    this.setState({ itemIdToExpandedRowMap: map });
  };

  // It returns the error or successful message to display in the Message Tab
  getMessage( queryResultsForTable: ResponseDetail<QueryResult>[] ): Array<QueryMessage> {
    return queryResultsForTable.map(queryResult => {
      return {

        text: queryResult.fulfilled && queryResult.data ? queryResult.data.message : queryResult.errorMessage,
        className: queryResult.fulfilled ? "successful-message" : "error-message"
      };
    });
  }

  getTranslateMessage( translationResult: ResponseDetail<TranslateResult>[] ): Array<QueryMessage> {
    return translationResult.map(translation => {
      return {
        text: translation.data ? SUCCESS_MESSAGE : translation.errorMessage,
        className: translation.fulfilled ? "successful-message" : "error-message"
      }
    });
  }

  onRun = (queriesString: string): void => {
    const queries: string[] = getQueries(queriesString);

    if (queries.length > 0) {

      const responsePromise = Promise.all(
        queries.map((query: string) =>
          this.httpClient
            .post("../api/sql_console/query", {query})
            .catch((error: any) => {
              this.setState({
                messages: [
                  {
                    text: error.message,
                    className: "error-message"
                  }
                ]
              });
            })
        )
      );

      const translationPromise = Promise.all(
        queries.map((query: string) =>
          this.httpClient
            .post("../api/sql_console/translate", {query})
            .catch((error: any) => {
              this.setState({
                messages: [
                  {
                    text: error.message,
                    className: "error-message"
                  }
                ]
              });
            })
        )
      );

      Promise.all([responsePromise, translationPromise]).then(([response, translationResponse]) => {
        const results: ResponseDetail<string>[] = response.map(response =>
          this.processQueryResponse(response as IHttpResponse<ResponseData>));
        const resultTable: ResponseDetail<QueryResult>[] = getQueryResultsForTable(results);
        const translationResult: ResponseDetail<TranslateResult>[] = translationResponse.map(translationResponse =>
          this.processTranslateResponse(translationResponse as IHttpResponse<ResponseData>));

        this.setState({
          queries: queries,
          queryResults: results,
          queryTranslations: translationResult,
          queryResultsTable: resultTable,
          selectedTabId: getDefaultTabId(results),
          selectedTabName: getDefaultTabLabel(results, queries[0]),
          messages: this.getMessage(resultTable),
          itemIdToExpandedRowMap: {},
          queryResultsJSON: [],
          queryResultsCSV: [],
          queryResultsTEXT: [],
          searchQuery: ""
        }, () => console.log("Successfully updated the states")); // added callback function to handle async issues
      })

    }
  };

  onTranslate = (queriesString: string): void => {
    const queries: string[] = getQueries(queriesString);

    if (queries.length > 0) {

      const translationPromise = Promise.all(
        queries.map((query: string) =>
          this.httpClient
            .post("../api/sql_console/translate", {query})
            .catch((error: any) => {
              this.setState({
                messages: [
                  {
                    text: error.message,
                    className: "error-message"
                  }
                ]
              });
            })
        )
      );

      Promise.all([translationPromise]).then(([translationResponse]) => {
        const translationResult: ResponseDetail<TranslateResult>[] = translationResponse.map(translationResponse =>
          this.processTranslateResponse(translationResponse as IHttpResponse<ResponseData>));
        const shouldCleanResults = queries == this.state.queries;
        if (shouldCleanResults) {
          this.setState({
            queries,
            queryTranslations: translationResult,
            messages: this.getTranslateMessage(translationResult)
          })
        } else {
          this.setState({
            queries,
            queryTranslations: translationResult,
            messages: this.getTranslateMessage(translationResult),

            // clean all the results generated from the last cached query
            queryResults: [],
            queryResultsTable: [],
            selectedTabName: MESSAGE_TAB_LABEL,
            selectedTabId: MESSAGE_TAB_LABEL,
            itemIdToExpandedRowMap: {},
            queryResultsJSON: [],
            queryResultsCSV: [],
            queryResultsTEXT: [],
            searchQuery: ""
          }, () => console.log("Successfully updated the states"))
        }
      });
    }
  };

  getJson = (queries: string[]): void => {
    if (queries.length > 0) {
      Promise.all(
        queries.map((query: string) =>
          this.httpClient
            .post("../api/sql_console/queryjson", {query})
            .catch((error: any) => {
              this.setState({
                messages: [
                  {
                    text: error.message,
                    className: "error-message"
                  }
                ]
              });
            })
        )
      ).then(
        response => {
          const results: ResponseDetail<string>[] = response.map(response =>
            this.processQueryResponse(response as IHttpResponse<ResponseData>));
          this.setState({
            queries,
            queryResultsJSON: results
          }, () => console.log("Successfully updated the states"));
        }
      )
    }
  };

  getJdbc = (queries: string[]): void => {
    if (queries.length > 0) {
      Promise.all(
        queries.map((query: string) =>
          this.httpClient
            .post("../api/sql_console/query", {query})
            .catch((error: any) => {
              this.setState({
                messages: [
                  {
                    text: error.message,
                    className: "error-message"
                  }
                ]
              });
            })
        )
      ).then(
        jdbcResponse => {
          const jdbcResult: ResponseDetail<string>[] = jdbcResponse.map(jdbcResponse =>
            this.processQueryResponse(jdbcResponse as IHttpResponse<ResponseData>));
          this.setState({
            queries,
            queryResults: jdbcResult
          }, () => console.log("Successfully updated the states"));
        }
      )
    }
  };

  getCsv = (queries: string[]): void => {
    if (queries.length > 0) {
      Promise.all(
        queries.map((query: string) =>
          this.httpClient
            .post("../api/sql_console/querycsv", {query})
            .catch((error: any) => {
              this.setState({
                messages: [
                  {
                    text: error.message,
                    className: "error-message"
                  }
                ]
              });
            })
        )
      ).then(
        csvResponse => {
          const csvResult: ResponseDetail<string>[] = csvResponse.map(csvResponse =>
            this.processQueryResponse(csvResponse as IHttpResponse<ResponseData>));
          this.setState({
            queries,
            queryResultsCSV: csvResult
          }, () => console.log("Successfully updated the states"));
        }
      )
    }
  };

  getText = (queries: string[]): void => {
    if (queries.length > 0) {
      Promise.all(
        queries.map((query: string) =>
          this.httpClient
            .post("../api/sql_console/querytext", {query})
            .catch((error: any) => {
              this.setState({
                messages: [
                  {
                    text: error.message,
                    className: "error-message"
                  }
                ]
              });
            })
        )
      ).then(
        textResponse => {
          const textResult: ResponseDetail<string>[] = textResponse.map(textResponse =>
            this.processQueryResponse(textResponse as IHttpResponse<ResponseData>));
          this.setState({
            queries,
            queryResultsTEXT: textResult
          }, () => console.log("Successfully updated the states"));
        }
      )
    }
  };

  onClear = (): void => {
    this.setState({
      queries: [],
      queryTranslations: [],
      queryResultsTable: [],
      queryResults: [],
      queryResultsCSV: [],
      queryResultsJSON: [],
      queryResultsTEXT: [],
      messages: [],
      selectedTabId: MESSAGE_TAB_LABEL,
      selectedTabName: MESSAGE_TAB_LABEL,
      itemIdToExpandedRowMap: {}
    });
  };

  render() {
    return (
      <div>
        <Header />
        <div className="sql-console-query-container">
          <div className="sql-console-query-editor">
            <QueryEditor
              onRun={this.onRun}
              onTranslate={this.onTranslate}
              onClear={this.onClear}
              sqlQueriesString={this.props.sqlQueriesString ? this.props.sqlQueriesString : '' }
              queryTranslations={this.state.queryTranslations}
            />
          </div>

          <EuiSpacer size="l" />
          <div className="sql-console-query-result">
            <QueryResults
              queries={this.state.queries}
              queryResults={this.state.queryResultsTable}
              queryResultsJDBC={getSelectedResults(this.state.queryResults, this.state.selectedTabId)}
              queryResultsJSON={getSelectedResults(this.state.queryResultsJSON, this.state.selectedTabId)}
              queryResultsCSV={getSelectedResults(this.state.queryResultsCSV, this.state.selectedTabId)}
              queryResultsTEXT={getSelectedResults(this.state.queryResultsTEXT, this.state.selectedTabId)}
              messages={this.state.messages}
              selectedTabId={this.state.selectedTabId}
              selectedTabName={this.state.selectedTabName}
              onSelectedTabIdChange={this.onSelectedTabIdChange}
              itemIdToExpandedRowMap={this.state.itemIdToExpandedRowMap}
              onQueryChange={this.onQueryChange}
              updateExpandedMap={this.updateExpandedMap}
              searchQuery={this.state.searchQuery}
              tabsOverflow={false}
              getJson={this.getJson}
              getJdbc={this.getJdbc}
              getCsv={this.getCsv}
              getText={this.getText}
            />
          </div>
        </div>
      </div>
    );
  }
}

export default Main;
