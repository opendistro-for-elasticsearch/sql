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

import com.amazon.opendistroforelasticsearch.sql.data.model.AbstractExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import java.util.Objects;
import lombok.Data;

/**
 * Elasticsearch GeoPointValue.
 * Todo, add this to avoid the unknown value type exception, the implementation will be changed.
 */
public class ElasticsearchExprGeoPointValue extends AbstractExprValue {

  private final GeoPoint geoPoint;

  public ElasticsearchExprGeoPointValue(Double lat, Double lon) {
    this.geoPoint = new GeoPoint(lat, lon);
  }

  @Override
  public Object value() {
    return geoPoint;
  }

  @Override
  public ExprType type() {
    return ES_GEO_POINT;
  }

  @Override
  public int compare(ExprValue other) {
    return geoPoint.toString()
        .compareTo((((ElasticsearchExprGeoPointValue) other).geoPoint).toString());
  }

  @Override
  public boolean equal(ExprValue other) {
    return geoPoint.equals(((ElasticsearchExprGeoPointValue) other).geoPoint);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(geoPoint);
  }

  @Data
  public static class GeoPoint {

    private final Double lat;

    private final Double lon;
  }
}
