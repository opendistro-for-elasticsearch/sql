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

package com.amazon.opendistroforelasticsearch.sql.domain;

public class KVValue implements Cloneable {
    public String key;
    public Object value;

    public KVValue(Object value) {
        this.value = value;
    }

    public KVValue(String key, Object value) {
        if (key != null) {
            this.key = key.replace("'", "");
        }
        this.value = value;
    }

    @Override
    public String toString() {
        if (key == null) {
            return value.toString();
        } else {
            return key + "=" + value;
        }
    }
}
