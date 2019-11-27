.. highlight:: sh

=========================
Developing OpenDistro SQL
=========================

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Prerequisites
=============

JDK
---

JDK 12 is required to build the plugin because of the dependency on Elasticsearch test framework in our integration test. So you must have a JDK 12 installation on your machine. After the installation, please configure JAVA_HOME environment variable accordingly. If everything goes right, you should be able to see sample output as below on MacOS::

 $ echo $JAVA_HOME
 /Library/Java/JavaVirtualMachines/jdk-12.0.2.jdk/Contents/Home

 $ java -version
 java version "12.0.2" 2019-07-16
 Java(TM) SE Runtime Environment (build 12.0.2+10)
 Java HotSpot(TM) 64-Bit Server VM (build 12.0.2+10, mixed mode, sharing)

Here is official instruction on how to set JAVA_HOME for different platforms: https://docs.oracle.com/cd/E19182-01/820-7851/inst_cli_jdk_javahome_t/. 

Elasticsearch & Kibana
----------------------

For your convenience to deploy code changes when developing, it’s suggested to install Elasticsearch and Kibana on your local machine. You can simply download open source zip for both software and extract them to a folder.

Note that Kibana is optional for the development but would be more productive to test your query. Alternatively you can run your query against the plugin in Elasticsearch by CURL from terminal.


Getting Source Code
===================

Now you can check out the code from your forked GitHub repository and create a new branch for your bug fix or enhancement work.::

 $ git clone https://github.com/<your_account>/sql.git
 $ git checkout -b <branch_name>

If there is update in master or you want to keep the forked repository long living, you can sync it by following the instructions: https://help.github.com/en/github/collaborating-with-issues-and-pull-requests/syncing-a-fork. Basically you just need to pull latest changes from upstream master once you add it for the first time.::

 #Merge to your local master
 $ git fetch upstream
 $ git checkout master
 $ git merge upstream/master

 #Merge to your branch if any
 $ git checkout <branch_name>
 $ git merge master

After getting the source code as well as Elasticsearch and Kibana, your workspace layout may look like this::

 $ make opendistro
 $ cd opendistro
 $ ls -la                                                                     
 total 32
 drwxr-xr-x  7 user group^users 4096 Nov 21 12:59 .
 drwxr-xr-x 19 user group^users 4096 Nov 21 09:44 ..
 drwxr-xr-x 10 user group^users 4096 Nov  8 12:16 elasticsearch-7.3.2
 drwxr-xr-x 14 user group^users 4096 Nov  8 12:14 kibana-7.3.2-linux-x86_64
 drwxr-xr-x 16 user group^users 4096 Nov 15 10:59 sql
 drwxr-xr-x  9 user group^users 4096 Oct 31 14:39 sql-jdbc


Configuring IDEs
================

You can develop the plugin in your favorite IDEs such as Eclipse and IntelliJ IDEs. Before start making any code change, you may want to configure your IDEs. In this section we take Intellij IDEA for example to show you how to get it ready for development soon.

Java Language Level
-------------------

Although JDK 12 is required to build the plugin, the Java language level needs to be Java 8 for compatibility. Only in this case your plugin works with Elasticsearch running against JDK 8. Otherwise it will raise runtime exception when executing new API from new JDK. In case your IDE doesn’t set it right, you may want to double check your project setting after import.

Remote Debugging
----------------

Firstly you need to add the following configuration to the JVM used by your IDE. For Intellij IDEA, it should be added to <ES installation>/config/jvm.options file. After configuring this, an agent in JVM will listen on the port when your Elasticsearch bootstraps and wait for IDE debugger to connect. So you should be able to debug by setting up a “Remote Run/Debug Configuration”::

 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005

License Header
--------------

Because our code is licensed under Apache 2, you need to add the following license header to all new source code files. To automate this whenever creating new file, you can follow instructions for your IDE::

 /*
  * Licensed under the Apache License, Version 2.0 (the "License").
  * You may not use this file except in compliance with the License.
  * A copy of the License is located at
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0
  * 
  * or in the "license" file accompanying this file. This file is distributed 
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
  * express or implied. See the License for the specific language governing 
  * permissions and limitations under the License.
  */

For example, here is the instructions for Intellij IDEA https://www.jetbrains.com/help/idea/copyright.html to add a Template and Scope profile using the template.

Note that missing license header will be detected by Gradle license plugin and fails the build.


Making Code Changes
===================

Project Strucure
----------------

The plugin codebase is in standard layout of Gradle project::

 .
 ├── CODE_OF_CONDUCT.md
 ├── CONTRIBUTING.md
 ├── LICENSE.TXT
 ├── NOTICE
 ├── README.md
 ├── THIRD-PARTY
 ├── build.gradle
 ├── config
 │   └── checkstyle
 │       ├── checkstyle.xml
 │       └── suppressions.xml
 ├── docs
 │   ├── dev
 │   │   ├── SemanticAnalysis.md
 │   │   ├── SubQuery.md
 │   │   └── img
 │   └── user
 │       ├── admin
 │       ├── index.rst
 │       └── interfaces
 ├── gradle.properties
 ├── gradlew
 ├── gradlew.bat
 ├── opendistro-elasticsearch-sql.release-notes
 ├── settings.gradle
 └── src
     ├── assembly
     │   └── zip.xml
     ├── main
     │   ├── antlr
     │   ├── java
     │   └── resources
     └── test
         ├── java
         └── resources

Here are files and folders you are most likely to touch:

- build.gradle: Gradle build script.
- config/: only Checkstyle configuration files for now.
- docs/: include documentation for developers and reference manual for users.
- src/: source code root

  - main/antlr: ANTLR4 grammar files.
  - main/java: Java source code.
  - test/java: Java test code.

Code Convention
---------------

We’re integrated Checkstyle plugin into Gradle build: https://github.com/opendistro-for-elasticsearch/sql/blob/master/config/checkstyle/checkstyle.xml. So any violation will fail the build. You need to identify the offending code from Gradle error message and fix them and rerun the Gradle build. Here are the highlight of some Checkstyle rules:

* No line starts with tab character in source file.
* Line width <= 120 characters.
* Wildcard imports: You can enforce single import by configuring your IDE. Instructions for Intellij IDEA: https://www.jetbrains.com/help/idea/creating-and-optimizing-imports.html#disable-wildcard-imports.
* Operator needs to wrap at next line.


Building and Running Tests
==========================

Gradle Build
------------

Most of the time you just need to run ./gradlew build which will make sure you pass all checks and testing. While you’re developing, you may want to run specific Gradle task only. In this case, you can run ./gradlew with task name which only triggers the task along with those it depends on. Here is a list for common tasks:

+---------------------------------+-----------------------------------------------------------+
|   Gradle Task                   |  Description                                              |
+=================================+===========================================================+
| ./gradlew assemble              |  Generate jar and zip files in build/distributions folder.|
+---------------------------------+-----------------------------------------------------------+
| ./gradlew generateGrammarSource |  (Re-)Generate ANTLR parser from grammar file.            |
+---------------------------------+-----------------------------------------------------------+
| ./gradlew compileJava           |  Compile all Java source files.                           |
+---------------------------------+-----------------------------------------------------------+
| ./gradlew checkstyle            |  Run all checks according to Checkstyle configuration.    |
+---------------------------------+-----------------------------------------------------------+
| ./gradlew test                  |  Run all unit tests.                                      |
+---------------------------------+-----------------------------------------------------------+
| ./gradlew integTestRunner       |  Run all integration test (this takes time).              |
+---------------------------------+-----------------------------------------------------------+

For `test` and `integTestRunner`, you can use —tests “UT full path” to run a task individually. For example ./gradlew test --tests “com.amazon.opendistroforelasticsearch.sql.unittest.LocalClusterStateTest”.

Sometimes your Gradle build fails or timeout due to Elasticsearch integration test process hung there. You can check this by the following commands::

 #Check if multiple Gradle daemons started by different JDK.
 #Kill unnecessary ones and restart if necessary.
 $ ps aux | grep -i gradle
 $ ./gradlew stop
 $ ./gradlew start

 #Check if ES integTest process hung there. Kill it if so.
 $ ps aux | grep -i elasticsearch

 #Clean and rebuild
 $ ./gradlew clean
 $ ./gradlew build

Tips for Testing
----------------

For test cases, you can use the cases in the following checklist in case you miss any important one and break some queries:

- *Functions*

  - SQL functions
  - Special Elasticsearch functions
  
- *Basic Query*

  - SELECT-FROM-WHERE
  - GROUP BY & HAVING
  - ORDER BY
  
- *Alias*

  - Table alias
  - Field alias
  
- *Complex Query*

  - Subquery: IN/EXISTS
  - JOIN: INNER/LEFT OUTER.
  - Nested field query
  - Multi-query: UNION/MINUS
  
- *Other Statements*

  - DELETE
  - SHOW
  - DESCRIBE
  
- *Explain*

  - DSL for simple query
  - Execution plan for complex query like JOIN
  
- *Response format*

  - Default
  - JDBC: You could set up DbVisualizer or other GUI.
  - CSV
  - Raw

For unit test:

* Put your test class in the same package in src/test/java so you can access and test package-level method.
* Make sure you are testing against the right abstraction. For example a bad practice is to create many classes by ESActionFactory class and write test cases on very high level. This makes it more like an integration test.

For integration test:

* Elasticsearch test framework is in use so an in-memory cluster will spin up for each test class.
* You can only access the plugin and verify the correctness of your functionality via REST client externally. 

Here is a sample for integration test for your reference:

.. code:: java

 public class XXXIT extends SQLIntegTestCase { // Extends our base test class
 
     @Override
     protected void init() throws Exception {
         loadIndex(Index.ACCOUNT); // Load predefined test index mapping and data
     }
 
     @Override
     public void testXXX() { // Test query against the index and make assertion
         JSONObject response = executeQuery("SELECT ...");
         Assert.assertEquals(6, getTotalHits(response));
     }
 }

Finally thanks to JaCoCo library, you can check out the test coverage for your changes easily.

Deploying Locally
-----------------

Sometime you want to deploy your changes to local Elasticsearch cluster, basically there are couple of steps you need to follow:

1. Re-assemble to generate plugin jar file with your changes.
2. Replace the jar file with the new one in your workspace.
3. Restart Elasticsearch cluster to take it effect.


To automate this common task, you can prepare an all-in-one command for reuse. Below is a sample command for MacOS::

 ./gradlew assemble && {echo y | cp -f build/distributions/opendistro_sql-1*0.jar <Elasticsearch_home>/plugins/opendistro-sql} && {kill $(ps aux | awk '/[E]lasticsearch/ {print $2}'); sleep 3; nohup <Elasticsearch_home>/bin/elasticsearch > ~/Temp/es.log 2>&1 &}

Note that for the first time you need to create `opendistro-sql` folder and unzip `build/distribution/opendistro_sql-xxxx.zip` to it.


Documentation
=============

Dev Docs
--------

For new feature or big enhancement, it is worth document your design idea for others to understand your code better. There is already a docs/dev folder for all this kind of development documents.

Reference Manual
----------------

Currently the reference manual documents are generated from a set of special integration tests. The integration tests use custom DSL to build ReStructure Text markup with real query and result set captured and documented.

1. Add a new template to `src/test/resources/doctest/templates`.
2. Add a new test class as below with `@DocTestConfig` annotation specifying template and test data used.
3. Run `./gradlew build` to generate the actual documents into `docs/user` folder.

Sample test class:

.. code:: java

 @DocTestConfig(template = "interfaces/protocol.rst", testData = {"accounts.json"})
 public class ProtocolIT extends DocTest {
 
     @Section(1)
     public void test() {
         section(
             title("A New Section"),
             description(
                 "Describe what is the use of new functionality."
             ),
             example(
                 description("Describe what is the use case of this example to show"),
                 post("SELECT ...")
             )
         );
     }
