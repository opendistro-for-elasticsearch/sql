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

/// <reference types="cypress" />

import { edit } from "brace";
import { delay, testQueries, verifyDownloadData, files } from "../utils/constants";

describe('Test UI buttons', () => {
  beforeEach(() => {
    cy.visit('app/opendistro-sql-workbench');
  });

  it('Test Run button and field search', () => {
    cy.get('textarea.ace_text-input').eq(0).focus().type('{enter}select * from accounts where balance > 49500;', { force: true });
    cy.wait(delay);
    cy.get('.euiButton__text').contains('Run').click();
    cy.wait(delay);
    cy.get('.euiTab__content').contains('accounts').click();

    cy.get('input.euiFieldSearch').type('marissa');
    cy.get('span.euiTableCellContent__text').eq(15).should((account_number) => {
      expect(account_number).to.contain('803');
    });
  });

  it('Test Translate button', () => {
    cy.get('textarea.ace_text-input').eq(0).focus().type('{selectall}{backspace}', { force: true });
    cy.wait(delay);
    cy.get('textarea.ace_text-input').eq(0).focus().type('{selectall}{backspace}select log(balance) from accounts where abs(age) > 20;', { force: true });
    cy.wait(delay);
    cy.get('.euiButton__text').contains('Translate').click();
    cy.wait(delay);

    // Note: Translation retrived this way will get cut off, so doing a substring check
    cy.get('.ace_content').eq(1).then((translate_editor) => {
      const editor = edit(translate_editor[0]);
      expect(editor.getValue()).to.have.string("Math.abs(doc['age'].value);abs_1 > 20");
    });
  });

  it('Test Clear button', () => {
    cy.get('.euiButton__text').contains('Clear').click();
    cy.wait(delay);

    cy.get('.ace_content').eq(0).then((sql_query_editor) => {
      const editor = edit(sql_query_editor[0]);
      expect(editor.getValue()).to.equal('');
    });
  });
});

describe('Test and verify downloads', () => {
  verifyDownloadData.map(({ title, url, file }) => {
    it(title, () => {
      cy.request({
        method: 'POST',
        form: true,
        url: url,
        headers: {
          'content-type': 'application/json;charset=UTF-8',
          'kbn-version': '7.8.0',
        },
        body: {
          'query': 'select * from accounts where balance > 49500'
        }
      }).then(response => {
        expect(response.body.resp).to.have.string(files[file]);
      });
    });
  });
});

describe('Test table display', () => {
  beforeEach(() => {
    cy.visit('app/opendistro-sql-workbench');
    cy.get('textarea.ace_text-input').eq(0).focus().type('{selectall}{backspace}', { force: true });
    cy.wait(delay);
  });

  testQueries.map(({ title, query, cell_idx, expected_string }) => {
    it(title, () => {
      cy.get('textarea.ace_text-input').eq(0).focus().type(`{selectall}{backspace}${query}`, { force: true });
      cy.wait(delay);
      cy.get('.euiButton__text').contains('Run').click();
      cy.wait(delay);

      cy.get('span.euiTableCellContent__text').eq(cell_idx).should((cell) => {
        expect(cell).to.contain(expected_string);
      });
    });
  });

  it('Test nested fields display', () => {
    cy.get('textarea.ace_text-input').eq(0).focus().type(`{selectall}{backspace}select * from employee_nested;`, { force: true });
    cy.wait(delay);
    cy.get('.euiButton__text').contains('Run').click();
    cy.wait(delay);

    cy.get('span.euiTableCellContent__text').eq(21).click();
    cy.wait(delay);
    cy.get('span.euiTableCellContent__text').eq(27).should((cell) => {
      expect(cell).to.contain('2018-06-23');
    });
  });
});