package com.amazon.opendistroforelasticsearch.sql.benchmark.utils;

import java.io.IOException;

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
  }
}
