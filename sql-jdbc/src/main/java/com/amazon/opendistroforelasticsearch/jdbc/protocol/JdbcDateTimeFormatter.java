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

package com.amazon.opendistroforelasticsearch.jdbc.protocol;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.sql.Date;

public enum JdbcDateTimeFormatter {

    JDBC_FORMAT("yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss");

    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter timestampFormatter;

    JdbcDateTimeFormatter(String dateFormat, String timestampFormat) {
        this.dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
        this.timestampFormatter = DateTimeFormatter.ofPattern(timestampFormat);
    }

    public String format(Date date) {
        return date.toLocalDate().format(dateFormatter);
    }

    public String format(Timestamp date) {
        return date.toLocalDateTime().format(timestampFormatter);
    }
}
