/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.setting;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.amazon.opendistroforelasticsearch.sql.common.setting.Settings;
import java.util.List;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElasticsearchSettingsTest {

  @Mock
  private ClusterSettings clusterSettings;

  @Test
  void getSettingValue() {
    ElasticsearchSettings settings = new ElasticsearchSettings(clusterSettings);
    ByteSizeValue sizeValue = settings.getSettingValue(Settings.Key.PPL_QUERY_MEMORY_LIMIT);

    assertNotNull(sizeValue);
  }

  @Test
  void pluginSettings() {
    List<Setting<?>> settings = ElasticsearchSettings.pluginSettings();

    assertFalse(settings.isEmpty());
  }

  @Test
  void update() {
    ElasticsearchSettings settings = new ElasticsearchSettings(clusterSettings);
    ByteSizeValue oldValue = settings.getSettingValue(Settings.Key.PPL_QUERY_MEMORY_LIMIT);
    ElasticsearchSettings.Updater updater =
        settings.new Updater(Settings.Key.PPL_QUERY_MEMORY_LIMIT);
    updater.accept(new ByteSizeValue(0L));

    ByteSizeValue newValue = settings.getSettingValue(Settings.Key.PPL_QUERY_MEMORY_LIMIT);

    assertNotEquals(newValue.getBytes(), oldValue.getBytes());
  }
}