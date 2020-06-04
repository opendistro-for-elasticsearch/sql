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

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr;

/**
 * Configuration for SQL analysis.
 */
public class SqlAnalysisConfig {

    /** Is entire analyzer enabled to perform the analysis */
    private final boolean isAnalyzerEnabled;

    /** Is suggestion enabled for field name typo */
    private final boolean isFieldSuggestionEnabled;

    /** Skip entire analysis for index mapping larger than this threhold */
    private final int analysisThreshold;

    public SqlAnalysisConfig(boolean isAnalyzerEnabled,
                             boolean isFieldSuggestionEnabled,
                             int analysisThreshold) {
        this.isAnalyzerEnabled = isAnalyzerEnabled;
        this.isFieldSuggestionEnabled = isFieldSuggestionEnabled;
        this.analysisThreshold = analysisThreshold;
    }

    public boolean isAnalyzerEnabled() {
        return isAnalyzerEnabled;
    }

    public boolean isFieldSuggestionEnabled() {
        return isFieldSuggestionEnabled;
    }

    public int getAnalysisThreshold() {
        return analysisThreshold;
    }

    @Override
    public String toString() {
        return "SqlAnalysisConfig{"
            + "isAnalyzerEnabled=" + isAnalyzerEnabled
            + ", isFieldSuggestionEnabled=" + isFieldSuggestionEnabled
            + ", analysisThreshold=" + analysisThreshold
            + '}';
    }
}
