/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.security;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import org.elasticsearch.SpecialPermission;

/**
 * Ref:
 * https://www.elastic.co/guide/en/elasticsearch/plugins/current/plugin-authors.html#_java_security_permissions
 */
public class SecurityAccess {

  /**
   * Execute the operation in privileged mode.
   */
  public static <T> T doPrivileged(final PrivilegedExceptionAction<T> operation)
      throws IOException {
    SpecialPermission.check();
    try {
      return AccessController.doPrivileged(operation);
    } catch (final PrivilegedActionException e) {
      throw (IOException) e.getCause();
    }
  }
}
