/*
 * Copyright <2019> Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.jdbc.internal;

public enum Version {

    // keep this in sync with the gradle version
    Current(1, 10, 0, 0);

    private int major;
    private int minor;
    private int build;
    private int revision;

    private String fullVersion;

    Version(int major, int minor, int build, int revision) {
        this.major = major;
        this.minor = minor;
        this.build = build;
        this.revision = revision;
        this.fullVersion = String.format("%d.%d.%d.%d", major, minor, build, revision);
    }

    public int getMajor() {
        return this.major;
    }

    public int getMinor() {
        return this.minor;
    }

    public int getBuild() {
        return this.build;
    }

    public int getRevision() {
        return this.revision;
    }

    public String getFullVersion() {
        return this.fullVersion;
    }
}
