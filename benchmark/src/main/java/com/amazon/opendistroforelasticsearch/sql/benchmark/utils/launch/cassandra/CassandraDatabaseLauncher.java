package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.launch.cassandra;

import static com.amazon.opendistroforelasticsearch.sql.benchmark.utils.CommandExecution.executeCommand;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.launch.DatabaseLauncher;
import java.io.IOException;

public class CassandraDatabaseLauncher implements DatabaseLauncher {

  /**
   * Function to launch an Cassandra database.
   */
  @Override
  public void launchDatabase(String systemPassword) throws IOException, InterruptedException {
    String commands = "echo " + systemPassword + " | sudo -S "
        + "update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java"
        + " && echo " + systemPassword + " | sudo -S systemctl start cassandra.service"
        + " && sudo systemctl status cassandra";
    executeCommand(commands);
  }

  /**
   * Function to shutdown an Cassandra database.
   */
  @Override
  public void shutdownDatabase(String systemPassword) throws IOException, InterruptedException {
    String commands = "echo " + systemPassword + " | sudo -S systemctl stop cassandra.service"
        + " && sudo systemctl status cassandra";
    executeCommand(commands);
  }
}
