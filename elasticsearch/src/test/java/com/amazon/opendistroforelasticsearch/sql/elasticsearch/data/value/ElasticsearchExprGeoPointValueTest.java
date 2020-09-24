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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value;

import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType.ES_GEO_POINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ElasticsearchExprGeoPointValueTest {

  private ElasticsearchExprGeoPointValue geoPointValue = new ElasticsearchExprGeoPointValue(1.0,
      1.0);

  @Test
  void value() {
    assertEquals(new ElasticsearchExprGeoPointValue.GeoPoint(1.0, 1.0), geoPointValue.value());
  }

  @Test
  void type() {
    assertEquals(ES_GEO_POINT, geoPointValue.type());
  }

  @Test
  void compare() {
    assertEquals(0, geoPointValue.compareTo(new ElasticsearchExprGeoPointValue(1.0, 1.0)));
  }

  @Test
  void equal() {
    assertTrue(geoPointValue.equal(new ElasticsearchExprGeoPointValue(1.0,
        1.0)));
  }

  @Test
  void testHashCode() {
    assertNotNull(geoPointValue.hashCode());
  }
}