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

package com.amazon.opendistroforelasticsearch.sql.doctest.core.test;

import com.amazon.opendistroforelasticsearch.sql.doctest.core.markup.RstDocument;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test cases for {@link RstDocument}
 */
public class RstDocumentTest {

    private ByteArrayOutputStream content;

    private RstDocument document;

    @Before
    public void setUp() {
        content = new ByteArrayOutputStream();
        document = new RstDocument(new PrintWriter(content, true)); // Enable auto flush
    }

    @Test
    public void testSection() {
        document.section("Test Section");
        assertThat(
            content.toString(),
            is(
                "Test Section\n" +
                "============\n" +
                "\n"
            )
        );
    }

    @Test
    public void testSubSection() {
        document.subSection("Test Sub Section");
        assertThat(
            content.toString(),
            is(
                "Test Sub Section\n" +
                "----------------\n" +
                "\n"
            )
        );
    }

    @Test
    public void testParagraph() {
        document.paragraph("Test paragraph");
        assertThat(
            content.toString(),
            is(
                "Test paragraph\n" +
                "\n"
            )
        );
    }

    @Test
    public void testCodeBlock() {
        document.codeBlock("Test code", ">> curl localhost:9200");
        assertThat(
            content.toString(),
            is(
                "Test code::\n" +
                "\n" +
                "\t>> curl localhost:9200\n" +
                "\n"
            )
        );
    }

    @Test
    public void testTable() {
        document.table(
            "Test table",
            "+----------+\n" +
            "|Test Table|\n" +
            "+==========+\n" +
            "| test data|\n" +
            "+----------+"
        );

        assertThat(
            content.toString(),
            is(
                "Test table:\n" +
                "\n" +
                "+----------+\n" +
                "|Test Table|\n" +
                "+==========+\n" +
                "| test data|\n" +
                "+----------+\n" +
                "\n"
            )
        );
    }

    @Test
    public void testImage() {
        document.image("Query syntax", "/docs/user/img/query_syntax.png");

        assertThat(
            content.toString(),
            is(
                "Query syntax:\n" +
                "\n" +
                ".. image:: /docs/user/img/query_syntax.png\n" +
                "\n"
            )
        );
    }

}
