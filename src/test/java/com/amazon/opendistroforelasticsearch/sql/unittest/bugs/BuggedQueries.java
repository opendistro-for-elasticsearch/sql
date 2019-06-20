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

package com.amazon.opendistroforelasticsearch.sql.unittest.bugs;

import com.alibaba.druid.sql.parser.ParserException;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.query.ESActionFactory;
import org.elasticsearch.client.Client;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.SQLFeatureNotSupportedException;

/**
 * Special cases for queries that had broken our parser in past.
 */
public class BuggedQueries {

    @Test(expected = ParserException.class)
    public void missingWhereAndFieldName() throws SQLFeatureNotSupportedException, SqlParseException {
        ESActionFactory.create(Mockito.mock(Client.class), "select * from products like 'SomeProduct*' limit 10");
    }
}
