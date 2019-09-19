# SubQuery

# Rewrite EXISTS in Nested Query

Opendistro-sql use ElasticSearch nested query to query the nested field. In ElasticSearch domain, the nested query can filter the nested field based the query logic which is correspond to the [EXISTS in PartiQL](https://partiql.org/tutorial.html#use-case-checking-whether-a-nested-collection-satisfies-a-condition). It means, there could be a way to translate the EXISTS query based on the nested query.
Let’s go though the use cases find out how to translate the SQL to DSL.

## Use cases

### **1.  EXISTS**

If the SQL doesn’t have condition in the subquery, the [ElasticSearch exists](https://www.elastic.co/guide/en/elasticsearch/reference/7.3/query-dsl-exists-query.html) could be used to translate the SQL to DSL.

```
SELECT e.name AS employeeName
FROM web_in AS s, s.employeesNest as e
WHERE EXISTS (SELECT * FROM e.projects AS p)

// DSL
GET /employee_nested/_search
{
  "from": 0,
  "size": 200,
  "query": {
    "bool": {
      "filter": [
        {
          "bool": {
            "must": [
              {
                "nested": {
                  "query": {
                    "exists": {
                      "field": "projects"
                    }
                  },
                  "path": "projects",
                  "ignore_unmapped": false,
                  "score_mode": "none",
                  "boost": 1
                }
              }
            ],
            "adjust_pure_negative": true,
            "boost": 1
          }
        }
      ],
      "adjust_pure_negative": true,
      "boost": 1
    }
  },
  "_source": {
    "includes": [
      "name"
    ],
    "excludes": []
  }
}

```



### **2.  EXISTS with WHERE condition**

```
// SQL
SELECT e.name AS employeeName
FROM web_in AS s, s.employeesNest as e
WHERE EXISTS(SELECT * 
             FROM e.projects AS p 
             WHERE p.name LIKE '%security%')
// DSL
{
  "from": 0,
  "size": 200,
  "query": {
    "bool": {
      "filter": [
        {
          "bool": {
            "must": [
              {
                "nested": {
                  "query": {
                    "wildcard": {
                      "projects.name": {
                        "wildcard": "security",
                        "boost": 1
                      }
                    }
                  },
                  "path": "projects",
                  "ignore_unmapped": false,
                  "score_mode": "none",
                  "boost": 1
                }
              }
            ],
            "adjust_pure_negative": true,
            "boost": 1
          }
        }
      ],
      "adjust_pure_negative": true,
      "boost": 1
    }
  },
  "_source": {
    "includes": [
      "name"
    ],
    "excludes": []
  }
}

// Opendistro-SQL
SELECT e.name 
FROM employee_nested as e, e.projects as p 
WHERE p.name LIKE 'security'

// Result
{employeeName:"Bob Smith"}
{employeeName:"Jane Smith"}
```

## Implementation
//Todo

# Appendix I - Test Data

## EmployeeName

```
{
  "employeesNest": [
    {
      "id": 3,
      "name": "Bob Smith",
      "title": null,
      "projects": [
        {
          "name": "AWS Redshift Spectrum querying",
          "started_year": 1990,
          "address": [
            {
              "city": "Seattle",
              "state": "WA"
            },
            {
              "city": "Boston",
              "state": "MA"
            }
          ]
        },
        {
          "name": "AWS Redshift security",
          "started_year": 1999,
          "address": [
            {
              "city": "Chicago",
              "state": "IL"
            }
          ]
        },
        {
          "name": "AWS Aurora security",
          "started_year": 2015
        }
      ],
      "comments": [
        {
          "date": "2018-06-23",
          "message": "I love New york",
          "likes": 56
        },
        {
          "date": "2017-10-25",
          "message": "Today is good weather",
          "likes": 22
        }
      ]
    },
    {
      "id": 4,
      "name": "Susan Smith",
      "title": "Dev Mgr",
      "projects": [],
      "comments": [
        {
          "date": "2018-06-23",
          "message": "comment_2_1",
          "likes": 56
        },
        {
          "date": "2017-10-25",
          "message": "comment_2_2",
          "likes": 22
        }
      ]
    },
    {
      "id": 6,
      "name": "Jane Smith",
      "title": "Software Eng 2",
      "projects": [
        {
          "name": "AWS Redshift security",
          "started_year": 1998
        },
        {
          "name": "AWS Hello security",
          "started_year": 2015,
          "address": [
            {
              "city": "Dallas",
              "state": "TX"
            }
          ]
        }
      ],
      "comments": [
        {
          "date": "2018-06-23",
          "message": "comment_3_1",
          "likes": 24
        },
        {
          "date": "2017-10-25",
          "message": "comment_3_2",
          "likes": 42
        }
      ]
    }
  ]
}
```