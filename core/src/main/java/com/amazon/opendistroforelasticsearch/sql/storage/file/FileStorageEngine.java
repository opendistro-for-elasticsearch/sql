/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.storage.file;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.storage.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import com.amazon.opendistroforelasticsearch.sql.storage.Table;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileStorageEngine implements StorageEngine {

    private final String databaseName;

    private final Map<String, Table> tableByNames = new HashMap<>();

    public FileStorageEngine(String content/*String dbFilePath*/) {
        //try {
            //String content = new String(Files.readAllBytes(Paths.get(dbFilePath)));
            JSONObject json = new JSONObject(content);

            // Bypass file format check
            Map<String, Object> dbJson = json.toMap();
            databaseName = dbJson.entrySet().iterator().next().getKey();
            ((Map<String, Object>) dbJson.get(databaseName)).forEach(
                (tableName, rowsJson) -> {
                    List<BindingTuple> rows = ((List<Object>) rowsJson).stream().
                                                                     map(rowJson -> ((Map<String, Object>) rowJson)).
                                                                     map(BindingTuple::from).
                                                                     collect(Collectors.toList());
                    tableByNames.put(tableName, new FileTable(rows));
                }
            );
        //} catch (IOException e) {
        //    throw new IllegalArgumentException("Failed to read database file: " + dbFilePath, e);
        //}
    }

    @Override
    public ExprType getType(String name) {
        return null;
    }

    @Override
    public Table getTable(String name) {
        return tableByNames.getOrDefault(name, Table.NONE);
    }

    private String getResourceFilePath(String relPath) {
        String projectRoot = System.getProperty("project.root", null);
        if (projectRoot == null) {
            return new File(relPath).getAbsolutePath();
        } else {
            return new File(projectRoot + "/" + relPath).getAbsolutePath();
        }
    }

}
