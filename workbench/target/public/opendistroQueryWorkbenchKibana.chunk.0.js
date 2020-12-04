(window["opendistroQueryWorkbenchKibana_bundle_jsonpfunction"] = window["opendistroQueryWorkbenchKibana_bundle_jsonpfunction"] || []).push([[0],{

/***/ "./public/ace-themes/sql_console.js":
/*!******************************************!*\
  !*** ./public/ace-themes/sql_console.js ***!
  \******************************************/
/*! no exports provided */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var brace__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! brace */ "./node_modules/brace/index.js");
/* harmony import */ var brace__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(brace__WEBPACK_IMPORTED_MODULE_0__);
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

brace__WEBPACK_IMPORTED_MODULE_0__["define"]('ace/theme/sql_console', ['require', 'exports', 'module', 'ace/lib/dom'], function (acequire, exports, module) {
  exports.isDark = false;
  exports.cssClass = 'ace-sql-console';
  exports.cssText = __webpack_require__(/*! ../index.scss */ "./public/index.scss");
  const dom = acequire('../lib/dom');
  dom.importCssString(exports.cssText, exports.cssClass);
});

/***/ }),

/***/ "./public/application.tsx":
/*!********************************!*\
  !*** ./public/application.tsx ***!
  \********************************/
/*! exports provided: renderApp */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "renderApp", function() { return renderApp; });
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var react_dom__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! react-dom */ "react-dom");
/* harmony import */ var react_dom__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(react_dom__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var _components_app__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./components/app */ "./public/components/app.tsx");
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



const renderApp = ({
  notifications,
  http
}, {
  navigation
}, {
  appBasePath,
  element
}) => {
  react_dom__WEBPACK_IMPORTED_MODULE_1___default.a.render( /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_components_app__WEBPACK_IMPORTED_MODULE_2__["WorkbenchApp"], {
    basename: appBasePath,
    notifications: notifications,
    http: http,
    navigation: navigation
  }), element);
  return () => react_dom__WEBPACK_IMPORTED_MODULE_1___default.a.unmountComponentAtNode(element);
};

/***/ }),

/***/ "./public/components/Main/index.ts":
/*!*****************************************!*\
  !*** ./public/components/Main/index.ts ***!
  \*****************************************/
/*! exports provided: Main */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _main__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./main */ "./public/components/Main/main.tsx");
/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "Main", function() { return _main__WEBPACK_IMPORTED_MODULE_0__["Main"]; });

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


/***/ }),

/***/ "./public/components/Main/main.tsx":
/*!*****************************************!*\
  !*** ./public/components/Main/main.tsx ***!
  \*****************************************/
/*! exports provided: getQueryResultsForTable, Main, default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "getQueryResultsForTable", function() { return getQueryResultsForTable; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "Main", function() { return Main; });
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _elastic_eui__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @elastic/eui */ "@elastic/eui");
/* harmony import */ var _elastic_eui__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var lodash__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! lodash */ "lodash");
/* harmony import */ var lodash__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(lodash__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var _QueryResults_QueryResults__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../QueryResults/QueryResults */ "./public/components/QueryResults/QueryResults.tsx");
/* harmony import */ var _QueryLanguageSwitch_Switch__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../QueryLanguageSwitch/Switch */ "./public/components/QueryLanguageSwitch/Switch.tsx");
/* harmony import */ var _SQLPage_SQLPage__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ../SQLPage/SQLPage */ "./public/components/SQLPage/SQLPage.tsx");
/* harmony import */ var _PPLPage_PPLPage__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ../PPLPage/PPLPage */ "./public/components/PPLPage/PPLPage.tsx");
/* harmony import */ var _utils_utils__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! ../../utils/utils */ "./public/utils/utils.ts");
/* harmony import */ var _utils_constants__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! ../../utils/constants */ "./public/utils/constants.ts");
function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

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









const SUCCESS_MESSAGE = 'Success';

const errorQueryResponse = queryResultResponseDetail => {
  let errorMessage = queryResultResponseDetail.errorMessage + ', this query is not runnable. \n \n' + queryResultResponseDetail.data;
  return errorMessage;
};

function getQueryResultsForTable(queryResults) {
  return queryResults.map(queryResultResponseDetail => {
    if (!queryResultResponseDetail.fulfilled) {
      return {
        fulfilled: queryResultResponseDetail.fulfilled,
        errorMessage: errorQueryResponse(queryResultResponseDetail)
      };
    } else {
      // let resultTable: Table;
      const responseObj = queryResultResponseDetail.data ? JSON.parse(queryResultResponseDetail.data) : '';
      let fields = [];
      let dataRows = [];

      const schema = lodash__WEBPACK_IMPORTED_MODULE_2___default.a.get(responseObj, 'schema');

      const datarows = lodash__WEBPACK_IMPORTED_MODULE_2___default.a.get(responseObj, 'datarows');

      let queryType = 'default';

      for (const column of schema.values()) {
        if (lodash__WEBPACK_IMPORTED_MODULE_2___default.a.isEqual(lodash__WEBPACK_IMPORTED_MODULE_2___default.a.get(column, 'name'), 'TABLE_NAME')) {
          queryType = 'show';

          for (const col of schema.values()) {
            if (lodash__WEBPACK_IMPORTED_MODULE_2___default.a.isEqual(lodash__WEBPACK_IMPORTED_MODULE_2___default.a.get(col, 'name'), 'DATA_TYPE')) queryType = 'describe';
          }
        }
      }

      switch (queryType) {
        case 'show':
          fields[0] = 'TABLE_NAME';
          let index = -1;

          for (const [id, field] of schema.entries()) {
            if (lodash__WEBPACK_IMPORTED_MODULE_2___default.a.eq(lodash__WEBPACK_IMPORTED_MODULE_2___default.a.get(field, 'name'), 'TABLE_NAME')) {
              index = id;
              break;
            }
          }

          for (const [id, field] of datarows.entries()) {
            let row = {};
            row['TABLE_NAME'] = field[index];
            let dataRow = {
              rowId: id,
              data: row
            };
            dataRows[id] = dataRow;
          }

          break;

        case 'describe':
        case 'default':
          for (const [id, field] of schema.entries()) {
            let alias = null;

            try {
              alias = lodash__WEBPACK_IMPORTED_MODULE_2___default.a.get(field, 'alias');
            } catch (e) {
              console.log('No alias for field ' + field);
            } finally {
              fields[id] = !alias ? lodash__WEBPACK_IMPORTED_MODULE_2___default.a.get(field, 'name') : alias;
            }
          }

          for (const [id, data] of datarows.entries()) {
            let row = {};

            for (const index of schema.keys()) {
              const fieldname = fields[index];
              row[fieldname] = data[index];
            }

            let dataRow = {
              rowId: id,
              data: row
            };
            dataRows[id] = dataRow;
          }

          break;

        default:
      }

      return {
        fulfilled: queryResultResponseDetail.fulfilled,
        data: {
          fields: fields,
          records: dataRows,
          message: SUCCESS_MESSAGE
        }
      };
    }
  });
}
class Main extends react__WEBPACK_IMPORTED_MODULE_0___default.a.Component {
  constructor(props) {
    super(props);

    _defineProperty(this, "httpClient", void 0);

    _defineProperty(this, "onSelectedTabIdChange", tab => {
      this.setState({
        selectedTabId: tab.id,
        selectedTabName: tab.name,
        searchQuery: '',
        itemIdToExpandedRowMap: {}
      });
    });

    _defineProperty(this, "onQueryChange", ({
      query
    }) => {
      // Reset pagination state.
      this.setState({
        searchQuery: query,
        itemIdToExpandedRowMap: {}
      });
    });

    _defineProperty(this, "updateExpandedMap", map => {
      this.setState({
        itemIdToExpandedRowMap: map
      });
    });

    _defineProperty(this, "onRun", queriesString => {
      const queries = Object(_utils_utils__WEBPACK_IMPORTED_MODULE_7__["getQueries"])(queriesString);
      const language = this.state.language;

      if (queries.length > 0) {
        let endpoint = '../api/sql_console/' + (lodash__WEBPACK_IMPORTED_MODULE_2___default.a.isEqual(language, 'SQL') ? 'sqlquery' : 'pplquery');
        const responsePromise = Promise.all(queries.map(query => this.httpClient.post(endpoint, {
          query
        }).catch(error => {
          this.setState({
            messages: [{
              text: error.message,
              className: 'error-message'
            }]
          });
        })));
        Promise.all([responsePromise]).then(([response]) => {
          const results = response.map(response => this.processQueryResponse(response));
          const resultTable = getQueryResultsForTable(results);
          this.setState({
            queries: queries,
            queryResults: results,
            queryResultsTable: resultTable,
            selectedTabId: Object(_utils_utils__WEBPACK_IMPORTED_MODULE_7__["getDefaultTabId"])(results),
            selectedTabName: Object(_utils_utils__WEBPACK_IMPORTED_MODULE_7__["getDefaultTabLabel"])(results, queries[0]),
            messages: this.getMessage(resultTable),
            itemIdToExpandedRowMap: {},
            queryResultsJSON: [],
            queryResultsCSV: [],
            queryResultsTEXT: [],
            searchQuery: ''
          }, () => console.log('Successfully updated the states')); // added callback function to handle async issues
        });
      }
    });

    _defineProperty(this, "onTranslate", queriesString => {
      const queries = Object(_utils_utils__WEBPACK_IMPORTED_MODULE_7__["getQueries"])(queriesString);
      const language = this.state.language;

      if (queries.length > 0) {
        let endpoint = '../api/sql_console/' + (lodash__WEBPACK_IMPORTED_MODULE_2___default.a.isEqual(language, 'SQL') ? 'translatesql' : 'translateppl');
        const translationPromise = Promise.all(queries.map(query => this.httpClient.post(endpoint, {
          query
        }).catch(error => {
          this.setState({
            messages: [{
              text: error.message,
              className: 'error-message'
            }]
          });
        })));
        Promise.all([translationPromise]).then(([translationResponse]) => {
          const translationResult = translationResponse.map(translationResponse => this.processTranslateResponse(translationResponse));
          const shouldCleanResults = queries == this.state.queries;

          if (shouldCleanResults) {
            this.setState({
              queries,
              queryTranslations: translationResult,
              messages: this.getTranslateMessage(translationResult)
            });
          } else {
            this.setState({
              queries,
              queryTranslations: translationResult,
              messages: this.getTranslateMessage(translationResult)
            }, () => console.log('Successfully updated the states'));
          }
        });
      }
    });

    _defineProperty(this, "getJson", queries => {
      if (queries.length > 0) {
        Promise.all(queries.map(query => this.httpClient.post('../api/sql_console/queryjson', {
          query
        }).catch(error => {
          this.setState({
            messages: [{
              text: error.message,
              className: 'error-message'
            }]
          });
        }))).then(response => {
          const results = response.map(response => this.processQueryResponse(response));
          this.setState({
            queries,
            queryResultsJSON: results
          }, () => console.log('Successfully updated the states'));
        });
      }
    });

    _defineProperty(this, "getJdbc", queries => {
      const language = this.state.language;

      if (queries.length > 0) {
        let endpoint = '../api/sql_console/' + (lodash__WEBPACK_IMPORTED_MODULE_2___default.a.isEqual(language, 'SQL') ? 'sqlquery' : 'pplquery');
        Promise.all(queries.map(query => this.httpClient.post(endpoint, {
          query
        }).catch(error => {
          this.setState({
            messages: [{
              text: error.message,
              className: 'error-message'
            }]
          });
        }))).then(jdbcResponse => {
          const jdbcResult = jdbcResponse.map(jdbcResponse => this.processQueryResponse(jdbcResponse));
          this.setState({
            queries,
            queryResults: jdbcResult
          }, () => console.log('Successfully updated the states'));
        });
      }
    });

    _defineProperty(this, "getCsv", queries => {
      const language = this.state.language;

      if (queries.length > 0) {
        let endpoint = '../api/sql_console/' + (lodash__WEBPACK_IMPORTED_MODULE_2___default.a.isEqual(language, 'SQL') ? 'sqlcsv' : 'pplcsv');
        Promise.all(queries.map(query => this.httpClient.post(endpoint, {
          query
        }).catch(error => {
          this.setState({
            messages: [{
              text: error.message,
              className: 'error-message'
            }]
          });
        }))).then(csvResponse => {
          const csvResult = csvResponse.map(csvResponse => this.processQueryResponse(csvResponse));
          this.setState({
            queries,
            queryResultsCSV: csvResult
          }, () => console.log('Successfully updated the states'));
        });
      }
    });

    _defineProperty(this, "getText", queries => {
      const language = this.state.language;

      if (queries.length > 0) {
        let endpoint = '../api/sql_console/' + (lodash__WEBPACK_IMPORTED_MODULE_2___default.a.isEqual(language, 'SQL') ? 'sqltext' : 'ppltext');
        Promise.all(queries.map(query => this.httpClient.post(endpoint, {
          query
        }).catch(error => {
          this.setState({
            messages: [{
              text: error.message,
              className: 'error-message'
            }]
          });
        }))).then(textResponse => {
          const textResult = textResponse.map(textResponse => this.processQueryResponse(textResponse));
          this.setState({
            queries,
            queryResultsTEXT: textResult
          }, () => console.log('Successfully updated the states'));
        });
      }
    });

    _defineProperty(this, "onClear", () => {
      this.setState({
        queries: [],
        queryTranslations: [],
        queryResultsTable: [],
        queryResults: [],
        queryResultsCSV: [],
        queryResultsJSON: [],
        queryResultsTEXT: [],
        messages: [],
        selectedTabId: _utils_constants__WEBPACK_IMPORTED_MODULE_8__["MESSAGE_TAB_LABEL"],
        selectedTabName: _utils_constants__WEBPACK_IMPORTED_MODULE_8__["MESSAGE_TAB_LABEL"],
        itemIdToExpandedRowMap: {}
      });
    });

    _defineProperty(this, "onChange", id => {
      this.setState({
        language: id,
        queryResultsTable: []
      }, () => console.log('Successfully updated language to ', this.state.language)); // added callback function to handle async issues
    });

    this.onChange = this.onChange.bind(this);
    this.state = {
      language: 'SQL',
      sqlQueriesString: 'SHOW tables LIKE %;',
      pplQueriesString: '',
      queries: [],
      queryTranslations: [],
      queryResultsTable: [],
      queryResults: [],
      queryResultsJSON: [],
      queryResultsCSV: [],
      queryResultsTEXT: [],
      selectedTabName: _utils_constants__WEBPACK_IMPORTED_MODULE_8__["MESSAGE_TAB_LABEL"],
      selectedTabId: _utils_constants__WEBPACK_IMPORTED_MODULE_8__["MESSAGE_TAB_LABEL"],
      searchQuery: '',
      itemIdToExpandedRowMap: {},
      messages: [],
      isResultFullScreen: false
    };
    this.httpClient = this.props.httpClient;
    this.updateSQLQueries = lodash__WEBPACK_IMPORTED_MODULE_2___default.a.debounce(this.updateSQLQueries, 250).bind(this);
    this.updatePPLQueries = lodash__WEBPACK_IMPORTED_MODULE_2___default.a.debounce(this.updatePPLQueries, 250).bind(this);
    this.setIsResultFullScreen = this.setIsResultFullScreen.bind(this);
  }

  processTranslateResponse(response) {
    if (!response) {
      return {
        fulfilled: false,
        errorMessage: 'no response',
        data: undefined
      };
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

  formatQueryErrorBody(data) {
    let prettyErrorMessage = "";
    prettyErrorMessage += 'reason: ' + data.errorReason + '\n';
    prettyErrorMessage += 'details: ' + data.errorDetails + '\n';
    prettyErrorMessage += 'type: ' + data.errorType + '\n';
    prettyErrorMessage += 'status: ' + data.status;
    return prettyErrorMessage;
  }

  processQueryResponse(response) {
    if (!response) {
      return {
        fulfilled: false,
        errorMessage: 'no response',
        data: ''
      };
    }

    if (!response.data.ok) {
      return {
        fulfilled: false,
        errorMessage: response.data.resp,
        data: this.formatQueryErrorBody(response.data)
      };
    }

    return {
      fulfilled: true,
      data: response.data.resp
    };
  }

  // It returns the error or successful message to display in the Message Tab
  getMessage(queryResultsForTable) {
    return queryResultsForTable.map(queryResult => {
      return {
        text: queryResult.fulfilled && queryResult.data ? queryResult.data.message : queryResult.errorMessage,
        className: queryResult.fulfilled ? 'successful-message' : 'error-message'
      };
    });
  }

  getTranslateMessage(translationResult) {
    return translationResult.map(translation => {
      return {
        text: translation.data ? SUCCESS_MESSAGE : translation.errorMessage,
        className: translation.fulfilled ? 'successful-message' : 'error-message'
      };
    });
  }

  updateSQLQueries(query) {
    this.setState({
      sqlQueriesString: query
    });
  }

  updatePPLQueries(query) {
    this.setState({
      pplQueriesString: query
    });
  }

  setIsResultFullScreen(isFullScreen) {
    this.setState({
      isResultFullScreen: isFullScreen
    });
  }

  render() {
    let page;
    let link;
    let linkTitle;

    if (this.state.language == 'SQL') {
      page = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_SQLPage_SQLPage__WEBPACK_IMPORTED_MODULE_5__["SQLPage"], {
        onRun: this.onRun,
        onTranslate: this.onTranslate,
        onClear: this.onClear,
        sqlQuery: this.state.sqlQueriesString,
        sqlTranslations: this.state.queryTranslations,
        updateSQLQueries: this.updateSQLQueries
      });
      link = 'https://opendistro.github.io/for-elasticsearch-docs/docs/sql/';
      linkTitle = 'SQL documentation';
    } else {
      page = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_PPLPage_PPLPage__WEBPACK_IMPORTED_MODULE_6__["PPLPage"], {
        onRun: this.onRun,
        onTranslate: this.onTranslate,
        onClear: this.onClear,
        pplQuery: this.state.pplQueriesString,
        pplTranslations: this.state.queryTranslations,
        updatePPLQueries: this.updatePPLQueries
      });
      link = 'https://opendistro.github.io/for-elasticsearch-docs/docs/ppl/';
      linkTitle = 'PPL documentation';
    }

    if (this.state.isResultFullScreen) {
      return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
        className: "sql-console-query-result"
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_QueryResults_QueryResults__WEBPACK_IMPORTED_MODULE_3__["default"], {
        language: this.state.language,
        queries: this.state.queries,
        queryResults: this.state.queryResultsTable,
        queryResultsJDBC: Object(_utils_utils__WEBPACK_IMPORTED_MODULE_7__["getSelectedResults"])(this.state.queryResults, this.state.selectedTabId),
        queryResultsJSON: Object(_utils_utils__WEBPACK_IMPORTED_MODULE_7__["getSelectedResults"])(this.state.queryResultsJSON, this.state.selectedTabId),
        queryResultsCSV: Object(_utils_utils__WEBPACK_IMPORTED_MODULE_7__["getSelectedResults"])(this.state.queryResultsCSV, this.state.selectedTabId),
        queryResultsTEXT: Object(_utils_utils__WEBPACK_IMPORTED_MODULE_7__["getSelectedResults"])(this.state.queryResultsTEXT, this.state.selectedTabId),
        messages: this.state.messages,
        selectedTabId: this.state.selectedTabId,
        selectedTabName: this.state.selectedTabName,
        onSelectedTabIdChange: this.onSelectedTabIdChange,
        itemIdToExpandedRowMap: this.state.itemIdToExpandedRowMap,
        onQueryChange: this.onQueryChange,
        updateExpandedMap: this.updateExpandedMap,
        searchQuery: this.state.searchQuery,
        tabsOverflow: false,
        getJson: this.getJson,
        getJdbc: this.getJdbc,
        getCsv: this.getCsv,
        getText: this.getText,
        isResultFullScreen: this.state.isResultFullScreen,
        setIsResultFullScreen: this.setIsResultFullScreen
      }));
    }

    return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
      className: "sql-console-query-container"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
      className: "query-language-switch"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiFlexGroup"], {
      alignItems: "center"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiFlexItem"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiTitle"], {
      size: "l"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("h1", null, "Query Workbench"))), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiFlexItem"], {
      grow: false
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_QueryLanguageSwitch_Switch__WEBPACK_IMPORTED_MODULE_4__["default"], {
      onChange: this.onChange,
      language: this.state.language
    })), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiFlexItem"], {
      grow: false
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiButton"], {
      href: link,
      target: "_blank",
      iconType: "popout",
      iconSide: "right"
    }, linkTitle)))), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiSpacer"], {
      size: "l"
    }), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", null, page), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiSpacer"], {
      size: "l"
    }), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
      className: "sql-console-query-result"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_QueryResults_QueryResults__WEBPACK_IMPORTED_MODULE_3__["default"], {
      language: this.state.language,
      queries: this.state.queries,
      queryResults: this.state.queryResultsTable,
      queryResultsJDBC: Object(_utils_utils__WEBPACK_IMPORTED_MODULE_7__["getSelectedResults"])(this.state.queryResults, this.state.selectedTabId),
      queryResultsJSON: Object(_utils_utils__WEBPACK_IMPORTED_MODULE_7__["getSelectedResults"])(this.state.queryResultsJSON, this.state.selectedTabId),
      queryResultsCSV: Object(_utils_utils__WEBPACK_IMPORTED_MODULE_7__["getSelectedResults"])(this.state.queryResultsCSV, this.state.selectedTabId),
      queryResultsTEXT: Object(_utils_utils__WEBPACK_IMPORTED_MODULE_7__["getSelectedResults"])(this.state.queryResultsTEXT, this.state.selectedTabId),
      messages: this.state.messages,
      selectedTabId: this.state.selectedTabId,
      selectedTabName: this.state.selectedTabName,
      onSelectedTabIdChange: this.onSelectedTabIdChange,
      itemIdToExpandedRowMap: this.state.itemIdToExpandedRowMap,
      onQueryChange: this.onQueryChange,
      updateExpandedMap: this.updateExpandedMap,
      searchQuery: this.state.searchQuery,
      tabsOverflow: false,
      getJson: this.getJson,
      getJdbc: this.getJdbc,
      getCsv: this.getCsv,
      getText: this.getText,
      isResultFullScreen: this.state.isResultFullScreen,
      setIsResultFullScreen: this.setIsResultFullScreen
    }))));
  }

}
/* harmony default export */ __webpack_exports__["default"] = (Main);

/***/ }),

/***/ "./public/components/PPLPage/PPLPage.tsx":
/*!***********************************************!*\
  !*** ./public/components/PPLPage/PPLPage.tsx ***!
  \***********************************************/
/*! exports provided: PPLPage */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "PPLPage", function() { return PPLPage; });
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _elastic_eui__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @elastic/eui */ "@elastic/eui");
/* harmony import */ var _elastic_eui__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__);
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


class PPLPage extends react__WEBPACK_IMPORTED_MODULE_0___default.a.Component {
  constructor(props) {
    super(props);
    this.state = {
      pplQuery: this.props.pplQuery,
      translation: "",
      isModalVisible: false
    };
  }

  setIsModalVisible(visible) {
    this.setState({
      isModalVisible: visible
    });
  }

  render() {
    const closeModal = () => this.setIsModalVisible(false);

    const showModal = () => this.setIsModalVisible(true);

    const pplTranslationsNotEmpty = () => {
      if (this.props.pplTranslations.length > 0) {
        return this.props.pplTranslations[0].fulfilled;
      }

      return false;
    };

    const showExplainErrorMessage = () => {
      return this.props.pplTranslations.map(queryTranslation => JSON.stringify(queryTranslation.errorMessage + ": This query is not explainable.", null, 2));
    };

    const explainContent = pplTranslationsNotEmpty() ? this.props.pplTranslations.map(queryTranslation => JSON.stringify(queryTranslation.data, null, 2)).join("\n") : showExplainErrorMessage();
    let modal;

    if (this.state.isModalVisible) {
      modal = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiOverlayMask"], {
        onClick: closeModal
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiModal"], {
        onClose: closeModal,
        style: {
          width: 800
        }
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiModalHeader"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiModalHeaderTitle"], null, "Explain")), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiModalBody"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiCodeBlock"], {
        language: "json",
        fontSize: "m",
        isCopyable: true
      }, explainContent)), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiModalFooter"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiButton"], {
        onClick: closeModal,
        fill: true
      }, "Close"))));
    }

    return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiPanel"], {
      className: "sql-console-query-editor container-panel",
      paddingSize: "l"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiText"], {
      className: "sql-query-panel-header"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("h3", null, "Query editor")), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiSpacer"], {
      size: "s"
    }), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiCodeEditor"], {
      theme: "sql_console",
      width: "100%",
      height: "5rem",
      value: this.props.pplQuery,
      onChange: this.props.updatePPLQueries,
      showPrintMargin: false,
      setOptions: {
        fontSize: "14px",
        showLineNumbers: false,
        showGutter: false
      },
      "aria-label": "Code Editor"
    }), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiSpacer"], null), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiFlexGroup"], {
      className: "action-container",
      gutterSize: "m"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiFlexItem"], {
      className: "sql-editor-buttons",
      grow: false,
      onClick: () => this.props.onRun(this.props.pplQuery)
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiButton"], {
      fill: true,
      className: "sql-editor-button"
    }, "Run")), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiFlexItem"], {
      grow: false,
      onClick: () => {
        this.props.updatePPLQueries("");
        this.props.onClear();
      }
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiButton"], {
      className: "sql-editor-button"
    }, "Clear")), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiFlexItem"], {
      grow: false,
      onClick: () => this.props.onTranslate(this.props.pplQuery)
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiButton"], {
      className: "sql-editor-button",
      onClick: showModal
    }, "Explain"), modal)));
  }

}

/***/ }),

/***/ "./public/components/QueryLanguageSwitch/Switch.tsx":
/*!**********************************************************!*\
  !*** ./public/components/QueryLanguageSwitch/Switch.tsx ***!
  \**********************************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _elastic_eui__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @elastic/eui */ "@elastic/eui");
/* harmony import */ var _elastic_eui__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__);
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

 // @ts-ignore

const toggleButtons = [{
  id: 'SQL',
  label: 'SQL'
}, {
  id: 'PPL',
  label: 'PPL'
}];

class Switch extends react__WEBPACK_IMPORTED_MODULE_0___default.a.Component {
  constructor(props) {
    super(props);
    this.state = {
      language: 'SQL'
    };
  }

  render() {
    return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiButtonGroup"], {
      className: "query-language-switch",
      legend: "query-language-swtich",
      options: toggleButtons,
      onChange: id => this.props.onChange(id),
      idSelected: this.props.language,
      buttonSize: "m"
    });
  }

}

/* harmony default export */ __webpack_exports__["default"] = (Switch);

/***/ }),

/***/ "./public/components/QueryResults/QueryResults.tsx":
/*!*********************************************************!*\
  !*** ./public/components/QueryResults/QueryResults.tsx ***!
  \*********************************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _elastic_eui_lib_services__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @elastic/eui/lib/services */ "@elastic/eui/lib/services");
/* harmony import */ var _elastic_eui_lib_services__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_elastic_eui_lib_services__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var _elastic_eui__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @elastic/eui */ "@elastic/eui");
/* harmony import */ var _elastic_eui__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var _QueryResultsBody__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./QueryResultsBody */ "./public/components/QueryResults/QueryResultsBody.tsx");
/* harmony import */ var _utils_utils__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../utils/utils */ "./public/utils/utils.ts");
/* harmony import */ var _utils_constants__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ../../utils/constants */ "./public/utils/constants.ts");
/* harmony import */ var _utils_PanelWrapper__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ../../utils/PanelWrapper */ "./public/utils/PanelWrapper.tsx");
/* harmony import */ var lodash__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! lodash */ "lodash");
/* harmony import */ var lodash__WEBPACK_IMPORTED_MODULE_7___default = /*#__PURE__*/__webpack_require__.n(lodash__WEBPACK_IMPORTED_MODULE_7__);
function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

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
 // @ts-ignore

 // @ts-ignore








class QueryResults extends react__WEBPACK_IMPORTED_MODULE_0___default.a.Component {
  constructor(props) {
    super(props);

    _defineProperty(this, "sortableColumns", void 0);

    _defineProperty(this, "sortableProperties", void 0);

    _defineProperty(this, "sortedColumn", void 0);

    _defineProperty(this, "tabNames", void 0);

    _defineProperty(this, "pager", void 0);

    _defineProperty(this, "showTabsMenu", () => {
      this.setState(prevState => ({
        isPopoverOpen: !prevState.isPopoverOpen
      }));
    });

    _defineProperty(this, "slideTabsRight", () => {
      if (document.getElementById(_utils_constants__WEBPACK_IMPORTED_MODULE_5__["TAB_CONTAINER_ID"])) {
        document.getElementById(_utils_constants__WEBPACK_IMPORTED_MODULE_5__["TAB_CONTAINER_ID"]).scrollBy(50, 0);
      }
    });

    _defineProperty(this, "slideTabsLeft", () => {
      if (document.getElementById(_utils_constants__WEBPACK_IMPORTED_MODULE_5__["TAB_CONTAINER_ID"])) {
        document.getElementById(_utils_constants__WEBPACK_IMPORTED_MODULE_5__["TAB_CONTAINER_ID"]).scrollBy(-50, 0);
      }
    });

    _defineProperty(this, "closePopover", () => {
      this.setState({
        isPopoverOpen: false
      });
    });

    _defineProperty(this, "onChangeItemsPerPage", itemsPerPage => {
      this.pager.setItemsPerPage(itemsPerPage);
      this.setState({
        itemsPerPage
      });
    });

    _defineProperty(this, "onChangePage", pageIndex => {
      this.pager.goToPageIndex(pageIndex);
      this.setState({});
    });

    _defineProperty(this, "onSort", prop => {
      this.sortableProperties.sortOn(prop);
      this.sortedColumn = prop;
      this.setState({});
    });

    this.state = {
      isPopoverOpen: false,
      tabsOverflow: this.props.tabsOverflow ? this.props.tabsOverflow : false,
      itemsPerPage: _utils_constants__WEBPACK_IMPORTED_MODULE_5__["DEFAULT_NUM_RECORDS_PER_PAGE"]
    };
    this.sortableColumns = [];
    this.sortedColumn = "";
    this.sortableProperties = new _elastic_eui_lib_services__WEBPACK_IMPORTED_MODULE_1__["SortableProperties"]([{
      name: "",
      getValue: item => "",
      isAscending: true
    }], "");
    this.tabNames = [];
    this.pager = new _elastic_eui__WEBPACK_IMPORTED_MODULE_2__["Pager"](0, this.state.itemsPerPage);
  }

  componentDidUpdate() {
    const showArrow = Object(_utils_utils__WEBPACK_IMPORTED_MODULE_4__["needsScrolling"])("tabsContainer");

    if (showArrow !== this.state.tabsOverflow) {
      this.setState({
        tabsOverflow: showArrow
      });
    }
  } // Actions for Tabs Button


  updatePagination(totalItemsCount) {
    this.pager.setTotalItems(totalItemsCount);
  } // Update SORTABLE COLUMNS - All columns


  updateSortableColumns(queryResultsSelected) {
    if (this.sortableColumns.length === 0) {
      queryResultsSelected.fields.map(field => {
        this.sortableColumns.push({
          name: field,
          getValue: item => item[field],
          isAscending: true
        });
      });
      this.sortedColumn = this.sortableColumns.length > 0 ? this.sortableColumns[0].name : "";
      this.sortableProperties = new _elastic_eui_lib_services__WEBPACK_IMPORTED_MODULE_1__["SortableProperties"](this.sortableColumns, this.sortedColumn);
    }
  }

  renderTabs() {
    const tabs = [{
      id: _utils_constants__WEBPACK_IMPORTED_MODULE_5__["MESSAGE_TAB_LABEL"],
      name: lodash__WEBPACK_IMPORTED_MODULE_7___default.a.truncate(_utils_constants__WEBPACK_IMPORTED_MODULE_5__["MESSAGE_TAB_LABEL"], {
        length: 17
      }),
      disabled: false
    }];
    this.tabNames = [];

    if (this.props.queryResults) {
      for (let i = 0; i < this.props.queryResults.length; i += 1) {
        const tabName = this.props.language === "SQL" ? Object(_utils_utils__WEBPACK_IMPORTED_MODULE_4__["getQueryIndex"])(this.props.queries[i]) : "Events";
        this.tabNames.push(tabName);

        if (this.props.queryResults[i].fulfilled) {
          tabs.push({
            id: i.toString(),
            name: tabName,
            disabled: false
          });
        }
      }
    }

    return tabs;
  }

  render() {
    // Update PAGINATION and SORTABLE columns
    const queryResultSelected = Object(_utils_utils__WEBPACK_IMPORTED_MODULE_4__["getSelectedResults"])(this.props.queryResults, this.props.selectedTabId);

    if (queryResultSelected) {
      const matchingItems = this.props.searchQuery ? _elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiSearchBar"].Query.execute(this.props.searchQuery, queryResultSelected.records) : queryResultSelected.records;
      this.updatePagination(matchingItems.length);
      this.updateSortableColumns(queryResultSelected);
    } // Action button with list of tabs, TODO: disable tabArrowRight and tabArrowLeft when no more scrolling is possible


    const tabArrowDown = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiIcon"], {
      onClick: this.showTabsMenu,
      type: "arrowDown"
    });
    const tabs = this.renderTabs();
    const tabsItems = tabs.map((tab, index) => /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiContextMenuItem"], {
      key: "10 rows",
      icon: "empty",
      style: {
        marginRight: 27
      },
      onClick: () => {
        this.closePopover();
        this.pager.goToPageIndex(0);
        this.sortableColumns = [];
        this.props.onSelectedTabIdChange(tab);
      }
    }, tab.name));
    const tabsButtons = tabs.map((tab, index) => /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiTab"], {
      onClick: () => {
        this.pager.goToPageIndex(0);
        this.sortableColumns = [];
        this.props.onSelectedTabIdChange(tab);
      },
      isSelected: tab.id === this.props.selectedTabId,
      disabled: tab.disabled,
      key: index
    }, tab.name));
    return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiPanel"], {
      className: "query-result-container",
      paddingSize: "none"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
      style: {
        padding: 20,
        paddingBottom: 0
      }
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiFlexGroup"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiFlexItem"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiText"], {
      className: "query-result-panel-header"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("h3", null, "Results"))), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiFlexItem"], {
      grow: false
    }, this.props.queryResults.length > 0 && (this.props.isResultFullScreen ? /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiButtonIcon"], {
      iconType: "cross",
      color: "text",
      id: "exit-fullscreen-button",
      onClick: () => this.props.setIsResultFullScreen(false)
    }) : /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiButton"], {
      size: "s",
      iconType: "fullScreen",
      onClick: () => this.props.setIsResultFullScreen(true)
    }, "Full screen view"))))), this.props.queryResults.length === 0 ?
    /*#__PURE__*/
    // show no results message instead of the results table when there are no results
    react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(react__WEBPACK_IMPORTED_MODULE_0___default.a.Fragment, null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiSpacer"], {
      size: "xxl"
    }), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiSpacer"], {
      size: "xl"
    }), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiText"], {
      style: {
        color: "#3f3f3f"
      }
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiTextAlign"], {
      textAlign: "center"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("h4", null, "No result")), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiTextAlign"], {
      textAlign: "center"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("p", null, "Enter a query in the query editor above to see results."))), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiSpacer"], {
      size: "xxl"
    }), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiSpacer"], {
      size: "xl"
    })) : /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(react__WEBPACK_IMPORTED_MODULE_0___default.a.Fragment, null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiFlexGroup"], {
      style: {
        padding: 5
      }
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiFlexGroup"], {
      className: "tabs-container",
      alignItems: "center",
      gutterSize: "s",
      id: "tabsContainer"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiFlexItem"], {
      style: {
        marginTop: "8px"
      },
      grow: false
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiTabs"], null, tabsButtons))), this.state.tabsOverflow && /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
      className: "tab-arrow-down-container"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiFlexGroup"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiFlexItem"], {
      grow: false
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiPopover"], {
      id: "singlePanel",
      button: tabArrowDown,
      "data-test-subj": "slide-down",
      isOpen: this.state.isPopoverOpen,
      closePopover: this.closePopover,
      panelPaddingSize: "none",
      anchorPosition: "downLeft"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiContextMenuPanel"], {
      items: tabsItems
    })))))), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_2__["EuiHorizontalRule"], {
      margin: "none"
    }), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_utils_PanelWrapper__WEBPACK_IMPORTED_MODULE_6__["PanelWrapper"], {
      shouldWrap: this.props.language === 'SQL' && this.props.selectedTabName !== _utils_constants__WEBPACK_IMPORTED_MODULE_5__["MESSAGE_TAB_LABEL"]
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_QueryResultsBody__WEBPACK_IMPORTED_MODULE_3__["default"], {
      language: this.props.language,
      queries: this.props.queries,
      selectedTabId: this.props.selectedTabId,
      selectedTabName: this.props.selectedTabName,
      tabNames: this.tabNames,
      queryResultSelected: queryResultSelected,
      queryResultsJSON: this.props.queryResultsJSON,
      queryResultsJDBC: this.props.queryResultsJDBC,
      queryResultsCSV: this.props.queryResultsCSV,
      queryResultsTEXT: this.props.queryResultsTEXT,
      messages: this.props.messages,
      searchQuery: this.props.searchQuery,
      onQueryChange: this.props.onQueryChange,
      pager: this.pager,
      itemsPerPage: this.state.itemsPerPage,
      firstItemIndex: this.pager.getFirstItemIndex(),
      lastItemIndex: this.pager.getLastItemIndex(),
      onChangeItemsPerPage: this.onChangeItemsPerPage,
      onChangePage: this.onChangePage,
      onSort: this.onSort,
      sortedColumn: this.sortedColumn,
      sortableProperties: this.sortableProperties,
      itemIdToExpandedRowMap: this.props.itemIdToExpandedRowMap,
      updateExpandedMap: this.props.updateExpandedMap,
      getJson: this.props.getJson,
      getJdbc: this.props.getJdbc,
      getCsv: this.props.getCsv,
      getText: this.props.getText
    }))));
  }

}

/* harmony default export */ __webpack_exports__["default"] = (QueryResults);

/***/ }),

/***/ "./public/components/QueryResults/QueryResultsBody.tsx":
/*!*************************************************************!*\
  !*** ./public/components/QueryResults/QueryResultsBody.tsx ***!
  \*************************************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _elastic_eui__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @elastic/eui */ "@elastic/eui");
/* harmony import */ var _elastic_eui__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var _utils_utils__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../utils/utils */ "./public/utils/utils.ts");
/* harmony import */ var _ace_themes_sql_console__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../ace-themes/sql_console */ "./public/ace-themes/sql_console.js");
/* harmony import */ var _utils_constants__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../utils/constants */ "./public/utils/constants.ts");
function _extends() { _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; }; return _extends.apply(this, arguments); }

function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

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
 // @ts-ignore

// @ts-ignore






const DoubleScrollbar = __webpack_require__(/*! react-double-scrollbar */ "./node_modules/react-double-scrollbar/dist/DoubleScrollbar.js");

class QueryResultsBody extends react__WEBPACK_IMPORTED_MODULE_0___default.a.Component {
  constructor(props) {
    super(props);

    _defineProperty(this, "items", void 0);

    _defineProperty(this, "columns", void 0);

    _defineProperty(this, "panels", void 0);

    _defineProperty(this, "expandedRowColSpan", void 0);

    _defineProperty(this, "getModal", errorMessage => {
      const closeModal = () => this.setIsModalVisible(false);

      let modal = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiOverlayMask"], {
        onClick: closeModal
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiModal"], {
        onClose: closeModal
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiModalHeader"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiModalHeaderTitle"], null, "Error")), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiModalBody"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiText"], null, errorMessage)), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiModalFooter"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiButton"], {
        onClick: closeModal,
        fill: true
      }, "Close"))));
      return modal;
    });

    _defineProperty(this, "onDownloadJDBC", () => {
      if (!this.props.queryResultsJDBC) {
        this.props.getJdbc(this.props.queries);
      }

      setTimeout(() => {
        const jsonObject = JSON.parse(this.props.queryResultsJDBC);
        const data = JSON.stringify(jsonObject, undefined, 4);
        Object(_utils_utils__WEBPACK_IMPORTED_MODULE_2__["onDownloadFile"])(data, "json", this.props.selectedTabName + ".json");
      }, 2000);
    });

    _defineProperty(this, "onDownloadCSV", () => {
      if (this.props.language == 'PPL') {
        this.setState({
          downloadErrorModal: this.getModal("PPL result in CSV format is not supported, please select JDBC format.")
        });
        this.setIsModalVisible(true);
        return;
      }

      if (!this.props.queryResultsCSV) {
        this.props.getCsv(this.props.queries);
      }

      setTimeout(() => {
        const data = this.props.queryResultsCSV;
        Object(_utils_utils__WEBPACK_IMPORTED_MODULE_2__["onDownloadFile"])(data, "csv", this.props.selectedTabName + ".csv");
      }, 2000);
    });

    _defineProperty(this, "onDownloadText", () => {
      if (this.props.language == 'PPL') {
        this.setState({
          downloadErrorModal: this.getModal("PPL result in Text format is not supported, please select JDBC format.")
        });
        this.setIsModalVisible(true);
        return;
      }

      if (!this.props.queryResultsTEXT) {
        this.props.getText(this.props.queries);
      }

      setTimeout(() => {
        const data = this.props.queryResultsTEXT;
        Object(_utils_utils__WEBPACK_IMPORTED_MODULE_2__["onDownloadFile"])(data, "plain", this.props.selectedTabName + ".txt");
      }, 2000);
    });

    _defineProperty(this, "onDownloadButtonClick", () => {
      this.setState(prevState => ({
        isDownloadPopoverOpen: !prevState.isDownloadPopoverOpen
      }));
    });

    _defineProperty(this, "closeDownloadPopover", () => {
      this.setState({
        isDownloadPopoverOpen: false
      });
    });

    _defineProperty(this, "toggleNodeData", (node, expandedRowMap) => {
      let newItemIdToExpandedRowMap = expandedRowMap;
      const rootNode = Object(_utils_utils__WEBPACK_IMPORTED_MODULE_2__["findRootNode"])(node, expandedRowMap);

      if (expandedRowMap[node.nodeId] && expandedRowMap[node.nodeId].expandedRow) {
        delete newItemIdToExpandedRowMap[node.nodeId].expandedRow;
      } else if (node.children && node.children.length > 0) {
        newItemIdToExpandedRowMap = this.updateExpandedRow(node, expandedRowMap);
      }

      if (rootNode !== node) {
        newItemIdToExpandedRowMap = this.updateExpandedRow(rootNode, expandedRowMap);
      }

      this.props.updateExpandedMap(newItemIdToExpandedRowMap);
    });

    _defineProperty(this, "createItem", (expandedRowMap, node, name, items = {}) => {
      const nodeId = node.nodeId;
      let isSelected = false;

      if (!Object(_utils_utils__WEBPACK_IMPORTED_MODULE_2__["isEmpty"])(node.parent)) {
        isSelected = expandedRowMap[node.parent.nodeId] && expandedRowMap[node.parent.nodeId].selectedNodes && expandedRowMap[node.parent.nodeId].selectedNodes.hasOwnProperty(nodeId);
      }

      return { ...items,
        id: nodeId,
        name,
        isSelected: isSelected,
        onClick: () => console.log('open side nav')
      };
    });

    _defineProperty(this, "renderNodeData", (node, expandedRowMap) => {
      // let dataRow: DataRow = {};
      let items = [];
      let columns = [];
      let records = [];
      const data = node.data;

      if (Array.isArray(data)) {
        items = data;
        columns = typeof items[0] === "object" ? Object.keys(items[0]) : [];
      } else if (typeof data === "object") {
        records.push(data);
        items = records;
        columns = this.addExpandingIconColumn(Object.keys(data));
      }

      let dataRow = {
        rowId: 0,
        data: items
      };
      return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiTable"], {
        className: "sideNav-table"
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiTableHeader"], {
        className: "table-header"
      }, this.renderHeaderCellsWithNoSorting(columns)), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiTableBody"], null, this.renderRow(dataRow, columns, node.nodeId.toString(), expandedRowMap))));
    });

    this.state = {
      itemIdToSelectedMap: {},
      itemIdToOpenActionsPopoverMap: {},
      incremental: true,
      filters: false,
      itemIdToExpandedRowMap: this.props.itemIdToExpandedRowMap,
      searchQuery: this.props.searchQuery,
      selectedItemMap: {},
      selectedItemName: "",
      selectedItemData: {},
      navView: false,
      isPopoverOpen: false,
      isDownloadPopoverOpen: false,
      isModalVisible: false,
      downloadErrorModal: {}
    };
    this.expandedRowColSpan = 0;
    this.items = [];
    this.columns = [];
    this.panels = [{
      id: 0,
      items: [{
        name: "Download JSON",
        onClick: () => {
          this.onDownloadJSON();
        }
      }, {
        name: "Download JDBC",
        onClick: () => {
          this.onDownloadJDBC();
        }
      }, {
        name: "Download CSV",
        onClick: () => {
          this.onDownloadCSV();
        }
      }, {
        name: "Download Text",
        onClick: () => {
          this.onDownloadText();
        }
      }]
    }];
  }

  setIsModalVisible(visible) {
    this.setState({
      isModalVisible: visible
    });
  }

  // Actions for Download files
  onDownloadJSON() {
    if (this.props.language == 'PPL') {
      this.setState({
        downloadErrorModal: this.getModal("PPL result in JSON format is not supported, please select JDBC format.")
      });
      this.setIsModalVisible(true);
      return;
    }

    if (!this.props.queryResultsJSON) {
      this.props.getJson(this.props.queries);
    }

    setTimeout(() => {
      const jsonObject = JSON.parse(this.props.queryResultsJSON);
      const data = JSON.stringify(jsonObject, undefined, 4);
      Object(_utils_utils__WEBPACK_IMPORTED_MODULE_2__["onDownloadFile"])(data, "json", this.props.selectedTabName + ".json");
    }, 2000);
  }

  // It sorts and filters table values
  getItems(records) {
    const matchingItems = this.props.searchQuery ? _elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiSearchBar"].Query.execute(this.props.searchQuery, records) : records;
    return this.props.sortableProperties.sortItems(matchingItems);
  } // It processes field values and determines whether it should link to an expanded row or an expanded array


  getFieldValue(fieldValue, field) {
    let hasExpandingRow = false;
    let hasExpandingArray = false;
    let value = "";
    let link = "";

    if (fieldValue === null) {
      return {
        hasExpandingRow: hasExpandingRow,
        value: "",
        hasExpandingArray,
        link
      };
    } // Not an object or array


    if (typeof fieldValue !== "object") {
      return {
        hasExpandingRow: hasExpandingRow,
        value: fieldValue,
        hasExpandingArray,
        link
      };
    } // Array of strings or objects


    if (Array.isArray(fieldValue)) {
      if (typeof fieldValue[0] !== "object") {
        hasExpandingArray = true;
        link = field.concat(": [", fieldValue.length.toString(), "]");
      } else {
        hasExpandingRow = true;
        link = field.concat(": {", fieldValue.length.toString(), "}");
      }
    } // Single object
    else {
        hasExpandingRow = true;
        link = field.concat(": {1}");
      }

    return {
      hasExpandingRow: hasExpandingRow,
      hasExpandingArray: hasExpandingArray,
      value: value,
      link: link
    };
  }

  addExpandingNodeIcon(node, expandedRowMap) {
    return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiButtonIcon"], {
      style: {
        marginLeft: -4
      },
      onClick: () => this.toggleNodeData(node, expandedRowMap),
      "aria-label": expandedRowMap[node.nodeId] && expandedRowMap[node.nodeId].expandedRow ? "Collapse" : "Expand",
      iconType: expandedRowMap[node.nodeId] && expandedRowMap[node.nodeId].expandedRow ? "minusInCircle" : "plusInCircle"
    });
  }

  addExpandingSideNavIcon(node, expandedRowMap) {
    if (!node.parent) {
      return;
    }

    return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiButtonIcon"], {
      onClick: () => this.updateExpandedRowMap(node, expandedRowMap),
      "aria-label": expandedRowMap[node.parent.nodeId] && expandedRowMap[node.parent.nodeId].selectedNodes && expandedRowMap[node.parent.nodeId].selectedNodes.hasOwnProperty(node.nodeId) ? "Collapse" : "Expand",
      iconType: expandedRowMap[node.parent.nodeId] && expandedRowMap[node.parent.nodeId].selectedNodes && expandedRowMap[node.parent.nodeId].selectedNodes.hasOwnProperty(node.nodeId) ? "minusInCircle" : "plusInCircle"
    });
  }

  addExpandingIconColumn(columns) {
    const expandIconColumn = [{
      id: "expandIcon",
      label: "",
      isSortable: false,
      width: "30px"
    }];
    columns = expandIconColumn.concat(columns);
    return columns;
  }

  updateSelectedNodes(parentNode, selectedNode, expandedRowMap, keepOpen = false) {
    if (!parentNode) {
      return expandedRowMap;
    }

    const parentNodeId = parentNode.nodeId;

    if (expandedRowMap[parentNodeId] && expandedRowMap[parentNodeId].selectedNodes && expandedRowMap[parentNodeId].selectedNodes.hasOwnProperty(selectedNode.nodeId) && !keepOpen) {
      delete expandedRowMap[parentNodeId].selectedNodes[selectedNode.nodeId];
    } else {
      if (!expandedRowMap[parentNodeId].selectedNodes) {
        expandedRowMap[parentNodeId].selectedNodes = {};
      }

      expandedRowMap[parentNodeId].selectedNodes[selectedNode.nodeId] = selectedNode.data;
    }

    return expandedRowMap;
  }

  updateExpandedRow(node, expandedRowMap) {
    let newItemIdToExpandedRowMap = expandedRowMap;

    if (expandedRowMap[node.nodeId]) {
      newItemIdToExpandedRowMap[node.nodeId].expandedRow = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
        id: node.nodeId,
        style: {
          padding: "0 0 20px 19px"
        }
      }, this.renderNav(node, node.name, expandedRowMap));
    }

    return newItemIdToExpandedRowMap;
  }

  updateExpandedRowMap(node, expandedRowMap, keepOpen = false) {
    if (!node) {
      return expandedRowMap;
    }

    let newItemIdToExpandedRowMap = this.updateSelectedNodes(node.parent, node, expandedRowMap, keepOpen);
    const rootNode = Object(_utils_utils__WEBPACK_IMPORTED_MODULE_2__["findRootNode"])(node, expandedRowMap);

    if (expandedRowMap[rootNode.nodeId]) {
      newItemIdToExpandedRowMap = this.updateExpandedRow(node.parent, newItemIdToExpandedRowMap);
      newItemIdToExpandedRowMap = this.updateExpandedRow(rootNode, newItemIdToExpandedRowMap);
    }

    this.props.updateExpandedMap(newItemIdToExpandedRowMap);
  }

  getChildrenItems(nodes, parentNode, expandedRowMap) {
    const itemList = [];

    if (nodes.length === 0 && parentNode.data) {
      const renderedData = this.renderNodeData(parentNode, expandedRowMap);
      itemList.push(this.createItem(expandedRowMap, parentNode, renderedData, {
        items: []
      }));
    }

    for (let i = 0; i < nodes.length; i++) {
      itemList.push(this.createItem(expandedRowMap, nodes[i], nodes[i].name, {
        icon: this.addExpandingSideNavIcon(nodes[i], expandedRowMap),
        items: this.getChildrenItems(nodes[i].children, nodes[i], expandedRowMap)
      }));
    }

    return itemList;
  }

  /************* Render Functions *************/
  renderMessagesTab() {
    return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiCodeEditor"], {
      className: this.props.messages && this.props.messages.length > 0 ? this.props.messages[0].className : "successful-message",
      mode: "text",
      theme: "sql_console",
      width: "100%",
      value: Object(_utils_utils__WEBPACK_IMPORTED_MODULE_2__["getMessageString"])(this.props.messages, this.props.tabNames),
      showPrintMargin: false,
      readOnly: true,
      setOptions: {
        fontSize: "14px",
        readOnly: true,
        highlightActiveLine: false,
        highlightGutterLine: false
      },
      "aria-label": "Code Editor"
    });
  }

  renderHeaderCells(columns) {
    return columns.map(field => {
      const label = field.id === "expandIcon" ? field.label : field;
      const colwidth = field.id === "expandIcon" ? _utils_constants__WEBPACK_IMPORTED_MODULE_4__["SMALL_COLUMN_WIDTH"] : _utils_constants__WEBPACK_IMPORTED_MODULE_4__["COLUMN_WIDTH"];
      return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiTableHeaderCell"], {
        key: label,
        width: colwidth,
        onSort: this.props.onSort.bind(this, field),
        isSorted: this.props.sortedColumn === field,
        isSortAscending: this.props.sortableProperties.isAscendingByName(field)
      }, label);
    });
  } // Inner tables sorting is not enabled


  renderHeaderCellsWithNoSorting(columns) {
    return columns.map(field => {
      const label = field.id === "expandIcon" ? field.label : field;
      const colwidth = field.id === "expandIcon" ? field.width : _utils_constants__WEBPACK_IMPORTED_MODULE_4__["COLUMN_WIDTH"];
      return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiTableHeaderCell"], {
        key: label,
        width: colwidth
      }, label);
    });
  }

  renderRow(item, columns, rowId, expandedRowMap) {
    let rows = [];
    const data = item.data; // If the data is an array or an object we add it to the expandedRowMap

    if (data && (typeof data === "object" && !Object(_utils_utils__WEBPACK_IMPORTED_MODULE_2__["isEmpty"])(data) || Array.isArray(data) && data.length > 0)) {
      let rowItems = [];

      if (Array.isArray(data)) {
        rowItems = data;
      } else {
        rowItems.push(data);
      }

      for (let i = 0; i < rowItems.length; i++) {
        let rowItem = rowItems[i];
        let tableCells = [];
        const tree = Object(_utils_utils__WEBPACK_IMPORTED_MODULE_2__["getRowTree"])(rowId, rowItem, expandedRowMap); // Add nodes to expandedRowMap

        if (!expandedRowMap[rowId] || !expandedRowMap[rowId].nodes) {
          expandedRowMap[rowId] = {
            nodes: tree
          };
        }

        const expandingNode = tree && tree._root.children.length > 0 ? this.addExpandingNodeIcon(tree._root, expandedRowMap) : "";

        if (columns.length > 0) {
          columns.map(field => {
            // Table cell
            if (field.id !== "expandIcon") {
              const fieldObj = this.getFieldValue(rowItem[field], field);
              let fieldValue; // If field is expandable

              if (fieldObj.hasExpandingRow || fieldObj.hasExpandingArray) {
                const fieldNode = expandedRowMap[tree._root.nodeId].nodes._root.children.find(node => node.name === field);

                fieldValue = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("span", null, " ", fieldObj.value, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiLink"], {
                  color: "primary",
                  onClick: () => {
                    this.updateExpandedRowMap(fieldNode, expandedRowMap, true);
                    Object(_utils_utils__WEBPACK_IMPORTED_MODULE_2__["scrollToNode"])(tree._root.nodeId);
                  }
                }, fieldObj.link));
              } else {
                fieldValue = fieldObj.value;
              }

              tableCells.push( /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiTableRowCell"], {
                key: field,
                truncateText: false,
                textOnly: true
              }, fieldValue));
            } // Expanding icon cell
            else {
                tableCells.push( /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiTableRowCell"], {
                  id: tree._root.nodeId
                }, expandingNode));
              }
          });
        } else {
          const fieldObj = this.getFieldValue(rowItem, "");
          tableCells.push( /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiTableRowCell"], {
            truncateText: false,
            textOnly: true
          }, fieldObj.value));
        }

        const tableRow = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiTableRow"], {
          key: rowId,
          "data-test-subj": 'tableRow'
        }, tableCells, " ");
        let row = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(react__WEBPACK_IMPORTED_MODULE_0__["Fragment"], null, tableRow);

        if (expandedRowMap[rowId] && expandedRowMap[rowId].expandedRow) {
          const tableRow = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiTableRow"], {
            className: "expanded-row",
            key: rowId
          }, tableCells, " ");
          const expandedRow = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiTableRow"], null, expandedRowMap[rowId].expandedRow);
          row = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(react__WEBPACK_IMPORTED_MODULE_0__["Fragment"], null, tableRow, expandedRow);
        }

        rows.push(row);
      }
    }

    return rows;
  }

  renderRows(items, columns, expandedRowMap) {
    let rows = [];

    if (items) {
      for (let itemIndex = this.props.firstItemIndex; itemIndex <= this.props.lastItemIndex; itemIndex++) {
        const item = items[itemIndex];
        const rowId = item.rowId;

        if (item) {
          const rowsForItem = this.renderRow(item, columns, rowId.toString(10), expandedRowMap);
          rows.push(rowsForItem);
        }
      }
    }

    return rows;
  }

  renderSearchBar() {
    const search = {
      box: {
        incremental: this.state.incremental,
        placeholder: "Search keyword",
        schema: true
      }
    };
    return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
      className: "search-bar"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiSearchBar"], _extends({
      onChange: this.props.onQueryChange,
      query: this.props.searchQuery
    }, search)));
  }

  renderNav(node, table_name, expandedRowMap) {
    const sideNav = [{
      items: this.getChildrenItems(node.children, node, expandedRowMap),
      id: node.nodeId,
      name: node.name,
      isSelected: false,
      onClick: () => console.log('open side nav')
    }];
    return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiSideNav"], {
      mobileTitle: "Navigate within $APP_NAME",
      items: sideNav,
      className: "sideNavItem__items",
      style: {
        width: "300px",
        padding: "0 0 20px 9px"
      }
    });
  }

  render() {
    // Action button with list of downloads
    const downloadsButton = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiButton"], {
      iconType: "arrowDown",
      iconSide: "right",
      size: "s",
      onClick: this.onDownloadButtonClick
    }, "Download");
    let modal;

    if (this.state.isModalVisible) {
      modal = this.state.downloadErrorModal;
    }

    if ( // this.props.selectedTabId === MESSAGE_TAB_LABEL ||
    this.props.queryResultSelected == undefined) {
      return this.renderMessagesTab();
    } else {
      if (this.props.queryResultSelected) {
        this.items = this.getItems(this.props.queryResultSelected.records); //Adding an extra empty column for the expanding icon

        this.columns = this.addExpandingIconColumn(this.props.queryResultSelected.fields);
        this.expandedRowColSpan = this.columns.length;
      }

      return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", null, this.props.language === 'SQL' && /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(react__WEBPACK_IMPORTED_MODULE_0___default.a.Fragment, null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiFlexGroup"], {
        alignItems: "flexStart",
        style: {
          padding: 20,
          paddingBottom: 0
        }
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiFlexItem"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiText"], {
        className: "table-name"
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("h4", null, this.props.selectedTabName, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("span", {
        className: "table-item-count"
      }, ` (${this.items.length})`))), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
        className: "search-panel"
      }, this.renderSearchBar())), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiFlexItem"], {
        grow: false
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
        className: "download-container"
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiPopover"], {
        className: "download-button-container",
        id: "singlePanel",
        button: downloadsButton,
        isOpen: this.state.isDownloadPopoverOpen,
        closePopover: this.closeDownloadPopover,
        panelPaddingSize: "none",
        anchorPosition: "downLeft"
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiContextMenu"], {
        initialPanelId: 0,
        panels: this.panels
      }))))), modal), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
        className: "sql-console-results-container"
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(DoubleScrollbar, null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiFlexGroup"], {
        gutterSize: "none"
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiFlexItem"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiTable"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiTableHeader"], {
        className: "table-header"
      }, this.renderHeaderCells(this.columns)), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiTableBody"], null, this.renderRows(this.items, this.columns, this.props.itemIdToExpandedRowMap))))))), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
        className: "pagination-container"
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiTablePagination"], {
        activePage: this.props.pager.getCurrentPageIndex(),
        itemsPerPage: this.props.itemsPerPage,
        itemsPerPageOptions: _utils_constants__WEBPACK_IMPORTED_MODULE_4__["PAGE_OPTIONS"],
        pageCount: this.props.pager.getTotalPages(),
        onChangeItemsPerPage: this.props.onChangeItemsPerPage,
        onChangePage: this.props.onChangePage
      })));
    }
  }

}

/* harmony default export */ __webpack_exports__["default"] = (QueryResultsBody);

/***/ }),

/***/ "./public/components/SQLPage/SQLPage.tsx":
/*!***********************************************!*\
  !*** ./public/components/SQLPage/SQLPage.tsx ***!
  \***********************************************/
/*! exports provided: SQLPage */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "SQLPage", function() { return SQLPage; });
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _elastic_eui__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @elastic/eui */ "@elastic/eui");
/* harmony import */ var _elastic_eui__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var brace_mode_sql__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! brace/mode/sql */ "./node_modules/brace/mode/sql.js");
/* harmony import */ var brace_mode_sql__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(brace_mode_sql__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var _ace_themes_sql_console__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../ace-themes/sql_console */ "./public/ace-themes/sql_console.js");
/* harmony import */ var brace_ext_language_tools__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! brace/ext/language_tools */ "./node_modules/brace/ext/language_tools.js");
/* harmony import */ var brace_ext_language_tools__WEBPACK_IMPORTED_MODULE_4___default = /*#__PURE__*/__webpack_require__.n(brace_ext_language_tools__WEBPACK_IMPORTED_MODULE_4__);
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





class SQLPage extends react__WEBPACK_IMPORTED_MODULE_0___default.a.Component {
  constructor(props) {
    super(props);
    this.state = {
      sqlQuery: this.props.sqlQuery,
      translation: "",
      isModalVisible: false
    };
  }

  setIsModalVisible(visible) {
    this.setState({
      isModalVisible: visible
    });
  }

  render() {
    const closeModal = () => this.setIsModalVisible(false);

    const showModal = () => this.setIsModalVisible(true);

    const sqlTranslationsNotEmpty = () => {
      if (this.props.sqlTranslations.length > 0) {
        return this.props.sqlTranslations[0].fulfilled;
      }

      return false;
    };

    const showExplainErrorMessage = () => {
      return this.props.sqlTranslations.map(queryTranslation => JSON.stringify(queryTranslation.errorMessage + ": This query is not explainable.", null, 2));
    };

    const explainContent = sqlTranslationsNotEmpty() ? this.props.sqlTranslations.map(queryTranslation => JSON.stringify(queryTranslation.data, null, 2)).join("\n") : showExplainErrorMessage();
    let modal;

    if (this.state.isModalVisible) {
      modal = /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiOverlayMask"], {
        onClick: closeModal
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiModal"], {
        onClose: closeModal,
        style: {
          width: 800
        }
      }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiModalHeader"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiModalHeaderTitle"], null, "Explain")), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiModalBody"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiCodeBlock"], {
        language: "json",
        fontSize: "m",
        isCopyable: true
      }, explainContent)), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiModalFooter"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiButton"], {
        onClick: closeModal,
        fill: true
      }, "Close"))));
    }

    return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiPanel"], {
      className: "sql-console-query-editor container-panel",
      paddingSize: "l"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiText"], {
      className: "sql-query-panel-header"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("h3", null, "Query editor")), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiSpacer"], {
      size: "s"
    }), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiCodeEditor"], {
      mode: "sql",
      theme: "sql_console",
      width: "100%",
      height: "7rem",
      value: this.props.sqlQuery,
      onChange: this.props.updateSQLQueries,
      showPrintMargin: false,
      setOptions: {
        fontSize: "14px",
        enableBasicAutocompletion: true,
        enableLiveAutocompletion: true
      },
      "aria-label": "Code Editor"
    }), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiSpacer"], null), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiFlexGroup"], {
      className: "action-container",
      gutterSize: "m"
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiFlexItem"], {
      grow: false,
      onClick: () => this.props.onRun(this.props.sqlQuery)
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiButton"], {
      fill: true,
      className: "sql-editor-button"
    }, "Run")), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiFlexItem"], {
      grow: false,
      onClick: () => {
        this.props.updateSQLQueries("");
        this.props.onClear();
      }
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiButton"], {
      className: "sql-editor-button"
    }, "Clear")), /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiFlexItem"], {
      grow: false,
      onClick: () => this.props.onTranslate(this.props.sqlQuery)
    }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiButton"], {
      className: "sql-editor-button",
      onClick: showModal
    }, "Explain"), modal)));
  }

}

/***/ }),

/***/ "./public/components/app.tsx":
/*!***********************************!*\
  !*** ./public/components/app.tsx ***!
  \***********************************/
/*! exports provided: WorkbenchApp */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "WorkbenchApp", function() { return WorkbenchApp; });
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _kbn_i18n_react__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @kbn/i18n/react */ "@kbn/i18n/react");
/* harmony import */ var _kbn_i18n_react__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_kbn_i18n_react__WEBPACK_IMPORTED_MODULE_1__);
/* harmony import */ var react_router_dom__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! react-router-dom */ "react-router-dom");
/* harmony import */ var react_router_dom__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(react_router_dom__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var _elastic_eui__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @elastic/eui */ "@elastic/eui");
/* harmony import */ var _elastic_eui__WEBPACK_IMPORTED_MODULE_3___default = /*#__PURE__*/__webpack_require__.n(_elastic_eui__WEBPACK_IMPORTED_MODULE_3__);
/* harmony import */ var _Main__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ./Main */ "./public/components/Main/index.ts");
function _extends() { _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; }; return _extends.apply(this, arguments); }

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






const onChange = () => {};

const WorkbenchApp = ({
  basename,
  notifications,
  http,
  navigation
}) => {
  return /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(react_router_dom__WEBPACK_IMPORTED_MODULE_2__["BrowserRouter"], {
    basename: '/' + basename
  }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_kbn_i18n_react__WEBPACK_IMPORTED_MODULE_1__["I18nProvider"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_3__["EuiPage"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_3__["EuiPageBody"], null, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(react_router_dom__WEBPACK_IMPORTED_MODULE_2__["Route"], {
    path: "/",
    render: props => /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_Main__WEBPACK_IMPORTED_MODULE_4__["Main"], _extends({
      httpClient: http
    }, props))
  }))))));
};

/***/ }),

/***/ "./public/utils/PanelWrapper.tsx":
/*!***************************************!*\
  !*** ./public/utils/PanelWrapper.tsx ***!
  \***************************************/
/*! exports provided: PanelWrapper */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "PanelWrapper", function() { return PanelWrapper; });
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! react */ "react");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _elastic_eui__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @elastic/eui */ "@elastic/eui");
/* harmony import */ var _elastic_eui__WEBPACK_IMPORTED_MODULE_1___default = /*#__PURE__*/__webpack_require__.n(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__);
/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */


function PanelWrapper({
  shouldWrap,
  children
}) {
  return shouldWrap ? /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement("div", {
    style: {
      backgroundColor: "#f5f7fa",
      padding: 25
    }
  }, /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(_elastic_eui__WEBPACK_IMPORTED_MODULE_1__["EuiPanel"], {
    paddingSize: "none"
  }, children)) : /*#__PURE__*/react__WEBPACK_IMPORTED_MODULE_0___default.a.createElement(react__WEBPACK_IMPORTED_MODULE_0___default.a.Fragment, null, children);
}

/***/ }),

/***/ "./public/utils/constants.ts":
/*!***********************************!*\
  !*** ./public/utils/constants.ts ***!
  \***********************************/
/*! exports provided: DEFAULT_NUM_RECORDS_PER_PAGE, PAGE_OPTIONS, COLUMN_WIDTH, MEDIUM_COLUMN_WIDTH, SMALL_COLUMN_WIDTH, TAB_CONTAINER_ID, MESSAGE_TAB_LABEL */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "DEFAULT_NUM_RECORDS_PER_PAGE", function() { return DEFAULT_NUM_RECORDS_PER_PAGE; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "PAGE_OPTIONS", function() { return PAGE_OPTIONS; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "COLUMN_WIDTH", function() { return COLUMN_WIDTH; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "MEDIUM_COLUMN_WIDTH", function() { return MEDIUM_COLUMN_WIDTH; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "SMALL_COLUMN_WIDTH", function() { return SMALL_COLUMN_WIDTH; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "TAB_CONTAINER_ID", function() { return TAB_CONTAINER_ID; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "MESSAGE_TAB_LABEL", function() { return MESSAGE_TAB_LABEL; });
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
// Table constants
const DEFAULT_NUM_RECORDS_PER_PAGE = 10;
const PAGE_OPTIONS = [10, 20, 50, 100];
const COLUMN_WIDTH = '155px';
const MEDIUM_COLUMN_WIDTH = '80px';
const SMALL_COLUMN_WIDTH = '27px'; // Tabs constants

const TAB_CONTAINER_ID = 'tabsContainer';
const MESSAGE_TAB_LABEL = 'Output';

/***/ }),

/***/ "./public/utils/utils.ts":
/*!*******************************!*\
  !*** ./public/utils/utils.ts ***!
  \*******************************/
/*! exports provided: getQueries, getQueryIndex, getDefaultTabId, getDefaultTabLabel, getSelectedResults, isEmpty, capitalizeFirstLetter, getMessageString, scrollToNode, onDownloadFile, Tree, Node, createRowTree, getRowTree, findRootNode, needsScrolling */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "getQueries", function() { return getQueries; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "getQueryIndex", function() { return getQueryIndex; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "getDefaultTabId", function() { return getDefaultTabId; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "getDefaultTabLabel", function() { return getDefaultTabLabel; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "getSelectedResults", function() { return getSelectedResults; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "isEmpty", function() { return isEmpty; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "capitalizeFirstLetter", function() { return capitalizeFirstLetter; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "getMessageString", function() { return getMessageString; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "scrollToNode", function() { return scrollToNode; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "onDownloadFile", function() { return onDownloadFile; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "Tree", function() { return Tree; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "Node", function() { return Node; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "createRowTree", function() { return createRowTree; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "getRowTree", function() { return getRowTree; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "findRootNode", function() { return findRootNode; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "needsScrolling", function() { return needsScrolling; });
/* harmony import */ var _constants__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./constants */ "./public/utils/constants.ts");
function _defineProperty(obj, key, value) { if (key in obj) { Object.defineProperty(obj, key, { value: value, enumerable: true, configurable: true, writable: true }); } else { obj[key] = value; } return obj; }

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
 // It returns an array of queries

const getQueries = queriesString => {
  if (queriesString == '') {
    return [];
  }

  return queriesString.split(';').map(query => query.trim()).filter(query => query != '');
}; // It retrieves the index from the query. The index is used to label the query results tab

function getQueryIndex(query) {
  if (query) {
    const queryFrom = query.toLowerCase().split("from");

    if (queryFrom.length > 1) {
      return queryFrom[1].split(" ")[1];
    }
  }

  return query;
} // Tabs utils

function getDefaultTabId(queryResults) {
  return queryResults && queryResults.length > 0 && queryResults[0].fulfilled ? "0" : _constants__WEBPACK_IMPORTED_MODULE_0__["MESSAGE_TAB_LABEL"];
}
function getDefaultTabLabel(queryResults, queryString) {
  return queryResults && queryResults.length > 0 && queryResults[0].fulfilled ? getQueryIndex(queryString) : _constants__WEBPACK_IMPORTED_MODULE_0__["MESSAGE_TAB_LABEL"];
} // It returns the results for the selected tab

function getSelectedResults(results, selectedTabId) {
  const selectedIndex = parseInt(selectedTabId);

  if (!Number.isNaN(selectedIndex) && results) {
    const selectedResult = results[selectedIndex];
    return selectedResult && selectedResult.fulfilled ? selectedResult.data : undefined;
  }

  return undefined;
}
function isEmpty(obj) {
  for (const key in obj) {
    if (obj.hasOwnProperty(key)) {
      return false;
    }
  }

  return true;
}
function capitalizeFirstLetter(name) {
  return name && name.length > 0 ? name.charAt(0).toUpperCase() + name.slice(1) : name;
}
function getMessageString(messages, tabNames) {
  return messages && messages.length > 0 && tabNames && tabNames.length > 0 ? messages.reduce((finalMessage, message, currentIndex) => finalMessage.concat(capitalizeFirstLetter(tabNames[currentIndex]), ': ', messages[currentIndex].text, '\n\n'), '') : '';
}
function scrollToNode(nodeId) {
  const element = document.getElementById(nodeId);

  if (element) {
    element.scrollIntoView();
  }
} // Download functions

function onDownloadFile(data, fileFormat, fileName) {
  const encodedUri = encodeURI(data);
  const content = 'data:text/' + fileFormat + ';charset=utf-8,' + encodedUri;
  const link = document.createElement("a");
  link.setAttribute('href', content);
  link.setAttribute('download', fileName);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}
class Tree {
  constructor(data, rootId) {
    _defineProperty(this, "_root", void 0);

    this._root = new Node(data, rootId, '', this._root);
  }

}
class Node {
  constructor(data, parentId, name = '', parent) {
    _defineProperty(this, "data", void 0);

    _defineProperty(this, "name", void 0);

    _defineProperty(this, "children", void 0);

    _defineProperty(this, "parent", void 0);

    _defineProperty(this, "nodeId", void 0);

    this.data = data;
    this.name = name;
    this.children = [];
    this.parent = parent;
    this.nodeId = name === '' ? parentId : parentId + '_' + name;
  }

} // It creates a tree of nested objects or arrays for the row, where rootId is the rowId and item is the field value

function createRowTree(item, rootId) {
  const tree = new Tree(item, rootId);
  const root = tree._root;

  if (typeof item === 'object') {
    for (let j = 0; j < Object.keys(item).length; j++) {
      const itemKey = Object.keys(item)[j];
      let data = item[itemKey]; // If value of field is an array or an object it gets added to the tree

      if (data !== null && (Array.isArray(data) || typeof data === 'object')) {
        const firstNode = new Node(data, rootId, itemKey, root);
        root.children.push(firstNode);
      }
    }
  }

  return tree;
} // It returns the tree for the given nodeId if it exists or create a new one otherwise

function getRowTree(nodeId, item, expandedRowMap) {
  return expandedRowMap[nodeId] && expandedRowMap[nodeId].nodes ? expandedRowMap[nodeId].nodes : createRowTree(item, nodeId);
}
function findRootNode(node, expandedRowMap) {
  const rootNodeId = node.nodeId.split('_')[0];
  return expandedRowMap[rootNodeId].nodes._root;
}
/********* TABS Functions *********/
//It checks if an element needs a scrolling

function needsScrolling(elementId) {
  const element = document.getElementById(elementId);

  if (element === null) {
    return false;
  }

  return element.scrollWidth > element.offsetWidth;
}

/***/ })

}]);
//# sourceMappingURL=opendistroQueryWorkbenchKibana.chunk.0.js.map