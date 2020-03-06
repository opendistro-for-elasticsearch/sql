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

package com.amazon.opendistroforelasticsearch.ppl.plugin;

import com.amazon.opendistroforelasticsearch.sql.executor.Format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.elasticsearch.common.settings.Setting;

import static java.util.Collections.unmodifiableMap;
import static org.elasticsearch.common.settings.Setting.Property.Dynamic;
import static org.elasticsearch.common.settings.Setting.Property.NodeScope;


public class PPLSettings implements PluginSettings{

    private Map<String, Setting<?>> settings;

    public static final String PPL_ENABLED = "opendistro.ppl.enabled";
    public static final String PPL_QUERY_SLOWLOG = "opendistro.ppl.query.slowlog";
    public static final String PPL_QUERY_RESPONSE_FORMAT = "opendistro.ppl.query.response.format";
    public static final String PPL_METRICS_ROLLINGWINDOW = "opendistro.ppl.metrics.rollingwindow";
    public static final String PPL_METRICS_ROLLINGINTERVAL = "opendistro.ppl.metrics.rollinginterval";

    public PPLSettings() {

        Map<String, Setting<?>> settings = new HashMap<>();
        settings.put(PPL_ENABLED, Setting.boolSetting(PPL_ENABLED, true, NodeScope, Dynamic));
        settings.put(PPL_QUERY_SLOWLOG, Setting.intSetting(PPL_QUERY_SLOWLOG, 2, NodeScope, Dynamic));
        settings.put(PPL_QUERY_RESPONSE_FORMAT, Setting.simpleString(PPL_QUERY_RESPONSE_FORMAT, Format.JDBC.getFormatName(),
                                                                 NodeScope, Dynamic));

        settings.put(PPL_METRICS_ROLLINGWINDOW, Setting.longSetting(PPL_METRICS_ROLLINGWINDOW, 3600L, 2L,
                NodeScope, Dynamic));
        settings.put(PPL_METRICS_ROLLINGINTERVAL, Setting.longSetting(PPL_METRICS_ROLLINGINTERVAL, 60L, 1L,
                NodeScope, Dynamic));

        this.settings = unmodifiableMap(settings);
    }

    public Setting<?> getSetting(String key) {
        if (settings.containsKey(key)) {
            return settings.get(key);
        }
        throw new IllegalArgumentException("Cannot find setting by key [" + key + "]");
    }

    public List<Setting<?>> getSettings() {
        return new ArrayList<>(settings.values());
    }
}
