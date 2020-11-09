/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.launch.mysql;

import static com.amazon.opendistroforelasticsearch.sql.benchmark.utils.CommandExecution.executeCommand;

import com.amazon.opendistroforelasticsearch.sql.benchmark.BenchmarkService;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.launch.DatabaseLauncher;

import java.io.IOException;

/**
 * Class to handle launching and shutting down MySQL databases.
 */
public class MysqlDatabaseLauncher implements DatabaseLauncher {

  /**
   * Function to launch an MySQL database.
   */
  @Override
  public void launchDatabase() throws IOException, InterruptedException {
    executeCommand(
        "echo " + BenchmarkService.systemPassword + " | sudo -S systemctl start mysql.service");
    executeCommand("echo " + BenchmarkService.systemPassword + " | sudo -S systemctl status mysql");
  }

  /**
   * Function to shutdown an MySQL database.
   */
  @Override
  public void shutdownDatabase() throws IOException, InterruptedException {
    executeCommand(
        "echo " + BenchmarkService.systemPassword + " | sudo -S systemctl stop mysql.service");
    executeCommand("echo " + BenchmarkService.systemPassword + " | sudo -S systemctl status mysql");
  }
}
