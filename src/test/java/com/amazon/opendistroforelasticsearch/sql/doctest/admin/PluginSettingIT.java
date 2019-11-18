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
import com.amazon.opendistroforelasticsearch.sql.doctest.core.dsl.DocTestConfig;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.dsl.Section;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.markup.Document;
import com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import org.elasticsearch.common.settings.Setting;

import java.util.EnumSet;

import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequestFormat.CURL_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequestFormat.NO_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat.NO_RESPONSE;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat.PRETTY_JSON_RESPONSE;
import static com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings.QUERY_ANALYSIS_ENABLED;
import static com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings.QUERY_ANALYSIS_SEMANTIC_SUGGESTION;
import static com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings.QUERY_ANALYSIS_SEMANTIC_THRESHOLD;
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

    private static final SqlSettings settings = new SqlSettings();

    @Section(value = 1)
    public void sqlEnabledSetting() {
        docSetting(
            SQL_ENABLED,
            "You can disable SQL plugin to reject all coming requests.",
            false,
            "SELECT * FROM accounts"
        );
    }

    @Section(value = 2)
    public void slowLogSetting() {
        docSetting(
            QUERY_SLOWLOG,
            "You can configure the time limit (seconds) for slow query which would be logged as " +
            "'Slow query: elapsed=xxx (ms)'.",
            10
        );
    }

    @Section(value = 3)
    public void queryAnalysisEnabledSetting() {
        docSetting(
            QUERY_ANALYSIS_ENABLED,
            "You can disable query analyzer to bypass strict syntactic and semantic analysis.",
            false
        );
    }

    @Section(value = 4)
    public void semanticSuggestionSetting() {
        docSetting(
            QUERY_ANALYSIS_SEMANTIC_SUGGESTION,
            "You can enable query analyzer to suggest correct field names for quick fix.",
            true,
            "SELECT first FROM accounts"
        );
    }

    @Section(value = 5)
    public void semanticAnalysisThresholdSetting() {
        docSetting(
            QUERY_ANALYSIS_SEMANTIC_THRESHOLD,
            "Because query analysis needs to build semantic context in memory, index with large number of field " +
            "would be skipped. You can update it to apply analysis to smaller or larger index as you wish.",
            50
        );
    }

    private void docSetting(String name, String description, Object sampleValue, String... sampleQueries) {
        Document.Example[] examples = new Document.Example[sampleQueries.length + 1];
        examples[0] = example("You can update the setting with a new value like this.",
                              put(name, sampleValue),
                              queryFormat(CURL_REQUEST, PRETTY_JSON_RESPONSE),
                              explainFormat(NO_REQUEST, NO_RESPONSE));

        for (int i = 0; i < sampleQueries.length; i++) {
            examples[i + 1] = example("Query result after the setting updated is like:",
                                      query(sampleQueries[i]),
                                      queryFormat(CURL_REQUEST, PRETTY_JSON_RESPONSE),
                                      explainFormat(NO_REQUEST, NO_RESPONSE));
        }

        section(
            title(name),
            description(description + "\n\n" + listSettingDetails(name)),
            examples
        );

        put(name, null)[0].send(restClient());
    }

    private String listSettingDetails(String name) {
        Setting<?> setting = settings.getSetting(name);
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
