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

import React, { Fragment } from "react";
// @ts-ignore
import { SortableProperties } from "@elastic/eui/lib/services";
// @ts-ignore
import { EuiCodeEditor, EuiModal, EuiModalBody, EuiModalFooter, EuiModalHeader, EuiModalHeaderTitle, EuiOverlayMask, EuiPanel, EuiPortal, EuiSearchBar, EuiSideNav } from "@elastic/eui";
import {
  EuiButton,
  EuiButtonIcon,
  EuiContextMenu,
  EuiFlexGroup,
  EuiFlexItem,
  EuiHorizontalRule,
  EuiLink,
  EuiPopover,
  EuiSpacer,
  EuiTable,
  EuiTableBody,
  EuiTableHeader,
  EuiTableHeaderCell,
  EuiTablePagination,
  EuiTableRow,
  EuiTableRowCell,
  EuiText,
  Pager
} from "@elastic/eui";
import {
  capitalizeFirstLetter,
  findRootNode,
  getMessageString,
  getRowTree,
  isEmpty,
  Node,
  onDownloadFile,
  scrollToNode
} from "../../utils/utils";
import "../../ace-themes/sql_console";
import { COLUMN_WIDTH, MEDIUM_COLUMN_WIDTH, PAGE_OPTIONS, SMALL_COLUMN_WIDTH } from "../../utils/constants";
import { ItemIdToExpandedRowMap, QueryMessage, QueryResult } from "../Main/main";

const DoubleScrollbar = require('react-double-scrollbar');

interface QueryResultsBodyProps {
  language: string;
  queries: string[];
  queryResultSelected: QueryResult;
  queryResultsJDBC: string;
  queryResultsCSV: string;
  queryResultsJSON: string;
  queryResultsTEXT: string;
  tabNames: string[];
  selectedTabName: string;
  selectedTabId: string;
  searchQuery: string;
  sortableProperties: SortableProperties;
  messages: QueryMessage[];
  pager: Pager;
  itemsPerPage: number;
  firstItemIndex: number;
  lastItemIndex: number;
  itemIdToExpandedRowMap: ItemIdToExpandedRowMap;
  sortedColumn: string;
  onChangeItemsPerPage: (itemsPerPage: number) => void;
  onChangePage: (pageIndex: number) => void;
  onSort: (prop: string) => void;
  onQueryChange: (query: object) => void;
  updateExpandedMap: (map: ItemIdToExpandedRowMap) => void;
  getJson: (queries: string[]) => void;
  getJdbc: (queries: string[]) => void;
  getCsv: (queries: string[]) => void;
  getText: (queries: string[]) => void;
}

interface QueryResultsBodyState {
  itemIdToSelectedMap: object;
  itemIdToOpenActionsPopoverMap: object;
  incremental: boolean;
  filters: boolean;
  itemIdToExpandedRowMap: ItemIdToExpandedRowMap;
  searchQuery: string;
  selectedItemMap: object;
  selectedItemName: string;
  selectedItemData: object;
  navView: boolean;
  isPopoverOpen: boolean;
  isDownloadPopoverOpen: boolean;
  isModalVisible: boolean;
  downloadErrorModal: any;
}

interface FieldValue {
  hasExpandingRow: boolean;
  hasExpandingArray: boolean;
  value: string;
  link: string;
}

class QueryResultsBody extends React.Component<QueryResultsBodyProps, QueryResultsBodyState> {
  public items: any[];
  public columns: any[];
  public panels: any[];
  public expandedRowColSpan: number;

  constructor(props: QueryResultsBodyProps) {
    super(props);

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

    this.panels = [
      {
        id: 0,
        items: [
          {
            name: "Download JSON",
            onClick: () => {
              this.onDownloadJSON();
            }
          },
          {
            name: "Download JDBC",
            onClick: () => {
              this.onDownloadJDBC();
            }
          },
          {
            name: "Download CSV",
            onClick: () => {
              this.onDownloadCSV();
            }
          },
          {
            name: "Download Text",
            onClick: () => {
              this.onDownloadText();
            }
          }
        ]
      }
    ];
  }

  setIsModalVisible(visible: boolean): void {
    this.setState({
      isModalVisible: visible
    })
  }

  getModal = (errorMessage: string): any => {
    const closeModal = () => this.setIsModalVisible(false);
    let modal = (
      <EuiOverlayMask onClick={closeModal}>
        <EuiModal onClose={closeModal}>
          <EuiModalHeader>
            <EuiModalHeaderTitle>Error</EuiModalHeaderTitle>
          </EuiModalHeader>

          <EuiModalBody>
            <EuiText>
              {errorMessage}
            </EuiText>
          </EuiModalBody>

          <EuiModalFooter>
            <EuiButton onClick={closeModal} fill>
              Close
            </EuiButton>
          </EuiModalFooter>
        </EuiModal>
      </EuiOverlayMask>
    );
    return modal;
  }

  // Actions for Download files
  onDownloadJSON() {
    if (this.props.language == 'PPL') {
      this.setState({
        downloadErrorModal: this.getModal("PPL result in JSON format is not supported, please select JDBC format."),
      })
      this.setIsModalVisible(true);
      return;
    }
    if (!this.props.queryResultsJSON) {
      this.props.getJson(this.props.queries);
    }
    setTimeout(() => {
      const jsonObject = JSON.parse(this.props.queryResultsJSON);
      const data = JSON.stringify(jsonObject, undefined, 4);
      onDownloadFile(data, "json", this.props.selectedTabName + ".json");
    }, 2000);
  };

  onDownloadJDBC = (): void => {
    if (!this.props.queryResultsJDBC) {
      this.props.getJdbc(this.props.queries);
    }
    setTimeout(() => {
      const jsonObject = JSON.parse(this.props.queryResultsJDBC);
      const data = JSON.stringify(jsonObject, undefined, 4);
      onDownloadFile(data, "json", this.props.selectedTabName + ".json");
    }, 2000);
  };

  onDownloadCSV = (): void => {
    if (this.props.language == 'PPL') {
      this.setState({
        downloadErrorModal: this.getModal("PPL result in CSV format is not supported, please select JDBC format."),
      })
      this.setIsModalVisible(true);
      return;
    }
    if (!this.props.queryResultsCSV) {
      this.props.getCsv(this.props.queries);
    }
    setTimeout(() => {
      const data = this.props.queryResultsCSV;
      onDownloadFile(data, "csv", this.props.selectedTabName + ".csv");
    }, 2000);
  };

  onDownloadText = (): void => {
    if (this.props.language == 'PPL') {
      this.setState({
        downloadErrorModal: this.getModal("PPL result in Text format is not supported, please select JDBC format."),
      })
      this.setIsModalVisible(true);
      return;
    }
    if (!this.props.queryResultsTEXT) {
      this.props.getText(this.props.queries);
    }
    setTimeout(() => {
      const data = this.props.queryResultsTEXT;
      onDownloadFile(data, "plain", this.props.selectedTabName + ".txt");
    }, 2000);
  };

  // Actions for Downloads Button
  onDownloadButtonClick = (): void => {
    this.setState(prevState => ({
      isDownloadPopoverOpen: !prevState.isDownloadPopoverOpen
    }));
  };

  closeDownloadPopover = (): void => {
    this.setState({
      isDownloadPopoverOpen: false
    });
  };

  // It sorts and filters table values
  getItems(records: { [key: string]: any }[]) {
    const matchingItems = this.props.searchQuery ? EuiSearchBar.Query.execute(this.props.searchQuery, records) : records;
    return this.props.sortableProperties.sortItems(matchingItems);
  }

  // It processes field values and determines whether it should link to an expanded row or an expanded array
  getFieldValue(fieldValue: any, field: string): FieldValue {
    let hasExpandingRow: boolean = false;
    let hasExpandingArray: boolean = false;
    let value: string = "";
    let link: string = "";

    if (fieldValue === null) {
      return {
        hasExpandingRow: hasExpandingRow,
        value: "",
        hasExpandingArray,
        link
      };
    }

    // Not an object or array
    if (typeof fieldValue !== "object") {
      return {
        hasExpandingRow: hasExpandingRow,
        value: fieldValue,
        hasExpandingArray,
        link
      };
    }

    // Array of strings or objects
    if (Array.isArray(fieldValue)) {
      if (typeof fieldValue[0] !== "object") {
        hasExpandingArray = true;
        link = field.concat(": [", fieldValue.length.toString(), "]");
      } else {
        hasExpandingRow = true;
        link = field.concat(": {", fieldValue.length.toString(), "}");
      }
    }
    // Single object
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

  addExpandingNodeIcon(node: Node, expandedRowMap: ItemIdToExpandedRowMap) {
    return (
      <EuiButtonIcon
        style={{ marginLeft: -4 }}
        onClick={() => this.toggleNodeData(node, expandedRowMap)}
        aria-label={
          expandedRowMap[node.nodeId] && expandedRowMap[node.nodeId].expandedRow
            ? "Collapse"
            : "Expand"
        }
        iconType={
          expandedRowMap[node.nodeId] && expandedRowMap[node.nodeId].expandedRow
            ? "minusInCircle"
            : "plusInCircle"
        }
      />
    );
  }

  addExpandingSideNavIcon(node: Node, expandedRowMap: ItemIdToExpandedRowMap) {
    if (!node.parent) {
      return;
    }
    return (
      <EuiButtonIcon
        onClick={() => this.updateExpandedRowMap(node, expandedRowMap)}
        aria-label={
          expandedRowMap[node.parent!.nodeId] &&
            expandedRowMap[node.parent!.nodeId].selectedNodes &&
            expandedRowMap[node.parent!.nodeId].selectedNodes.hasOwnProperty(node.nodeId)
            ? "Collapse"
            : "Expand"
        }
        iconType={
          expandedRowMap[node.parent!.nodeId] &&
            expandedRowMap[node.parent!.nodeId].selectedNodes &&
            expandedRowMap[node.parent!.nodeId].selectedNodes.hasOwnProperty(node.nodeId)
            ? "minusInCircle"
            : "plusInCircle"
        }
      />
    );
  }

  addExpandingIconColumn(columns: any[]) {
    const expandIconColumn = [
      {
        id: "expandIcon",
        label: "",
        isSortable: false,
        width: "30px"
      }
    ];

    columns = expandIconColumn.concat(columns);
    return columns;
  }

  updateSelectedNodes(parentNode: Node, selectedNode: Node, expandedRowMap: ItemIdToExpandedRowMap, keepOpen = false) {
    if (!parentNode) {
      return expandedRowMap;
    }
    const parentNodeId = parentNode.nodeId;

    if (
      expandedRowMap[parentNodeId] &&
      expandedRowMap[parentNodeId].selectedNodes &&
      expandedRowMap[parentNodeId].selectedNodes!.hasOwnProperty(selectedNode.nodeId) &&
      !keepOpen
    ) {
      delete expandedRowMap[parentNodeId].selectedNodes[selectedNode.nodeId];
    } else {
      if (!expandedRowMap[parentNodeId].selectedNodes) {
        expandedRowMap[parentNodeId].selectedNodes = {};
      }
      expandedRowMap[parentNodeId].selectedNodes[selectedNode.nodeId] = selectedNode.data;
    }
    return expandedRowMap;
  }

  updateExpandedRow(node: Node, expandedRowMap: ItemIdToExpandedRowMap) {
    let newItemIdToExpandedRowMap = expandedRowMap;

    if (expandedRowMap[node.nodeId]) {
      newItemIdToExpandedRowMap[node.nodeId].expandedRow = (
        <div id={node.nodeId} style={{ padding: "0 0 20px 19px" }}>
          {this.renderNav(node, node.name, expandedRowMap)}
        </div>
      );
    }
    return newItemIdToExpandedRowMap;
  }

  updateExpandedRowMap(node: Node | undefined, expandedRowMap: ItemIdToExpandedRowMap, keepOpen = false) {
    if (!node) {
      return expandedRowMap;
    }
    let newItemIdToExpandedRowMap = this.updateSelectedNodes(node.parent, node, expandedRowMap, keepOpen);
    const rootNode = findRootNode(node, expandedRowMap);
    if (expandedRowMap[rootNode.nodeId]) {
      newItemIdToExpandedRowMap = this.updateExpandedRow(node.parent, newItemIdToExpandedRowMap);
      newItemIdToExpandedRowMap = this.updateExpandedRow(rootNode, newItemIdToExpandedRowMap);
    }
    this.props.updateExpandedMap(newItemIdToExpandedRowMap);
  }

  toggleNodeData = (node: Node, expandedRowMap: ItemIdToExpandedRowMap) => {
    let newItemIdToExpandedRowMap = expandedRowMap;
    const rootNode = findRootNode(node, expandedRowMap);

    if (expandedRowMap[node.nodeId] && expandedRowMap[node.nodeId].expandedRow) {
      delete newItemIdToExpandedRowMap[node.nodeId].expandedRow;
    } else if (node.children && node.children.length > 0) {
      newItemIdToExpandedRowMap = this.updateExpandedRow(node, expandedRowMap);
    }

    if (rootNode !== node) {
      newItemIdToExpandedRowMap = this.updateExpandedRow(rootNode, expandedRowMap);
    }

    this.props.updateExpandedMap(newItemIdToExpandedRowMap);
  };

  getChildrenItems(nodes: Node[], parentNode: Node, expandedRowMap: ItemIdToExpandedRowMap) {
    const itemList: any[] = [];

    if (nodes.length === 0 && parentNode.data) {
      const renderedData = this.renderNodeData(parentNode, expandedRowMap);
      itemList.push(
        this.createItem(expandedRowMap, parentNode, renderedData, {
          items: [],
        })
      );
    }

    for (let i = 0; i < nodes.length; i++) {
      itemList.push(
        this.createItem(expandedRowMap, nodes[i], nodes[i].name,
          {
            icon: this.addExpandingSideNavIcon(nodes[i], expandedRowMap),
            items: this.getChildrenItems(
              nodes[i].children,
              nodes[i],
              expandedRowMap
            )
          })
      );
    }
    return itemList;
  }

  createItem = (expandedRowMap: ItemIdToExpandedRowMap, node: Node, name: any, items = {}) => {
    const nodeId = node.nodeId;
    let isSelected: boolean | undefined = false;

    if (!isEmpty(node.parent)) {
      isSelected = expandedRowMap[node.parent!.nodeId] &&
        expandedRowMap[node.parent!.nodeId].selectedNodes &&
        expandedRowMap[node.parent!.nodeId].selectedNodes!.hasOwnProperty(nodeId);
    }

    return {
      ...items,
      id: nodeId,
      name,
      isSelected: isSelected,
      onClick: () => console.log('open side nav'),
    };
  };

  /************* Render Functions *************/

  renderMessagesTab(): JSX.Element {
    return (
      <EuiCodeEditor
        className={
          this.props.messages && this.props.messages.length > 0
            ? this.props.messages[0].className
            : "successful-message"
        }
        mode="text"
        theme="sql_console"
        width="100%"
        value={getMessageString(this.props.messages, this.props.tabNames)}
        showPrintMargin={false}
        readOnly={true}
        setOptions={{
          fontSize: "14px",
          readOnly: true,
          highlightActiveLine: false,
          highlightGutterLine: false
        }}
        aria-label="Code Editor"
      />
    );
  }

  renderHeaderCells(columns: any[]) {
    return columns.map((field: any) => {
      const label = field.id === "expandIcon" ? field.label : field;
      const colwidth = field.id === "expandIcon" ? SMALL_COLUMN_WIDTH : field === "id" ? MEDIUM_COLUMN_WIDTH : COLUMN_WIDTH;
      return (
        <EuiTableHeaderCell
          key={label}
          width={colwidth}
          onSort={this.props.onSort.bind(this, field)}
          isSorted={this.props.sortedColumn === field}
          isSortAscending={this.props.sortableProperties.isAscendingByName(
            field
          )}
        >
          {label}
        </EuiTableHeaderCell>
      );
    });
  }
  // Inner tables sorting is not enabled
  renderHeaderCellsWithNoSorting(columns: any[]) {
    return columns.map((field: any) => {
      const label = field.id === "expandIcon" ? field.label : field;
      const colwidth = field.id === "expandIcon" ? field.width : COLUMN_WIDTH;
      return (
        <EuiTableHeaderCell key={label} width={colwidth}>
          {label}
        </EuiTableHeaderCell>
      );
    });
  }

  renderRow(item: any, columns: string[], rowId: string, expandedRowMap: ItemIdToExpandedRowMap) {
    let rows: any[] = [];
    // If the item is an array or an object we add it to the expandedRowMap
    if (item && ((typeof item === "object" && !isEmpty(item)) || (Array.isArray(item) && item.length > 0))
    ) {
      let rowItems: any[] = [];

      if (Array.isArray(item)) {
        rowItems = item;
      } else {
        rowItems.push(item);
      }

      for (let i = 0; i < rowItems.length; i++) {
        let rowItem = rowItems[i];
        let tableCells: Array<any> = [];
        const tree = getRowTree(rowId, rowItem, expandedRowMap);

        // Add nodes to expandedRowMap
        if (!expandedRowMap[rowId] || !expandedRowMap[rowId].nodes) {
          expandedRowMap[rowId] = { nodes: tree };
        }
        const expandingNode =
          tree && tree._root.children.length > 0 ? this.addExpandingNodeIcon(tree._root, expandedRowMap) : "";

        if (columns.length > 0) {
          columns.map((field: any) => {
            // Table cell
            if (field.id !== "expandIcon") {
              const fieldObj = this.getFieldValue(rowItem[field], field);
              let fieldValue: any;

              // If field is expandable
              if (fieldObj.hasExpandingRow || fieldObj.hasExpandingArray) {
                const fieldNode = expandedRowMap[tree._root.nodeId].nodes._root.children.find((node: Node) => node.name === field);
                fieldValue = (
                  <span>
                    {" "}
                    {fieldObj.value}
                    <EuiLink
                      color="primary"
                      onClick={() => {
                        this.updateExpandedRowMap(
                          fieldNode,
                          expandedRowMap,
                          true
                        );
                        scrollToNode(tree._root.nodeId);
                      }}
                    >
                      {fieldObj.link}
                    </EuiLink>
                  </span>
                );
              } else {
                fieldValue = fieldObj.value;
              }
              tableCells.push(
                <EuiTableRowCell
                  key={field}
                  truncateText={false}
                  textOnly={true}
                >
                  {fieldValue}
                </EuiTableRowCell>
              );
            }

            // Expanding icon cell
            else {
              tableCells.push(
                <EuiTableRowCell id={tree._root.nodeId}>
                  {expandingNode}
                </EuiTableRowCell>
              );
            }
          });
        } else {
          const fieldObj = this.getFieldValue(rowItem, "");
          tableCells.push(
            <EuiTableRowCell truncateText={false} textOnly={true}>
              {fieldObj.value}
            </EuiTableRowCell>
          );
        }

        const tableRow = <EuiTableRow key={rowId} data-test-subj={'tableRow'}>{tableCells} </EuiTableRow>;
        let row = <Fragment>{tableRow}</Fragment>;

        if (expandedRowMap[rowId] && expandedRowMap[rowId].expandedRow) {
          const tableRow = (
            <EuiTableRow className="expanded-row" key={rowId}>
              {tableCells}{" "}
            </EuiTableRow>
          );
          const expandedRow = (
            <EuiTableRow>{expandedRowMap[rowId].expandedRow}</EuiTableRow>
          );
          row = (
            <Fragment>
              {tableRow}
              {expandedRow}
            </Fragment>
          );
        }
        rows.push(row);
      }
    }
    return rows;
  }

  renderRows(items: any, columns: string[], expandedRowMap: ItemIdToExpandedRowMap) {
    let rows: any[] = [];
    if (items) {
      for (
        let itemIndex = this.props.firstItemIndex;
        itemIndex <= this.props.lastItemIndex;
        itemIndex++
      ) {
        const item = items[itemIndex];
        if (item) {
          const rowId = item["id"].toString();
          const rowsForItem = this.renderRow(
            item,
            columns,
            rowId,
            expandedRowMap
          );
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
    return (
      <div className="search-bar">
        <EuiSearchBar
          onChange={this.props.onQueryChange}
          query={this.props.searchQuery}
          {...search}
        />
      </div>
    );
  }

  renderNodeData = (node: Node, expandedRowMap: ItemIdToExpandedRowMap) => {
    let items: any[] = [];
    let columns: string[] = [];
    let records: any[] = [];
    const data = node.data;

    if (Array.isArray(data)) {
      items = data;
      columns = typeof items[0] === "object" ? Object.keys(items[0]) : [];
    } else if (typeof data === "object") {
      records.push(data);
      items = records;
      columns = this.addExpandingIconColumn(Object.keys(data));
    }

    return (
      <div>
        <EuiTable className="sideNav-table">
          <EuiTableHeader className="table-header">
            {this.renderHeaderCellsWithNoSorting(columns)}
          </EuiTableHeader>

          <EuiTableBody>
            {this.renderRow(items, columns, node.nodeId.toString(), expandedRowMap)}
          </EuiTableBody>
        </EuiTable>
      </div>
    );
  };

  renderNav(node: Node, table_name: string, expandedRowMap: ItemIdToExpandedRowMap) {
    const sideNav = [
      {
        items: this.getChildrenItems(node.children, node, expandedRowMap),
        id: node.nodeId,
        name: node.name,
        isSelected: false,
        onClick: () => console.log('open side nav'),
      }
    ];

    return (
      <EuiSideNav
        mobileTitle="Navigate within $APP_NAME"
        items={sideNav}
        className="sideNavItem__items"
        style={{ width: "300px", padding: "0 0 20px 9px" }}
      />
    );
  }

  render() {
    // Action button with list of downloads
    const downloadsButton = (
      <EuiButton
        iconType="arrowDown"
        iconSide="right"
        size="s"
        onClick={this.onDownloadButtonClick}
      >
        Download
      </EuiButton>
    );

    let modal;

    if (this.state.isModalVisible) {
      modal = this.state.downloadErrorModal;
    }

    if (
      // this.props.selectedTabId === MESSAGE_TAB_LABEL ||
      this.props.queryResultSelected == undefined
    ) {
      return this.renderMessagesTab();
    } else {
      if (this.props.queryResultSelected) {
        this.items = this.getItems(this.props.queryResultSelected.records);
        //Adding an extra empty column for the expanding icon
        this.columns = this.addExpandingIconColumn(this.props.queryResultSelected.fields);
        this.expandedRowColSpan = this.columns.length;
      }

      return (
        <div>
          {this.props.language === 'SQL' && (
            <>
              <EuiFlexGroup alignItems="flexStart" style={{ padding: 20, paddingBottom: 0 }}>
                {/*Table name*/}
                <EuiFlexItem>
                  <EuiText className="table-name">
                    <h4>
                      {this.props.selectedTabName}
                      <span className="table-item-count">{` (${this.items.length})`}</span>
                    </h4>
                  </EuiText>
                  <div className="search-panel">
                    {/*Search Bar*/}
                    {this.renderSearchBar()}
                  </div>
                </EuiFlexItem>

                {/*Download button*/}
                <EuiFlexItem grow={false}>
                  <div className="download-container">
                    <EuiPopover
                      className="download-button-container"
                      id="singlePanel"
                      button={downloadsButton}
                      isOpen={this.state.isDownloadPopoverOpen}
                      closePopover={this.closeDownloadPopover}
                      panelPaddingSize="none"
                      anchorPosition="downLeft"
                    >
                      <EuiContextMenu initialPanelId={0} panels={this.panels} />
                    </EuiPopover>
                  </div>
                </EuiFlexItem>
              </EuiFlexGroup>
              {modal}
            </>
          )}

          {/*Table*/}
          <div className="sql-console-results-container">
            {/*Add a scrollbar on top of the table*/}
            <DoubleScrollbar>
              <EuiFlexGroup gutterSize="none">
                <EuiFlexItem>
                  <EuiTable>
                    <EuiTableHeader className="table-header">
                      {this.renderHeaderCells(this.columns)}
                    </EuiTableHeader>

                    <EuiTableBody>
                      {this.renderRows(
                        this.items,
                        this.columns,
                        this.props.itemIdToExpandedRowMap
                      )}
                    </EuiTableBody>
                  </EuiTable>
                </EuiFlexItem>
              </EuiFlexGroup>
            </DoubleScrollbar>
          </div>

          <div className="pagination-container">
            <EuiTablePagination
              activePage={this.props.pager.getCurrentPageIndex()}
              itemsPerPage={this.props.itemsPerPage}
              itemsPerPageOptions={PAGE_OPTIONS}
              pageCount={this.props.pager.getTotalPages()}
              onChangeItemsPerPage={this.props.onChangeItemsPerPage}
              onChangePage={this.props.onChangePage}
            />
          </div>
        </div>
      );
    }
  }
}
export default QueryResultsBody;
