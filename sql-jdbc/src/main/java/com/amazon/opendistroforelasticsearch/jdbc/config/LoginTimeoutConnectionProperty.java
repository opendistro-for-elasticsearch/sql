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

/**
 * Login / Read timeout in seconds
 */
public class LoginTimeoutConnectionProperty extends IntConnectionProperty {

    public static final String KEY = "loginTimeout";

    public LoginTimeoutConnectionProperty() {
        super(KEY);
    }

    @Override
    protected Integer parseValue(Object value) throws ConnectionPropertyException {
        int intValue = super.parseValue(value);

        if (intValue < 0) {
            throw new ConnectionPropertyException(getKey(),
                    String.format("Login timeout property requires a valid integer >=0. Invalid value: %d", intValue));
        }
        return intValue;
    }

}
