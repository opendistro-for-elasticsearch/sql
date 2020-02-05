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

package com.amazon.opendistroforelasticsearch.sql.doctest.core.builder;

/**
 * Example value object.
 */
public class Example {

    /** Title for the example section */
    private String title;

    /** Description for the example */
    private String description;

    /** Sample SQL query */
    private String query;

    /** Query result set */
    private String result;

    /** Is result set formatted in table (markup handle table in different way */
    private boolean isTable;

    /** Explain query correspondent to the sample query */
    private String explainQuery;

    /** Result of explain */
    private String explainResult;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getQuery() {
        return query;
    }

    public String getResult() {
        return result;
    }

    public boolean isTable() {
        return isTable;
    }

    public String getExplainQuery() {
        return explainQuery;
    }

    public String getExplainResult() {
        return explainResult;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public void setTable(boolean table) {
        isTable = table;
    }

    public void setExplainQuery(String explainQuery) {
        this.explainQuery = explainQuery;
    }

    public void setExplainResult(String explainResult) {
        this.explainResult = explainResult;
    }
}
