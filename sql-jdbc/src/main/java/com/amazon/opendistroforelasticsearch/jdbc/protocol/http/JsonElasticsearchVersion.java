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

package com.amazon.opendistroforelasticsearch.jdbc.protocol.http;

import com.amazon.opendistroforelasticsearch.jdbc.ElasticsearchVersion;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonElasticsearchVersion implements ElasticsearchVersion {

    private String fullVersion;
    private int[] version = new int[3];

    public JsonElasticsearchVersion(@JsonProperty("number") String fullVersion) {
        if (fullVersion == null)
            return;

        this.fullVersion = fullVersion;
        String[] versionTokens = fullVersion.split("[.-]");

        for (int i=0; i<versionTokens.length && i < 3; i++) {
            this.version[i] = parseNumber(versionTokens[i]);
        }
    }

    private int parseNumber(String str) {
        int number = 0;
        try {
            number = Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            // eat
        }
        return number;
    }

    @Override
    public int getMajor() {
        return version[0];
    }

    @Override
    public int getMinor() {
        return version[1];
    }

    @Override
    public int getRevision() {
        return version[2];
    }

    @Override
    public String getFullVersion() {
        return fullVersion;
    }
}
