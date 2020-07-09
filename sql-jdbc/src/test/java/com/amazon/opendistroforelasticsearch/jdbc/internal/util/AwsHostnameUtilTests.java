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

package com.amazon.opendistroforelasticsearch.jdbc.internal.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


public class AwsHostnameUtilTests {

    /**
     * Test region name extracted from input hostname is as expected
     * when the input hostname is a known url format.
     *
     * @param hostname hostname to parse
     * @param expectedRegion expected region value
     */
    @ParameterizedTest
    @CsvSource({
            "search-domain-name.us-east-1.es.amazonaws.com, us-east-1",
            "search-domain-name.us-gov-west-1.es.amazonaws.com, us-gov-west-1",
            "search-domain-name.ap-southeast-2.es.a9.com, ap-southeast-2",
            "search-domain-name.sub-domain.us-west-2.es.amazonaws.com, us-west-2",
            "search-us-east-1.us-west-2.es.amazonaws.com, us-west-2",
    })
    void testNonNullRegionsFromAwsHostnames(String hostname, String expectedRegion) {
        assertEquals(expectedRegion, AwsHostNameUtil.parseRegion(hostname));
    }

    /**
     * Verify that a region value is not extracted from an input hostname
     *
     * @param hostname hostname to parse
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "search-domain-name.us-east-1.es.amazonaws.co",
            "search-domain-name.us-gov-west-1.es.amazonaws",
            "search-domain-name.ap-southeast-2.es.com",
    })
    void testNullRegions(String hostname) {
        String region = AwsHostNameUtil.parseRegion(hostname);
        assertNull(region, () -> hostname + " returned non-null region: " + region);
    }

}
