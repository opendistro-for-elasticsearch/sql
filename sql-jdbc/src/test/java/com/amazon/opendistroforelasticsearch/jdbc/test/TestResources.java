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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class TestResources {

    public static String readResourceAsString(String resourcePath) throws IOException {
        InputStream is = getResourceAsStream(resourcePath);

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    public static InputStream getResourceAsStream(String resourcePath) throws IOException {
        InputStream is = TestResources.class.getClassLoader().getResourceAsStream(resourcePath);

        if (is == null) {
            throw new TestResourcesException("Resource with path: " + resourcePath + " not found!");
        }

        return is;
    }

    public static void copyResourceToPath(String resourcePath, Path path) throws IOException {
        InputStream is = getResourceAsStream(resourcePath);

        Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
    }

    public static class TestResourcesException extends RuntimeException {

        public TestResourcesException() {
        }

        public TestResourcesException(String message) {
            super(message);
        }
    }
}
