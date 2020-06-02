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

package com.amazon.opendistroforelasticsearch.sql.doctest.admin;

import com.amazon.opendistroforelasticsearch.sql.doctest.core.DocTest;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.annotation.DocTestConfig;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.annotation.Section;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.builder.Example;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.builder.ListItems;
import com.amazon.opendistroforelasticsearch.sql.executor.Format;
import com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import org.elasticsearch.common.settings.Setting;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequestFormat.CURL_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequestFormat.IGNORE_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat.IGNORE_RESPONSE;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat.PRETTY_JSON_RESPONSE;
import static com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings.CURSOR_ENABLED;
import static com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings.CURSOR_FETCH_SIZE;
import static com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings.CURSOR_KEEPALIVE;
import static com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings.QUERY_ANALYSIS_ENABLED;
import static com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings.QUERY_ANALYSIS_SEMANTIC_SUGGESTION;
import static com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings.QUERY_ANALYSIS_SEMANTIC_THRESHOLD;
import static com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings.QUERY_RESPONSE_FORMAT;
import static com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings.QUERY_SLOWLOG;
import static com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings.SQL_ENABLED;
import static org.elasticsearch.common.settings.Setting.Property;
import static org.elasticsearch.common.settings.Setting.Property.Dynamic;
import static org.elasticsearch.common.settings.Setting.Property.Final;
import static org.elasticsearch.common.settings.Setting.Property.IndexScope;
import static org.elasticsearch.common.settings.Setting.Property.NodeScope;
import static org.elasticsearch.common.settings.Settings.EMPTY;

/**
 * Doc test for plugin settings.
 */
@DocTestConfig(template = "admin/settings.rst", testData = {"accounts.json"})
public class PluginSettingIT extends DocTest {

    private static final SqlSettings SETTINGS = new SqlSettings();

    @Section(1)
    public void sqlEnabledSetting() {
        docSetting(
            SQL_ENABLED,
            "You can disable SQL plugin to reject all coming requests.",
            false,
            "SELECT * FROM accounts"
        );
    }

    @Section(2)
    public void slowLogSetting() {
        docSetting(
            QUERY_SLOWLOG,
            "You can configure the time limit (seconds) for slow query which would be logged as " +
            "'Slow query: elapsed=xxx (ms)' in elasticsearch.log.",
            10
        );
    }

    @Section(3)
    public void queryAnalysisEnabledSetting() {
        docSetting(
            QUERY_ANALYSIS_ENABLED,
            "You can disable query analyzer to bypass strict syntactic and semantic analysis.",
            false
        );
    }

    @Section(4)
    public void semanticSuggestionSetting() {
        docSetting(
            QUERY_ANALYSIS_SEMANTIC_SUGGESTION,
            "You can enable query analyzer to suggest correct field names for quick fix.",
            true,
            "SELECT first FROM accounts"
        );
    }

    @Section(5)
    public void semanticAnalysisThresholdSetting() {
        docSetting(
            QUERY_ANALYSIS_SEMANTIC_THRESHOLD,
            "Because query analysis needs to build semantic context in memory, index with large number of field " +
            "would be skipped. You can update it to apply analysis to smaller or larger index as needed.",
            50
        );
    }

    @Section(6)
    public void responseFormatSetting() {
        docSetting(
                QUERY_RESPONSE_FORMAT,
                String.format("User can set default response format of the query. " +
                              "The supported format includes: %s.", Arrays.stream(Format.values())
                                                                     .map(Format::getFormatName)
                                                                     .collect(Collectors.joining(","))),
                Format.JSON.getFormatName(),
                "SELECT firstname, lastname, age FROM accounts ORDER BY age LIMIT 2"
        );
    }

    @Section(7)
    public void cursorEnabledSetting() {
        docSetting(
                CURSOR_ENABLED,
                "User can enable/disable pagination for all queries that are supported.",
                true
        );
    }

    @Section(8)
    public void cursorDefaultFetchSizeSetting() {
        docSetting(
                CURSOR_FETCH_SIZE,
                "User can set the default fetch_size for all queries that are supported by pagination. " +
                "Explicit `fetch_size` passed in request will override this value",
                50
        );
    }

    @Section(9)
    public void cursorDefaultContextKeepAliveSetting() {
        docSetting(
                CURSOR_KEEPALIVE,
                "User can set this value to indicate how long the cursor context should be kept open. " +
                "Cursor contexts are resource heavy, and a lower value should be used if possible.",
                "5m"
        );
    }

    /**
     * Generate content for sample queries with setting changed to new value.
     * Finally setting will be reverted to avoid potential impact on other test cases.
     */
    private void docSetting(String name, String description, Object sampleValue, String... sampleQueries) {
        try {
            section(
                title(name),
                description(description + "\n\n" + listSettingDetails(name)),
                createExamplesForSettingChangeQueryAndOtherSampleQueries(name, sampleValue, sampleQueries)
            );
        } finally {
            // Make sure the change is removed
            try {
                put(name, null).queryResponse();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private Example[] createExamplesForSettingChangeQueryAndOtherSampleQueries(String name,
                                                                               Object sampleValue,
                                                                               String[] sampleQueries) {
        Example[] examples = new Example[sampleQueries.length + 1];
        examples[0] = example("You can update the setting with a new value like this.",
                              put(name, sampleValue),
                              queryFormat(CURL_REQUEST, PRETTY_JSON_RESPONSE),
                              explainFormat(IGNORE_REQUEST, IGNORE_RESPONSE));

        for (int i = 0; i < sampleQueries.length; i++) {
            examples[i + 1] = example("Query result after the setting updated is like:",
                                      post(sampleQueries[i]),
                                      queryFormat(CURL_REQUEST, PRETTY_JSON_RESPONSE),
                                      explainFormat(IGNORE_REQUEST, IGNORE_RESPONSE));
        }
        return examples;
    }

    private String listSettingDetails(String name) {
        Setting<?> setting = SETTINGS.getSetting(name);
        ListItems list = new ListItems();

        list.addItem(StringUtils.format("The default value is %s.", setting.getDefault(EMPTY)));

        EnumSet<Property> properties = setting.getProperties();
        if (properties.contains(NodeScope)) {
            list.addItem("This setting is node scope.");
        } else if (properties.contains(IndexScope)) {
            list.addItem("This setting is index scope.");
        }

        if (properties.contains(Dynamic)) {
            list.addItem("This setting can be updated dynamically.");
        } else if (properties.contains(Final)) {
            list.addItem("This setting is not updatable.");
        }
        return list.toString();
    }

}
