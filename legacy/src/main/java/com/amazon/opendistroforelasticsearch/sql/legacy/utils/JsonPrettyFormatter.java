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

package com.amazon.opendistroforelasticsearch.sql.legacy.utils;

import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;

/**
 * Utility Class for formatting Json string pretty.
 */
public class JsonPrettyFormatter {

    /**
     * @param jsonString Json string without/with pretty format
     * @return A standard and pretty formatted json string
     * @throws IOException
     */
    public static String format(String jsonString) throws IOException {
        //turn _explain response into pretty formatted Json
        XContentBuilder contentBuilder = XContentFactory.jsonBuilder().prettyPrint();
        try (
                XContentParser contentParser = XContentFactory.xContent(XContentType.JSON)
                        .createParser(NamedXContentRegistry.EMPTY, LoggingDeprecationHandler.INSTANCE, jsonString)
        ){
            contentBuilder.copyCurrentStructure(contentParser);
        }
        return Strings.toString(contentBuilder);
    }

    private JsonPrettyFormatter() {
        throw new AssertionError(getClass().getCanonicalName() + " is a utility class and must not be initialized");
    }
}
