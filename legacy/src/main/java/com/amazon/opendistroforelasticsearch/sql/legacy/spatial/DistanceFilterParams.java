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
 * Created by Eliran on 1/8/2015.
 */
public class DistanceFilterParams {
    private String distance;
    private Point from;

    public DistanceFilterParams(String distance, Point from) {
        this.distance = distance;
        this.from = from;
    }

    public String getDistance() {
        return distance;
    }

    public Point getFrom() {
        return from;
    }
}
