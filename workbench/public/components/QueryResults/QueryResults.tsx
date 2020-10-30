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
// @ts-ignore
import { SortableProperties, SortableProperty } from "@elastic/eui/lib/services";
// @ts-ignore
import { EuiPanel, EuiFlexGroup, EuiFlexItem, EuiTab, EuiTabs, EuiPopover, EuiContextMenuItem, EuiContextMenuPanel, EuiHorizontalRule, EuiSearchBar, Pager, EuiIcon, EuiText, EuiSpacer, EuiTextAlign, EuiButton, EuiButtonIcon } from "@elastic/eui";
import { QueryResult, QueryMessage, Tab, ResponseDetail, ItemIdToExpandedRowMap } from "../Main/main";
import QueryResultsBody from "./QueryResultsBody";
import { getQueryIndex, needsScrolling, getSelectedResults } from "../../utils/utils";
import { DEFAULT_NUM_RECORDS_PER_PAGE, MESSAGE_TAB_LABEL, TAB_CONTAINER_ID } from "../../utils/constants";
import { PanelWrapper } from '../../utils/PanelWrapper';
import _ from 'lodash';

interface QueryResultsProps {
  language: string;
  queries: string[];
  queryResults: ResponseDetail<QueryResult>[];
  queryResultsJSON: string;
  queryResultsJDBC: string;
  queryResultsCSV: string;
  queryResultsTEXT: string;
  messages: QueryMessage[];
  selectedTabName: string;
  selectedTabId: string;
  searchQuery: string;
  tabsOverflow: boolean;
  onSelectedTabIdChange: (tab: Tab) => void;
  onQueryChange: (object: any) => void;
  updateExpandedMap: (map: ItemIdToExpandedRowMap) => void;
  itemIdToExpandedRowMap: ItemIdToExpandedRowMap;
  getJson: (queries: string[]) => void;
  getJdbc: (queries: string[]) => void;
  getCsv: (queries: string[]) => void;
  getText: (queries: string[]) => void;
  isResultFullScreen: boolean;
  setIsResultFullScreen: (isFullScreen: boolean) => void;
}

interface QueryResultsState {
  isPopoverOpen: boolean;
  tabsOverflow: boolean;
  itemsPerPage: number;
}

class QueryResults extends React.Component<QueryResultsProps, QueryResultsState> {
  public sortableColumns: Array<SortableProperty<string>>;
  public sortableProperties: SortableProperties;
  public sortedColumn: string;
  public tabNames: string[];
  public pager: Pager;

  constructor(props: QueryResultsProps) {
    super(props);

    this.state = {
      isPopoverOpen: false,
      tabsOverflow: this.props.tabsOverflow ? this.props.tabsOverflow : false,
      itemsPerPage: DEFAULT_NUM_RECORDS_PER_PAGE
    };

    this.sortableColumns = [];
    this.sortedColumn = "";
    this.sortableProperties = new SortableProperties(
      [
        {
          name: "",
          getValue: (item: any) => "",
          isAscending: true
        }
      ],
      ""
    );

    this.tabNames = [];
    this.pager = new Pager(0, this.state.itemsPerPage);
  }

  componentDidUpdate() {
    const showArrow = needsScrolling("tabsContainer");
    if (showArrow !== this.state.tabsOverflow) {
      this.setState({ tabsOverflow: showArrow });
    }
  }
  // Actions for Tabs Button
  showTabsMenu = (): void => {
    this.setState(prevState => ({
      isPopoverOpen: !prevState.isPopoverOpen
    }));
  };

  slideTabsRight = (): void => {
    if (document.getElementById(TAB_CONTAINER_ID)) {
      document.getElementById(TAB_CONTAINER_ID)!.scrollBy(50, 0);
    }
  };

  slideTabsLeft = (): void => {
    if (document.getElementById(TAB_CONTAINER_ID)) {
      document.getElementById(TAB_CONTAINER_ID)!.scrollBy(-50, 0);
    }
  };

  closePopover = (): void => {
    this.setState({
      isPopoverOpen: false
    });
  };

  onChangeItemsPerPage = (itemsPerPage: number) => {
    this.pager.setItemsPerPage(itemsPerPage);
    this.setState({
      itemsPerPage
    });
  };

  onChangePage = (pageIndex: number) => {
    this.pager.goToPageIndex(pageIndex);
    this.setState({});
  };

  updatePagination(totalItemsCount: number): void {
    this.pager.setTotalItems(totalItemsCount);
  }

  // Update SORTABLE COLUMNS - All columns
  updateSortableColumns(queryResultsSelected: QueryResult): void {
    if (this.sortableColumns.length === 0) {
      queryResultsSelected.fields.map((field: string) => {
        this.sortableColumns.push({
          name: field,
          getValue: (item: any) => item[field],
          isAscending: true
        });
      });
      this.sortedColumn =
        this.sortableColumns.length > 0 ? this.sortableColumns[0].name : "";
      this.sortableProperties = new SortableProperties(
        this.sortableColumns,
        this.sortedColumn
      );
    }
  }

  onSort = (prop: string) => {
    this.sortableProperties.sortOn(prop);
    this.sortedColumn = prop;
    this.setState({});
  };

  renderTabs(): Tab[] {
    const tabs = [
      {
        id: MESSAGE_TAB_LABEL,
        name: _.truncate(MESSAGE_TAB_LABEL, { length: 17 }),
        disabled: false
      }
    ];

    this.tabNames = [];
    if (this.props.queryResults) {
      for (let i = 0; i < this.props.queryResults.length; i += 1) {
        const tabName = this.props.language === "SQL" ? getQueryIndex(this.props.queries[i]) : "Events";
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
    const queryResultSelected = getSelectedResults(this.props.queryResults, this.props.selectedTabId);

    if (queryResultSelected) {
      const matchingItems: object[] = this.props.searchQuery
        ? EuiSearchBar.Query.execute(
          this.props.searchQuery,
          queryResultSelected.records
        )
        : queryResultSelected.records;
      this.updatePagination(matchingItems.length);
      this.updateSortableColumns(queryResultSelected);
    }

    // Action button with list of tabs, TODO: disable tabArrowRight and tabArrowLeft when no more scrolling is possible
    const tabArrowDown = (
      <EuiIcon
        onClick={this.showTabsMenu}
        type={"arrowDown"}
      />
    );
    const tabs: Tab[] = this.renderTabs();
    const tabsItems = tabs.map((tab, index) => (
      <EuiContextMenuItem
        key="10 rows"
        icon="empty"
        style={{ marginRight: 27 }}
        onClick={() => {
          this.closePopover();
          this.pager.goToPageIndex(0);
          this.sortableColumns = [];
          this.props.onSelectedTabIdChange(tab);
        }}
      >
        {tab.name}
      </EuiContextMenuItem>
    ));

    const tabsButtons = tabs.map((tab, index) => (
      <EuiTab
        onClick={() => {
          this.pager.goToPageIndex(0);
          this.sortableColumns = [];
          this.props.onSelectedTabIdChange(tab);
        }}
        isSelected={tab.id === this.props.selectedTabId}
        disabled={tab.disabled}
        key={index}
      >
        {tab.name}
      </EuiTab>
    ));

    return (
      <EuiPanel className="query-result-container" paddingSize="none">
        <div style={{ padding: 20, paddingBottom: 0 }}>
          <EuiFlexGroup>
            <EuiFlexItem>
              <EuiText className="query-result-panel-header">
                <h3>Results</h3>
              </EuiText>
            </EuiFlexItem>
            <EuiFlexItem grow={false}>
              {this.props.queryResults.length > 0 && (
                this.props.isResultFullScreen ?
                  <EuiButtonIcon
                    iconType="cross"
                    color="text"
                    id="exit-fullscreen-button"
                    onClick={() => this.props.setIsResultFullScreen(false)}
                  />
                  :
                  <EuiButton
                    size="s"
                    iconType="fullScreen"
                    onClick={() => this.props.setIsResultFullScreen(true)}
                  >
                    Full screen view
                </EuiButton>
              )}
            </EuiFlexItem>
          </EuiFlexGroup>
        </div>
        {this.props.queryResults.length === 0 ? (
          // show no results message instead of the results table when there are no results
          <>
            <EuiSpacer size="xxl" />
            <EuiSpacer size="xl" />
            <EuiText style={{ color: "#3f3f3f" }}>
              <EuiTextAlign textAlign="center">
                <h4>No result</h4>
              </EuiTextAlign>
              <EuiTextAlign textAlign="center">
                <p>Enter a query in the query editor above to see results.</p>
              </EuiTextAlign>
            </EuiText>
            <EuiSpacer size="xxl" />
            <EuiSpacer size="xl" />
          </>
        ) : (
            <>
              <EuiFlexGroup
                style={{
                  padding: 5
                }}
              >
                {/*TABS*/}
                <EuiFlexGroup
                  className="tabs-container"
                  alignItems="center"
                  gutterSize="s"
                  id="tabsContainer"
                >
                  <EuiFlexItem
                    style={{ marginTop: "8px" }}
                    grow={false}
                  >
                    <EuiTabs>{tabsButtons}</EuiTabs>
                  </EuiFlexItem>
                </EuiFlexGroup>

                {/*ARROW DOWN*/}
                {this.state.tabsOverflow && (
                  <div className="tab-arrow-down-container">
                    <EuiFlexGroup>
                      <EuiFlexItem grow={false}>
                        <EuiPopover
                          id="singlePanel"
                          button={tabArrowDown}
                          data-test-subj="slide-down"
                          isOpen={this.state.isPopoverOpen}
                          closePopover={this.closePopover}
                          panelPaddingSize="none"
                          anchorPosition="downLeft"
                        >
                          <EuiContextMenuPanel items={tabsItems} />
                        </EuiPopover>
                      </EuiFlexItem>
                    </EuiFlexGroup>
                  </div>
                )}
              </EuiFlexGroup>
              <EuiHorizontalRule margin="none" />

              {/*RESULTS TABLE*/}
              <PanelWrapper shouldWrap={this.props.language === 'SQL' && this.props.selectedTabName !== MESSAGE_TAB_LABEL}>
                <QueryResultsBody
                  language={this.props.language}
                  queries={this.props.queries}
                  selectedTabId={this.props.selectedTabId}
                  selectedTabName={this.props.selectedTabName}
                  tabNames={this.tabNames}
                  queryResultSelected={queryResultSelected}
                  queryResultsJSON={this.props.queryResultsJSON}
                  queryResultsJDBC={this.props.queryResultsJDBC}
                  queryResultsCSV={this.props.queryResultsCSV}
                  queryResultsTEXT={this.props.queryResultsTEXT}
                  messages={this.props.messages}
                  searchQuery={this.props.searchQuery}
                  onQueryChange={this.props.onQueryChange}
                  pager={this.pager}
                  itemsPerPage={this.state.itemsPerPage}
                  firstItemIndex={this.pager.getFirstItemIndex()}
                  lastItemIndex={this.pager.getLastItemIndex()}
                  onChangeItemsPerPage={this.onChangeItemsPerPage}
                  onChangePage={this.onChangePage}
                  onSort={this.onSort}
                  sortedColumn={this.sortedColumn}
                  sortableProperties={this.sortableProperties}
                  itemIdToExpandedRowMap={this.props.itemIdToExpandedRowMap}
                  updateExpandedMap={this.props.updateExpandedMap}
                  getJson={this.props.getJson}
                  getJdbc={this.props.getJdbc}
                  getCsv={this.props.getCsv}
                  getText={this.props.getText}
                />
              </PanelWrapper>
            </>
          )}
      </EuiPanel>
    );
  }
}
export default QueryResults;
