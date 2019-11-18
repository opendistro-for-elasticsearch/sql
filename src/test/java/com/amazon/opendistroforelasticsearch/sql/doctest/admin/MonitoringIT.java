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
import com.amazon.opendistroforelasticsearch.sql.doctest.core.markup.DataTable;
import com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequest;
import com.amazon.opendistroforelasticsearch.sql.metrics.MetricName;

import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequestFormat.CURL_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.request.SqlRequestFormat.NO_REQUEST;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat.NO_RESPONSE;
import static com.amazon.opendistroforelasticsearch.sql.doctest.core.response.SqlResponseFormat.PRETTY_JSON_RESPONSE;
import static com.amazon.opendistroforelasticsearch.sql.metrics.MetricName.FAILED_REQ_COUNT_CB;
import static com.amazon.opendistroforelasticsearch.sql.metrics.MetricName.FAILED_REQ_COUNT_CUS;
import static com.amazon.opendistroforelasticsearch.sql.metrics.MetricName.FAILED_REQ_COUNT_SYS;
import static com.amazon.opendistroforelasticsearch.sql.metrics.MetricName.REQ_COUNT_TOTAL;
import static com.amazon.opendistroforelasticsearch.sql.metrics.MetricName.REQ_TOTAL;
import static com.amazon.opendistroforelasticsearch.sql.plugin.RestSqlStatsAction.STATS_API_ENDPOINT;

/**
 * Doc test for plugin monitoring functionality
 */
@DocTestConfig(template = "admin/monitoring.rst")
public class MonitoringIT extends DocTest {

    @Section
    public void nodeStats() {
        section(
            title("Node Stats"),
            description("The meaning of fields in the response is as follows:\n\n" + fieldDescriptions()),
            example(
                description(),
                getStats(),
                queryFormat(CURL_REQUEST, PRETTY_JSON_RESPONSE),
                explainFormat(NO_REQUEST, NO_RESPONSE)
            )
        );
    }

    private String fieldDescriptions() {
        DataTable table = new DataTable(new String[]{ "Field name", "Description" });
        table.addRow(row(REQ_TOTAL, "Total count of request"));
        table.addRow(row(REQ_COUNT_TOTAL, "Total count of request within last window"));
        table.addRow(row(FAILED_REQ_COUNT_SYS, "Count of failed request due to system error"));
        table.addRow(row(FAILED_REQ_COUNT_CUS, "Count of failed request due to bad request"));
        table.addRow(row(FAILED_REQ_COUNT_CB, "Is plugin being circuit broken or not"));
        return table.toString();
    }

    private String[] row(MetricName name, String description) {
        return new String[] { name.getName(), description };
    }

    private SqlRequest[] getStats() {
        return new SqlRequest[] {
            new SqlRequest("GET", STATS_API_ENDPOINT, "")
        };
    }

}
