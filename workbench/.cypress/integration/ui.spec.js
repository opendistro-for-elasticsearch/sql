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


describe('Test PPL UI', () => {
  beforeEach(() => {
    cy.visit('app/opendistro-query-workbench');
    cy.wait(delay);
    cy.get('.euiToggle__input[title=PPL]').click({ force: true });
    cy.wait(delay);
  });

  it('Confirm results are empty', () => {
    cy.get('.euiTextAlign')
      .contains('Enter a query in the query editor above to see results.')
      .should('have.length', 1);
  });

  it('Test Run button', () => {
    cy.get('textarea.ace_text-input').eq(0).focus().type('source=accounts', { force: true });
    cy.wait(delay);
    cy.get('.euiButton__text').contains('Run').click({ force: true });
    cy.wait(delay);
    cy.get('.euiTab__content').contains('Events').click({ force: true });

    cy.get('span.euiTableCellContent__text')
      .eq(19)
      .should((employer) => {
        expect(employer).to.contain('Pyrami');
      });
  });

  it('Test Clear button', () => {
    cy.get('textarea.ace_text-input').eq(0).focus().type('source=accounts', { force: true });
    cy.wait(delay);
    cy.get('.euiButton__text').contains('Run').click({ force: true });
    cy.wait(delay);
    cy.get('.euiTab__content').contains('Events').click({ force: true });
    cy.wait(delay);
    cy.get('.euiButton__text').contains('Clear').click({ force: true });
    cy.wait(delay);

    cy.get('.euiTextAlign')
      .contains('Enter a query in the query editor above to see results.')
      .should('have.length', 1);
    cy.get('.ace_content')
      .eq(0)
      .then((queryEditor) => {
        const editor = edit(queryEditor[0]);
        expect(editor.getValue()).to.equal('');
      });
  });

  it('Test full screen view', () => {
    cy.get('.euiButton__text').contains('Full screen view').should('not.exist');
    cy.get('.euiTitle').contains('Query Workbench').should('exist');

    cy.get('textarea.ace_text-input').eq(0).focus().type('source=accounts', { force: true });
    cy.wait(delay);
    cy.get('.euiButton__text').contains('Run').click({ force: true });
    cy.wait(delay);
    cy.get('.euiButton__text').contains('Full screen view').click({ force: true });

    cy.get('.euiTitle').should('not.exist');

    cy.get('button#exit-fullscreen-button').click({ force: true });
    cy.wait(delay);
    cy.get('.euiButton__text').contains('Full screen view').should('exist');
    cy.get('.euiTitle').contains('Query Workbench').should('exist');
  });
});

describe('Test SQL UI', () => {
  beforeEach(() => {
    cy.visit('app/opendistro-query-workbench');
    cy.wait(delay);
    cy.get('.euiToggle__input[title=SQL]').click({ force: true });
    cy.wait(delay);
  });

  it('Confirm results are empty', () => {
    cy.get('.euiTextAlign')
      .contains('Enter a query in the query editor above to see results.')
      .should('have.length', 1);
  });

  it('Test Run button and field search', () => {
    cy.get('textarea.ace_text-input')
      .eq(0)
      .focus()
      .type('{enter}select * from accounts where balance > 49500;', { force: true });
    cy.wait(delay);
    cy.get('.euiButton__text').contains('Run').click({ force: true });
    cy.wait(delay);
    cy.get('.euiTab__content').contains('accounts').click({ force: true });

    cy.get('input.euiFieldSearch').type('marissa');
    cy.get('span.euiTableCellContent__text')
      .eq(13)
      .should((account_number) => {
        expect(account_number).to.contain('803');
      });
  });

  it('Test Translate button', () => {
    cy.get('textarea.ace_text-input').eq(0).focus().type('{selectall}{backspace}', { force: true });
    cy.wait(delay);
    cy.get('textarea.ace_text-input')
      .eq(0)
      .focus()
      .type('{selectall}{backspace}select log(balance) from accounts where abs(age) > 20;', {
        force: true,
      });
    cy.wait(delay);
    cy.get('.euiButton__text').contains('Explain').click({ force: true });
    cy.wait(delay);

    // hard to get euiCodeBlock content, check length instead
    cy.get('.euiCodeBlock__code').children().should('have.length', 13);
  });

  it('Test Clear button', () => {
    cy.get('.euiButton__text').contains('Clear').click({ force: true });
    cy.wait(delay);

    cy.get('.ace_content')
      .eq(0)
      .then((queryEditor) => {
        const editor = edit(queryEditor[0]);
        expect(editor.getValue()).to.equal('');
      });
  });

  it('Test full screen view', () => {
    cy.get('.euiButton__text').contains('Full screen view').should('not.exist');
    cy.get('.euiTitle').contains('Query Workbench').should('exist');

    cy.get('.euiButton__text').contains('Run').click({ force: true });
    cy.wait(delay);
    cy.get('.euiButton__text').contains('Full screen view').click({ force: true });

    cy.get('.euiTitle').should('not.exist');
  });
});

describe('Test and verify SQL downloads', () => {
  verifyDownloadData.map(({ title, url, file }) => {
    it(title, () => {
      cy.request({
        method: 'POST',
        form: true,
        url: url,
        headers: {
          'content-type': 'application/json;charset=UTF-8',
          'kbn-version': '7.10.2',
        },
        body: {
          'query': 'select * from accounts where balance > 49500'
        }
      }).then((response) => {
        if (title === 'Download and verify CSV') {
          expect(response.body.data.body).to.have.string(files[file]);
        }
        else {
          expect(response.body.data.resp).to.have.string(files[file]);
        }
      });
    });
  });
});

describe('Test table display', () => {
  beforeEach(() => {
    cy.visit('app/opendistro-query-workbench');
    cy.wait(delay);
    cy.get('.euiToggle__input[title=SQL]').click({ force: true });
    cy.wait(delay);
    cy.get('textarea.ace_text-input').eq(0).focus().type('{selectall}{backspace}', { force: true });
    cy.wait(delay);
  });

  testQueries.map(({ title, query, cell_idx, expected_string }) => {
    it(title, () => {
      cy.get('textarea.ace_text-input')
        .eq(0)
        .focus()
        .type(`{selectall}{backspace}${query}`, { force: true });
      cy.wait(delay);
      cy.get('.euiButton__text').contains('Run').click({ force: true });
      cy.wait(delay);

      cy.get('span.euiTableCellContent__text')
        .eq(cell_idx)
        .should((cell) => {
          expect(cell).to.contain(expected_string);
        });
    });
  });

  // skip until nested support is added
  it.skip('Test nested fields display', () => {
    cy.get('textarea.ace_text-input')
      .eq(0)
      .focus()
      .type(`{selectall}{backspace}select * from employee_nested;`, { force: true });
    cy.wait(delay);
    cy.get('.euiButton__text').contains('Run').click({ force: true });
    cy.wait(delay);

    cy.get('button.euiLink').eq(2).click({ force: true });
    cy.wait(delay);
    cy.get('span.euiTableCellContent__text')
      .eq(24)
      .should((cell) => {
        expect(cell).to.contain('2018-06-23');
      });
  });
});
