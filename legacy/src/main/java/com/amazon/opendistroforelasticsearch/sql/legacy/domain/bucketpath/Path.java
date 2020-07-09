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

package com.amazon.opendistroforelasticsearch.sql.legacy.domain.bucketpath;

public class Path {
    private final String path;
    private final String separator;
    private final PathType type;

    private Path(String path, String separator, PathType type) {
        this.path = path;
        this.separator = separator;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public String getSeparator() {
        return separator;
    }

    public PathType getType() {
        return type;
    }

    public boolean isMetricPath() {
        return type == PathType.METRIC;
    }

    public boolean isAggPath() {
        return type == PathType.AGG;
    }

    public static Path getAggPath(String path) {
        return new Path(path, ">", PathType.AGG);
    }

    public static Path getMetricPath(String path) {
        return new Path(path, ".", PathType.METRIC);
    }

    public enum PathType {
        AGG, METRIC
    }
}
