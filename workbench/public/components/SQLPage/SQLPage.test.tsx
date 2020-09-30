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
import "@testing-library/jest-dom/extend-expect";
import { render, fireEvent } from "@testing-library/react";
import { SQLPage } from "./SQLPage";


describe("<SQLPage /> spec", () => {

  it("renders the component", () => {
    render(
      <SQLPage
        onRun={() => { }}
        onTranslate={() => { }}
        onClear={() => { }}
        updateSQLQueries={() => { }}
        sqlTranslations={[]}
        sqlQuery={''}
      />
    );
    expect(document.body.children[0]).toMatchSnapshot();
  });

  it('tests the action buttons', async () => {
    const onRun = jest.fn();
    const onTranslate = jest.fn();
    const onClean = jest.fn();
    const updateSQLQueries = jest.fn();

    const { getByText } = render(
      <SQLPage
        onRun={onRun}
        onTranslate={onTranslate}
        onClear={onClean}
        updateSQLQueries={updateSQLQueries}
        sqlTranslations={[]}
        sqlQuery={''}
      />
    );

    expect(document.body.children[0]).toMatchSnapshot();

    fireEvent.click(getByText('Run'));
    expect(onRun).toHaveBeenCalledTimes(1);

    fireEvent.click(getByText('Clear'));
    expect(onClean).toHaveBeenCalledTimes(1);

    fireEvent.click(getByText('Explain'));
    expect(onTranslate).toHaveBeenCalledTimes(1);

  });

});



