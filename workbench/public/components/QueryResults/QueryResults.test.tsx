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
import "regenerator-runtime";
import "mutationobserver-shim";
import "@testing-library/jest-dom/extend-expect";
import { render, fireEvent, configure } from "@testing-library/react";
import { mockQueryResults, mockQueries } from "../../../test/mocks/mockData";
import { MESSAGE_TAB_LABEL } from "../../utils/constants";
import QueryResults from "./QueryResults";
import { Tab, ItemIdToExpandedRowMap, ResponseDetail, QueryResult } from "../Main/main";

configure({ testIdAttribute: 'data-test-subj' });

function renderSQLQueryResults(mockQueryResults: ResponseDetail<QueryResult>[],
  mockQueries: string[] = [],
  mockSearchQuery: string = '',
  onSelectedTabIdChange: (tab: Tab) => void,
  onQueryChange: () => {},
  updateExpandedMap: (map: ItemIdToExpandedRowMap) => {},
  getJson: (queries: string[]) => void,
  getJdbc: (queries: string[]) => void,
  getCsv: (queries: string[]) => void,
  getText: (queries: string[]) => void,
  setIsResultFullScreen: (isFullScreen: boolean) => void) {

  return {
    ...render(
      <QueryResults
        language='SQL'
        queries={mockQueries}
        queryResults={mockQueryResults}
        queryResultsJDBC={''}
        queryResultsJSON={''}
        queryResultsCSV={''}
        queryResultsTEXT={''}
        messages={[]}
        selectedTabId={'0'}
        selectedTabName={MESSAGE_TAB_LABEL}
        onSelectedTabIdChange={onSelectedTabIdChange}
        itemIdToExpandedRowMap={{}}
        onQueryChange={onQueryChange}
        updateExpandedMap={updateExpandedMap}
        searchQuery={mockSearchQuery}
        tabsOverflow={true}
        getJson={getJson}
        getJdbc={getJdbc}
        getCsv={getCsv}
        getText={getText}
        isResultFullScreen={false}
        setIsResultFullScreen={setIsResultFullScreen}
      />
    ),
  };
}

describe("<QueryResults /> spec", () => {
  it("renders the component with no data", async () => {
    (window as any).HTMLElement.prototype.scrollBy = function () { };
    expect(document.body.children[0]).toMatchSnapshot();
  });
});

describe("<QueryResults with data/> spec", () => {
  const onSelectedTabIdChange = jest.fn();
  const onQueryChange = jest.fn();
  const updateExpandedMap = jest.fn();
  const mockSearchQuery = "";
  const getRawResponse = jest.fn();
  const getJdbc = jest.fn();
  const getCsv = jest.fn();
  const getText = jest.fn();
  const setIsResultFullScreen = jest.fn();
  (window as any).HTMLElement.prototype.scrollBy = jest.fn();

  it("renders the component with mock query results", async () => {
    const { getAllByRole, getByText, getAllByText, getAllByTestId, getAllByLabelText } =
      renderSQLQueryResults(mockQueryResults, mockQueries, mockSearchQuery, onSelectedTabIdChange, onQueryChange,
        updateExpandedMap, getRawResponse, getJdbc, getCsv, getText, setIsResultFullScreen);

    expect(document.body.children[0]).toMatchSnapshot();

    // It tests that the selected tab is the first tab with results
    expect(getAllByRole('tab')[0].getAttribute('aria-selected')).toEqual('false');
    expect(getAllByRole('tab')[1].getAttribute('aria-selected')).toEqual('true');

    //It tests that there is one tab for each QueryResult
    expect(getAllByRole('tab')).toHaveLength(11);

    // It tests Tab button
    await fireEvent.click(getAllByRole('tab')[5]);

    // TODO: uncomment this test when sorting is fixed
    // It tests sorting
    // await fireEvent.click(getAllByTestId('tableHeaderSortButton')[1]);

    // It tests pagination
    await fireEvent.click(getAllByLabelText('Page 2 of 2')[0]);
    await fireEvent.click(getAllByText('Rows per page', { exact: false })[0]);
    expect(getByText("10 rows"));
    expect(getByText("20 rows"));
    expect(getByText("50 rows"));
    expect(getByText("100 rows"));
    await fireEvent.click(getByText("20 rows"));
  });

  it("renders the component to test tabs down arrow", async () => {
    const { getAllByTestId } = renderSQLQueryResults(mockQueryResults, mockQueries, mockSearchQuery, onSelectedTabIdChange,
      onQueryChange, updateExpandedMap, getRawResponse, getJdbc, getCsv, getText, setIsResultFullScreen);

    expect(document.body.children[0]).toMatchSnapshot();

    // It tests right scrolling arrows
    expect(getAllByTestId('slide-down'));
    await fireEvent.click(getAllByTestId('slide-down')[0]);
  });

});


function renderPPLQueryResults(mockQueryResults: ResponseDetail<QueryResult>[],
  mockQueries: string[] = [],
  mockSearchQuery: string = '',
  onSelectedTabIdChange: (tab: Tab) => void,
  onQueryChange: () => {},
  updateExpandedMap: (map: ItemIdToExpandedRowMap) => {},
  getJson: (queries: string[]) => void,
  getJdbc: (queries: string[]) => void,
  getCsv: (queries: string[]) => void,
  getText: (queries: string[]) => void,
  setIsResultFullScreen: (isFullScreen: boolean) => void) {

  return {
    ...render(
      <QueryResults
        language='PPL'
        queries={mockQueries}
        queryResults={mockQueryResults}
        queryResultsJDBC={''}
        queryResultsJSON={''}
        queryResultsCSV={''}
        queryResultsTEXT={''}
        messages={[]}
        selectedTabId={'0'}
        selectedTabName={MESSAGE_TAB_LABEL}
        onSelectedTabIdChange={onSelectedTabIdChange}
        itemIdToExpandedRowMap={{}}
        onQueryChange={onQueryChange}
        updateExpandedMap={updateExpandedMap}
        searchQuery={mockSearchQuery}
        tabsOverflow={true}
        getJson={getJson}
        getJdbc={getJdbc}
        getCsv={getCsv}
        getText={getText}
        isResultFullScreen={false}
        setIsResultFullScreen={setIsResultFullScreen}
      />
    ),
  };
}

describe("<QueryResults /> spec", () => {
  it("renders the component with no data", async () => {
    (window as any).HTMLElement.prototype.scrollBy = function () { };

    expect(document.body.children[0]).toMatchSnapshot();
  });
});

describe("<QueryResults with data/> spec", () => {
  const onSelectedTabIdChange = jest.fn();
  const onQueryChange = jest.fn();
  const updateExpandedMap = jest.fn();
  const mockSearchQuery = "";
  const getRawResponse = jest.fn();
  const getJdbc = jest.fn();
  const getCsv = jest.fn();
  const getText = jest.fn();
  const setIsResultFullScreen = jest.fn();
  (window as any).HTMLElement.prototype.scrollBy = jest.fn();

  it("renders the component with mock query results", async () => {
    const { getAllByRole, getByText, getAllByText, getAllByTestId, getAllByLabelText } =
      renderPPLQueryResults(mockQueryResults, mockQueries, mockSearchQuery, onSelectedTabIdChange, onQueryChange,
        updateExpandedMap, getRawResponse, getJdbc, getCsv, getText, setIsResultFullScreen);

    expect(document.body.children[0]).toMatchSnapshot();

    // It tests that the selected tab is the first tab with results
    expect(getAllByRole('tab')[0].getAttribute('aria-selected')).toEqual('false');
    expect(getAllByRole('tab')[1].getAttribute('aria-selected')).toEqual('true');

    //It tests that there is one tab for each QueryResult
    expect(getAllByRole('tab')).toHaveLength(11);

    // It tests Tab button
    await fireEvent.click(getAllByRole('tab')[5]);

    // TODO: uncomment this test when sorting is fixed
    // It tests sorting
    // await fireEvent.click(getAllByTestId('tableHeaderSortButton')[1]);

    // It tests pagination
    await fireEvent.click(getAllByLabelText('Page 2 of 2')[0]);
    await fireEvent.click(getAllByText('Rows per page', { exact: false })[0]);
    expect(getByText("10 rows"));
    expect(getByText("20 rows"));
    expect(getByText("50 rows"));
    expect(getByText("100 rows"));
    await fireEvent.click(getByText("20 rows"));
  });

  it("renders the component to test tabs down arrow", async () => {
    const { getAllByTestId } = renderPPLQueryResults(mockQueryResults, mockQueries, mockSearchQuery, onSelectedTabIdChange,
      onQueryChange, updateExpandedMap, getRawResponse, getJdbc, getCsv, getText, setIsResultFullScreen);

    expect(document.body.children[0]).toMatchSnapshot();

    // It tests right scrolling arrows
    expect(getAllByTestId('slide-down'));
    await fireEvent.click(getAllByTestId('slide-down')[0]);
  });

});
