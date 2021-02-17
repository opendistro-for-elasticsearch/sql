
## Agenda

Presenter: Chen Dai 

Contact info: https://www.linkedin.com/in/chen-dai/

Date: Nov 16, 2020

Topics:

1. A Brief Introduction to SQL and related projects
2. What’re already supported in the SQL engine
3. What’re new improvements in the SQL engine


---
## Introduction

Here is a great blog that covers main features of SQL, Query Workbench, SQL-CLI, JDBC/ODBC driver: https://opendistro.github.io/for-elasticsearch/blog/odfe-updates/2020/06/An-overview-of-the-SQL-Engine-in-Open-Distro-for-Elasticsearch/

Now we can start the demo. Note that you can follow the instructions in Resources (II) Demo Cluster to set up Elasticsearch and Kibana cluster locally.

## Current SQL Engine

Demonstrate the support for basic SQL queries, complex queries and Elasticsearch functions.

```
#Queries
##Basic query
SELECT Carrier, AVG(FlightDelayMin)
FROM kibana_sample_data_flights
WHERE OriginWeather = 'Sunny'
GROUP BY Carrier
HAVING AVG(FlightDelayMin) > 40
ORDER BY Carrier
LIMIT 3

##Complex query
SELECT f1.FlightNum, f2.Carrier
FROM kibana_sample_data_flights f1
JOIN kibana_sample_data_flights f2
  ON f1.FlightNum = f2.FlightNum
WHERE f1.OriginWeather = 'Sunny'

#Elasticsearch
##Full text search
SELECT customer_full_name
FROM kibana_sample_data_ecommerce
WHERE MATCH_QUERY(customer_full_name, 'King')

##Nested field query
SELECT * FROM employees LIMIT 10

SELECT p.name, p.started_year
FROM employees e,
    e.projects p
WHERE p.name LIKE '%Redshift%'

```

## Improvements on SQL Engine

Architecture changes: https://github.com/opendistro-for-elasticsearch/sql/blob/develop/docs/dev/Architecture.md

```
#New query planner: explain this to see
SELECT Carrier, AVG(FlightDelayMin)
FROM kibana_sample_data_flights
WHERE OriginWeather = 'Sunny' 
GROUP BY Carrier

#Extensibility example: ranking window function
SELECT
    Carrier, FlightDelayMin,
    RANK() OVER(
    PARTITION BY Carrier
    ORDER BY FlightDelayMin DESC
    ) AS rnk
FROM kibana_sample_data_flights
WHERE FlightDelayMin > 0

#New expression system
SELECT
  SUBSTRING(Carrier, 1, 2) AS sub,
  AVG(ABS(FlightDelayMin * -10))
FROM kibana_sample_data_flights
WHERE OriginWeather = 'Sunny' 
GROUP BY sub
```

## Features in Development

1. SQL standard
    1. SQL Functions
    2. Complex queries: JOINs
2. Elasticsearch
    1. More nested field support
    2. More metrics and bucket functions

Contribution: https://github.com/opendistro-for-elasticsearch/sql/blob/develop/docs/developing.rst

---
## Resources

### (I) Documentations

1. SQL reference manual
    1. https://opendistro.github.io/for-elasticsearch-docs/docs/sql/
    2. https://github.com/opendistro-for-elasticsearch/sql/blob/develop/docs/user/index.rst
2. Setting for enabling new SQL engine: https://github.com/opendistro-for-elasticsearch/sql/blob/master/docs/user/admin/settings.rst#opendistro-sql-engine-new-enabled

### (II) Demo Cluster Setup

1. Create a Docker compose file as below
2. Start docker container: `docker-compose up`
3. Visit Kibana on http://localhost:5601 (default username and password are both `admin`)
4. Load Kibana sample flights and ecommerce indices and `employees` test index as below
5. Start playing with our SQL plugin


`docker-compose.yml` sample:

```
version: '3'
services:
  odfe-node1:
    image: amazon/opendistro-for-elasticsearch:1.11.0
    container_name: odfe-node1
    environment:
      - cluster.name=odfe-cluster
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms1024m -Xmx1024m" # minimum and maximum Java heap size, recommend setting both to 50% of system RAM
    ports:
      - 9200:9200
      - 9600:9600 # required for Performance Analyzer
    networks:
      - odfe-net
  kibana:
    image: amazon/opendistro-for-elasticsearch-kibana:1.11.0
    container_name: odfe-kibana
    ports:
      - 5601:5601
    expose:
      - "5601"
    environment:
      ELASTICSEARCH_URL: https://odfe-node1:9200
      ELASTICSEARCH_HOSTS: https://odfe-node1:9200
    networks:
      - odfe-net

networks:
  odfe-net:
```

Index mapping for `employees`:

```
PUT employees
{
  "mappings": {
    "properties": {
      "id": {
        "type": "long"
      },
      "name": {
        "type": "text",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      },
      "projects": {
        "type": "nested",
        "properties": {
          "name": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword"
              }
            },
            "fielddata": true
          },
          "started_year": {
            "type": "long"
          }
        }
      },
      "title": {
        "type": "text",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      }
    }
  }
}

POST employees/_bulk
{"index":{"_id":"1"}}
{"id":3,"name":"Bob Smith","title":null,"projects":[{"name":"AWS Redshift Spectrum querying","started_year":1990},{"name":"AWS Redshift security","started_year":1999},{"name":"AWS Aurora security","started_year":2015}]}
{"index":{"_id":"2"}}
{"id":4,"name":"Susan Smith","title":"Dev Mgr","projects":[]}
{"index":{"_id":"3"}}
{"id":6,"name":"Jane Smith","title":"Software Eng 2","projects":[{"name":"AWS Redshift security","started_year":1998},{"name":"AWS Hello security","started_year":2015,"address":[{"city":"Dallas","state":"TX"}]}]}
```

