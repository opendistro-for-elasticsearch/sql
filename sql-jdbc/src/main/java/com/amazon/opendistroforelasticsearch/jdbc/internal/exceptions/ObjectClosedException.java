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

package com.amazon.opendistroforelasticsearch.jdbc.internal.exceptions;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;

/**
 * Exception indicating JDBC operation can not occur due to the
 * target object being in Closed state
 */
public class ObjectClosedException extends SQLNonTransientException {

    public ObjectClosedException(String reason, String SQLState, int vendorCode) {
        super(reason, SQLState, vendorCode);
    }

    public ObjectClosedException(String reason, String SQLState) {
        super(reason, SQLState);
    }

    public ObjectClosedException(String reason) {
        super(reason);
    }

    public ObjectClosedException() {
    }

    public ObjectClosedException(Throwable cause) {
        super(cause);
    }
}
