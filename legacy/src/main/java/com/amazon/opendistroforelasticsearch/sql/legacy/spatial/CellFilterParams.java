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

package com.amazon.opendistroforelasticsearch.sql.legacy.spatial;

/**
 * Created by Eliran on 15/8/2015.
 */
public class CellFilterParams {
    private Point geohashPoint;
    private int precision;
    private boolean neighbors;

    public CellFilterParams(Point geohashPoint, int precision, boolean neighbors) {
        this.geohashPoint = geohashPoint;
        this.precision = precision;
        this.neighbors = neighbors;
    }

    public CellFilterParams(Point geohashPoint, int precision) {
        this(geohashPoint, precision, false);
    }

    public Point getGeohashPoint() {
        return geohashPoint;
    }

    public int getPrecision() {
        return precision;
    }

    public boolean isNeighbors() {
        return neighbors;
    }
}

