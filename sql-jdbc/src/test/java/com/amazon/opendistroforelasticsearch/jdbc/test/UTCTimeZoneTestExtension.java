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

package com.amazon.opendistroforelasticsearch.jdbc.test;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.TimeZone;

public class UTCTimeZoneTestExtension implements BeforeEachCallback, AfterEachCallback {

    TimeZone jvmDefaultTimeZone;

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        // restore JVM default timezone
        TimeZone.setDefault(jvmDefaultTimeZone);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        jvmDefaultTimeZone = TimeZone.getDefault();

        // test case inputs assume default TZ is UTC
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
