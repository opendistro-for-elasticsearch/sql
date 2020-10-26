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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;

/**
 * Class to handle command execution.
 */
public class CommandExecution {
  /**
   * Function to execute commands.
   *
   * @param commands Commands separated by &&.
   */
  public static void executeCommand(final String commands)
      throws IOException, InterruptedException {
    String[] executeCommands = {"/bin/bash", "-c", commands};
    ProcessBuilder processBuilder = new ProcessBuilder(executeCommands);
    Process process = processBuilder.start();
    if (process != null) {
      process.waitFor();
    }
    System.out.println("Output of " + commands);
    try(BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
       String line;
       while ((line = input.readLine()) != null) {
         System.out.println(line);
       }
    }
  }
}
