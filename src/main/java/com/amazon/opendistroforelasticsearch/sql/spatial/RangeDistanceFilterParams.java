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

package com.amazon.opendistroforelasticsearch.sql.spatial;

/**
 * Created by Eliran on 15/8/2015.
 */
public class RangeDistanceFilterParams extends DistanceFilterParams {
    private String distanceTo;

    public RangeDistanceFilterParams(String distanceFrom, String distanceTo, Point from) {
        super(distanceFrom, from);
        this.distanceTo = distanceTo;
    }

    public String getDistanceTo() {
        return distanceTo;
    }

    public String getDistanceFrom() {
        return this.getDistance();
    }
}
