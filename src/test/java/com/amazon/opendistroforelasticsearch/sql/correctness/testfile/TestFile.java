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

package com.amazon.opendistroforelasticsearch.sql.correctness.testfile;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Abstraction for test related file reading */
public interface TestFile {

    default String content(String relativePath) {
        try {
            URL url = Resources.getResource("correctness/" + relativePath);
            return Resources.toString(url, Charsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read test file [" + relativePath + "]");
        }
    }

    default List<String> lines(String content) {
        return Arrays.asList(content.split("\\r?\\n"));
    }

    default List<String[]> splitBy(String content, String separator) { //TODO: refactor
        List<String[]> rows = lines(content).stream().
                                             map(line -> line.split(separator)).
                                             collect(Collectors.toList());

        // Separator in quote is escaped
        List<String[]> result = new ArrayList<>();
        for (String[] row : rows) {
            List<String> escaped = new ArrayList<>();
            boolean isQuote = false;
            for (int i = 0; i < row.length; i++) {
                if (row[i].startsWith("\"")) {
                    isQuote = true;
                    escaped.add(row[i]);
                } else {
                    if (isQuote) {
                        escaped.set(escaped.size() - 1, escaped.get(escaped.size() - 1) + row[i]);
                    } else {
                        escaped.add(row[i]);
                    }

                    if (row[i].endsWith("\"")) {
                        isQuote = false;
                    }
                }
            }
            result.add(escaped.toArray(new String[0]));
        }
        return result;
    }
}
