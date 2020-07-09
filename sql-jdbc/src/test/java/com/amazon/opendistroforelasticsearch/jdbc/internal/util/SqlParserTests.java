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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SqlParserTests {

    @ParameterizedTest
    @MethodSource("pameterizedValidSqlProvider")
    void testQueryParamCount(String sql, int expectedCount) {
        int paramCount = SqlParser.countParameterMarkers(sql);
        assertEquals(expectedCount, paramCount,
                () -> String.format("[%s] returned %d parameters. Expected %d.", sql, paramCount, expectedCount));
    }


    private static Stream<Arguments> pameterizedValidSqlProvider() {
        return Stream.of(
                Arguments.of("select X from table", 0),
                Arguments.of("select X from table where Y='?'", 0),

                // single line comments
                Arguments.of("select X from table -- where Y=?", 0),
                Arguments.of("select X from table where Y='?'--", 0),
                Arguments.of("select X from table where Y='?'--?", 0),
                Arguments.of("select X from table where Y='?'--some comment ?", 0),
                Arguments.of("select X from table where Y='?'--some comment ?", 0),
                Arguments.of("select X from table where Y='?'--some comment", 0),

                // multi and single line comments
                Arguments.of("select X,Y /* ? \n ? */ from table where Y='?'--some comment ?", 0),
                Arguments.of("select X,Y /* ? \r\n \n? */ from table where Y='?'--some comment ?", 0),
                Arguments.of("select X,Y /* ? \n ? */ from table where Y='?'--some comment ?", 0),
                Arguments.of("select X,Y /* ? ? */ from table where Y='?'--some comment ?", 0),

                // double quotes
                Arguments.of("select X,Y from table where Y=\"?\"--some comment ?", 0),

                // escaped single quotes
                Arguments.of("select X,Y from table where Y='''?'--some comment ?", 0),

                // 1 param marker
                Arguments.of("select X from table where Y='?'--some comment \n and Z=?", 1),
                Arguments.of("select X from table where Y='?'--some comment \r\n and Z=?", 1),
                Arguments.of("select X from table where Y='?'--some comment \r\n and Z=? /* \n */ -- and P=?", 1),

                // 2 param markers
                Arguments.of("select X from table where Y='?'--some comment \r\n and Z=? /* \n */ and P=?", 2),

                // Many param markers
                Arguments.of("select X from table where A=? and B=? and C=? and D=? and (E=? or F=?) ", 6),
                Arguments.of("select X from table where A=? \n --- \n and B=? /* ? */ and C=? and \n D=? and (E=? or F=?) ", 6)

        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "select X from table /*",
            "select X,Y /* ? \n ? from table where Y='?'--some comment ?",
            "select X,Y /*unterminated-comment ? \n ? from table where A='?' and B='unterminated-literal --some comment \n and c=?"
    })
    void testUnterminatedCommentQueries(String sql) {

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> SqlParser.countParameterMarkers(sql),
                () -> String.format("[%s] did not throw an exception. Expected unterminated comment exception.", sql));

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("unterminated comment"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "select X from table where A='unterminated and B=?",
            "select X,Y from T where A in (?) \n and B=? and C='unterminated --some comment \n and D=?"
    })
    void testUnterminatedStringQueries(String sql) {

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> SqlParser.countParameterMarkers(sql),
                () -> String.format("[%s] did not throw an exception. Expected unterminated string exception.", sql));

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("unterminated string"));
    }
}
