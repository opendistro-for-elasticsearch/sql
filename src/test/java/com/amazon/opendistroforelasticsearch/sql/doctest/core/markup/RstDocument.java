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

package com.amazon.opendistroforelasticsearch.sql.doctest.core.markup;

import com.google.common.base.Strings;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.APPEND;

/**
 * ReStructure Text
 */
public class RstDocument implements Document {

    private final PrintWriter docWriter;

    public RstDocument(Path documentPath) {
        try {
            docWriter = new PrintWriter(Files.newBufferedWriter(documentPath, APPEND));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to open document file " + documentPath, e);
        }
    }

    @Override
    public Document section(String title) {
        return print(
            title,
            Strings.repeat("=", title.length())
        );
    }

    @Override
    public Document subSection(String title) {
        return print(
            title,
            Strings.repeat("-", title.length())
        );
    }

    @Override
    public Document paragraph(String text) {
        return println(text);
    }

    @Override
    public Document codeBlock(String description, String code) {
        if (Strings.isNullOrEmpty(code)) {
            return this;
        }

        return println(
            description + "::",
            indent(code)
        );
    }

    @Override
    public Document table(String description, DataTable table) {
        docWriter.println(description + "::");
        if (table.toString().startsWith("+---")) {
            docWriter.println(table);
        } else {
            docWriter.println(indent(table.toString()));
        }
        docWriter.println();
        return this;
    }

    @Override
    public void close() {
        docWriter.close();
    }

    /** Print each line with a blank line at last */
    private Document print(String... lines) {
        for (String line : lines) {
            docWriter.println(line);
        }
        docWriter.println();
        return this;
    }

    /** Print each line with a blank line followed */
    private Document println(String... lines) {
        for (String line : lines) {
            docWriter.println(line);
            docWriter.println();
        }
        return this;
    }

    private String indent(String text) {
        return "\t" + text.replaceAll("\\n", "\n\t");
    }

}
