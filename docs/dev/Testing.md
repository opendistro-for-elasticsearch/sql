# How OpenDistro SQL Is Tested

## 1.Introduction

### 1.1 Problem Statement

Currently there are quite a few unit tests and integration tests in the codebase. However, there are still problems to rely on these test cases only to ensure the correctness of our plugin:

 1. **Test coverage**: Although the amount of test cases seems large, we’ve found that more than one important case was missed, for example use table alias in FROM clause, GROUP BY field alias etc.
 2. **Test correctness**: Some test case make assertion on the result set too simple to capture the issue if any. For example, some assertion is as loose as checking if result set is not empty. Unfortunately with strict assertion, we’re still not sure if the assertion is correct or not. For example, there were issue [#124](https://github.com/opendistro-for-elasticsearch/sql/issues/124) and [#226](https://github.com/opendistro-for-elasticsearch/sql/issues/226) regarding LEFT JOIN with WHERE clause and SUBSTRING. The correct semantic was actually different from what we thought. We did have MySQL reference docs at hands but it’s possible to miss some cases or misunderstand the correct use. So some mechanism is required to replace the loose assertion and be able to enforce the verification of our understanding.
 3. **Test Bench**: We want to run this new test on a regular basis to improving our implementation continuously.

### 1.2 Our Goals

In this work, we want to address the problems above for our testing to get more confidence of the correctness of our plugin. To achieve this goal, we need to improve both the quantity and quality of our tests and also keep it running on a regular basis. Although the search space of SQL is infinite, we can cover typical use cases and corner cases and most importantly make sure it does demonstrate the correctness of our implementation if it can pass.

> Note that performance is also another important area that we want to test and understand how efficient or where is the bottleneck of typical queries. Since Elasticsearch provides ESRally benchmark tool which is totally different from our homemade test harness, we will be focuses on the correctness and won't cover performance testing here.

## 2.Design

### 2.1 Approaches

First we can improve the test coverage by improving the diversity of our test case set, including diversity of test schema, test data and test cases. Secondly although it's difficult to provide rigorous mathematical proof for the result set of each query, we can make our best to ensure relative correctness by comparing with a few other database implementations. Of course the comparison testing doesn't guarantee 100% correctness and heavily depends on the correctness of reference implementation. But it should be much more reliable than hand written assertions after reading specification documentation previously.

### 2.2 Components

At this stage we don’t want to spend too much efforts on setting up a complicated infrastructure for testing. So we can take full advantage of capabilities that GitHub provides:

 1. **Test Data & Cases**: Use test case set with Kibana flights and ecommerce sample index.
 2. **Trigger**: Set up another GitHub Action workflow.
 3. **Test Runner**: Use embedded Elasticsearch and other IMDBs.
 4. **Reporting**: Use standard JUnit report or simple custom json format.
 5. **Visualization**: Enable GitHub Pages for viewing or feed into Elasticsearch.

![Test Framework Components](img/test-framework-components.png)

## 3.Implementation

### 3.1 Test Data

For schema, we can just use Elasticsearch mapping as the format of schema and convert it to `INSERT` statement. For data we use CSV format simply.

```
{
  "_doc": {
    "properties": {
      "AvgTicketPrice": {
        "type": "float"
      },
      "Cancelled": {
        "type": "boolean"
      },
      "Carrier": {
        "type": "keyword"
      },
      ...
      ...
      "dayOfWeek": {
        "type": "integer"
      }
    }
  }
}

FlightNum,Origin,FlightDelay,DistanceMiles,...,DestCityName
9HY9SWR,Frankfurt am Main Airport,false,10247.856675613455,...,Sydney
X98CCZO,Cape Town International Airport,false,5482.606664853586,...,Venice
......
```

### 3.2 Test Cases

For now we don't implement a Fuzzer to generate test queries because test queries prepared manually is sufficient to help identify issues. So we just put all queries in a text file. In future, a fuzzer can generate queries and save to file to integrate with Test Runner smoothly.

```
SELECT 1 AS `empty` FROM `kibana_sample_data_flights`
SELECT substring(OriginWeather, 1, 2) AS OriginWeather FROM kibana_sample_data_flights
SELECT SUM(FlightDelayMin) AS sum_FlightDelayMin_ok FROM kibana_sample_data_flights
SELECT SUM(FlightDelay) AS sum_FlightDelay_ok FROM kibana_sample_data_flights
SELECT SUM(DistanceMiles) AS sum_DistanceMiles_ok FROM kibana_sample_data_flights
```

### 3.3 Test Runner

To simplify the test and be confident about the test result, Test Runner runs query by JDBC driver of OpenDistro SQL and other databases. In this case we don’t need to parse the data format returned from our plugin. And obviously another benefit is the correctness of JDBC driver is also covered.

![How We Do Comparison Test](img/how-we-do-comparison-test.png)

### 3.4 Trigger

GitHub Action can be set up to trigger Gradle task to generate test report.

### 3.5 Reporting

Elasticsearch integration test is still using JUnit 4 which has many problems, such as dynamic test cases. So we can define our own report format for flexibility:

```
{
  "summary": {
    "total": 3,
    "success": 1,
    "failure": 2
  },
  "tests": [
    {
      "id": 1,
      "sql": "...",
      "result": "Success"
    },
    {
      "id": 2,
      "sql": "...",
      "result": "Failed",
      "resultSets": [
        {
          "database": "Elasticsearch",
          "resultSet": {
            "schema": [{"name":"","type":""},...],
            "dataRows": [[...],...,[...]]
          }
        },
        {
          "database": "Other database",
          "resultSet": {
            "schema": [...],
            "dataRows": [[...],...,[...]]
          }
        }
      ]
    },
    {
      "id": 3,
      "sql": "...",
      "result": "Failed",
      "reason": "..."
    },
  ]
}
```

The workflow of generating test result is:

![The Workflow of Comparison Test](img/the-workflow-of-comparison-test.png)

### 3.6 Visualization

TODO

