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

package com.amazon.opendistroforelasticsearch.jdbc.config;

public class PortConnectionProperty extends IntConnectionProperty {

    public static final String KEY = "port";

    public PortConnectionProperty() {
        super(KEY);
    }

    @Override
    protected Integer parseValue(Object value) throws ConnectionPropertyException {
        int intValue = super.parseValue(value);

        if (intValue < 0 || intValue > 65535) {
            throw new ConnectionPropertyException(getKey(),
                    String.format("Port number property requires a valid integer (0-65535). Invalid value: %d", intValue));
        }

        return intValue;
    }

    @Override
    public Integer getDefault() {
        return 9200;
    }
}
