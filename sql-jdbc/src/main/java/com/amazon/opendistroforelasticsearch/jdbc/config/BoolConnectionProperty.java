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

public class BoolConnectionProperty extends ConnectionProperty<Boolean> {

    public BoolConnectionProperty(String key) {
        super(key);
    }

    @Override
    protected Boolean parseValue(Object value) throws ConnectionPropertyException {

        if (value == null) {
            return getDefault();
        } else if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }

        throw new ConnectionPropertyException(getKey(),
                String.format("Property %s requires a valid boolean. Invalid property value of type %s. ",
                        getKey(), value.getClass().getName()));
    }

    @Override
    public Boolean getDefault() {
        return false;
    }
}
