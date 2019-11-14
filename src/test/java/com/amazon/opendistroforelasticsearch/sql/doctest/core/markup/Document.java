/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.doctest.core.markup;

import com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils;

import java.io.Closeable;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Document for different format and markup
 */
public interface Document extends Closeable {

    String DOCUMENT_FOLDER_ROOT = "docs/user/";

    Document section(String title);

    Document subSection(String title);

    Document paragraph(String text);

    Document codeBlock(String description, String code);

    Document table(String description, DataTable table);

    static Path path(String templateRelativePath) {
        return Paths.get(TestUtils.getResourceFilePath(DOCUMENT_FOLDER_ROOT + templateRelativePath));
    }

    class Section {
        private String title;
        private String description;
        private Example[] examples;

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public Example[] getExamples() {
            return examples;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setExamples(Example[] examples) {
            this.examples = examples;
        }
    }

    class Example {
        private String description;
        private String query;
        private String result;
        private String explainQuery;
        private String explainResult;

        public String getDescription() {
            return description;
        }

        public String getQuery() {
            return query;
        }

        public String getResultSet() {
            return result;
        }

        public String getExplainQuery() {
            return explainQuery;
        }

        public String getExplainResult() {
            return explainResult;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public void setExplainQuery(String explainQuery) {
            this.explainQuery = explainQuery;
        }

        public void setExplainResult(String explainResult) {
            this.explainResult = explainResult;
        }
    }

}
