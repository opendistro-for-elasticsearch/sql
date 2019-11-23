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

package com.amazon.opendistroforelasticsearch.sql.doctest.core;

import com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Abstraction for document template file
 */
public class Template {

    private static final String TEMPLATE_FOLDER_ROOT = "src/test/resources/doctest/templates/";

    private final Path templateFullPath;

    public Template(String templateRelativePath) {
        this.templateFullPath = Paths.get(TestUtils.getResourceFilePath(TEMPLATE_FOLDER_ROOT + templateRelativePath));
    }

    /**
     * Copy template file to target document. Replace it if existing.
     * @param docFullPath   full path of target document
     */
    public void copyToDocument(Path docFullPath) {
        try {
            Files.createDirectories(docFullPath.getParent());
            Files.copy(templateFullPath, docFullPath, REPLACE_EXISTING, COPY_ATTRIBUTES);
        } catch (IOException e) {
            throw new IllegalStateException(StringUtils.format(
                "Failed to copy from template [%s] to document file [%s]", templateFullPath, docFullPath), e);
        }
    }

}
