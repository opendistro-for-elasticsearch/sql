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
 * The Path connection property.
 *
 * A trailing '/' is not expected or required in the
 * input value but is ignored if present.
 *
 */
public class PathConnectionProperty extends StringConnectionProperty {

    public static final String KEY = "path";

    public PathConnectionProperty() {
        super(KEY);
    }

    @Override
    protected String parseValue(Object value) throws ConnectionPropertyException {
        String stringValue = super.parseValue(value);

        // Remove the trailing '/' as all internal calls
        // will implicitly apply this.
        if (stringValue.length() > 1 && stringValue.endsWith("/")) {
            stringValue = stringValue.substring(0, stringValue.length()-1);
        }
        return stringValue;
    }

    @Override
    public String getDefault() {
        return "";
    }
}
