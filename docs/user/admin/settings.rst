.. highlight:: sh

===============
Plugin Settings
===============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 1


Introduction
============

When Elasticsearch bootstraps, SQL plugin will register a few settings in Elasticsearch cluster settings. Most of the settings are able to change dynamically so you can control the behavior of SQL plugin without need to bounce your cluster.


opendistro.sql.enabled
======================

Description
-----------

You can disable SQL plugin to reject all coming requests.

1. The default value is true.
2. This setting is node scope.
3. This setting can be updated dynamically.


Example 1
---------

You can update the setting with a new value like this.

SQL query::

	>> curl -H 'Content-Type: application/json' -X PUT localhost:9200/_opendistro/_sql/settings -d '{
	  "transient" : {
	    "opendistro.sql.enabled" : "false"
	  }
	}'

Result set::

	{
	  "acknowledged" : true,
	  "persistent" : { },
	  "transient" : {
	    "opendistro" : {
	      "sql" : {
	        "enabled" : "false"
	      }
	    }
	  }
	}

Example 2
---------

Query result after the setting updated is like:

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql -d '{
	  "query" : "SELECT * FROM accounts"
	}'

Result set::

	{
	  "error" : {
	    "reason" : "Invalid SQL query",
	    "details" : "Either opendistro.sql.enabled or rest.action.multi.allow_explicit_index setting is false",
	    "type" : "SQLFeatureDisabledException"
	  },
	  "status" : 400
	}

opendistro.sql.query.slowlog
============================

Description
-----------

You can configure the time limit (seconds) for slow query which would be logged as 'Slow query: elapsed=xxx (ms)' in elasticsearch.log.

1. The default value is 2.
2. This setting is node scope.
3. This setting can be updated dynamically.


Example
-------

You can update the setting with a new value like this.

SQL query::

	>> curl -H 'Content-Type: application/json' -X PUT localhost:9200/_opendistro/_sql/settings -d '{
	  "transient" : {
	    "opendistro.sql.query.slowlog" : "10"
	  }
	}'

Result set::

	{
	  "acknowledged" : true,
	  "persistent" : { },
	  "transient" : {
	    "opendistro" : {
	      "sql" : {
	        "query" : {
	          "slowlog" : "10"
	        }
	      }
	    }
	  }
	}

opendistro.sql.query.analysis.enabled
=====================================

Description
-----------

You can disable query analyzer to bypass strict syntactic and semantic analysis.

1. The default value is true.
2. This setting is node scope.
3. This setting can be updated dynamically.


Example
-------

You can update the setting with a new value like this.

SQL query::

	>> curl -H 'Content-Type: application/json' -X PUT localhost:9200/_opendistro/_sql/settings -d '{
	  "transient" : {
	    "opendistro.sql.query.analysis.enabled" : "false"
	  }
	}'

Result set::

	{
	  "acknowledged" : true,
	  "persistent" : { },
	  "transient" : {
	    "opendistro" : {
	      "sql" : {
	        "query" : {
	          "analysis" : {
	            "enabled" : "false"
	          }
	        }
	      }
	    }
	  }
	}

opendistro.sql.query.analysis.semantic.suggestion
=================================================

Description
-----------

You can enable query analyzer to suggest correct field names for quick fix.

1. The default value is false.
2. This setting is node scope.
3. This setting can be updated dynamically.


Example 1
---------

You can update the setting with a new value like this.

SQL query::

	>> curl -H 'Content-Type: application/json' -X PUT localhost:9200/_opendistro/_sql/settings -d '{
	  "transient" : {
	    "opendistro.sql.query.analysis.semantic.suggestion" : "true"
	  }
	}'

Result set::

	{
	  "acknowledged" : true,
	  "persistent" : { },
	  "transient" : {
	    "opendistro" : {
	      "sql" : {
	        "query" : {
	          "analysis" : {
	            "semantic" : {
	              "suggestion" : "true"
	            }
	          }
	        }
	      }
	    }
	  }
	}

Example 2
---------

Query result after the setting updated is like:

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql -d '{
	  "query" : "SELECT first FROM accounts"
	}'

Result set::

	{
	  "error" : {
	    "reason" : "Invalid SQL query",
	    "details" : "Field [first] cannot be found or used here. Did you mean [firstname]?",
	    "type" : "SemanticAnalysisException"
	  },
	  "status" : 400
	}

opendistro.sql.query.analysis.semantic.threshold
================================================

Description
-----------

Because query analysis needs to build semantic context in memory, index with large number of field would be skipped. You can update it to apply analysis to smaller or larger index as needed.

1. The default value is 200.
2. This setting is node scope.
3. This setting can be updated dynamically.


Example
-------

You can update the setting with a new value like this.

SQL query::

	>> curl -H 'Content-Type: application/json' -X PUT localhost:9200/_opendistro/_sql/settings -d '{
	  "transient" : {
	    "opendistro.sql.query.analysis.semantic.threshold" : "50"
	  }
	}'

Result set::

	{
	  "acknowledged" : true,
	  "persistent" : { },
	  "transient" : {
	    "opendistro" : {
	      "sql" : {
	        "query" : {
	          "analysis" : {
	            "semantic" : {
	              "threshold" : "50"
	            }
	          }
	        }
	      }
	    }
	  }
	}

opendistro.sql.query.response.format
====================================

Description
-----------

User can set default response format of the query. The supported format includes: jdbc,json,csv,raw,table.

1. The default value is jdbc.
2. This setting is node scope.
3. This setting can be updated dynamically.


Example 1
---------

You can update the setting with a new value like this.

SQL query::

	>> curl -H 'Content-Type: application/json' -X PUT localhost:9200/_opendistro/_sql/settings -d '{
	  "transient" : {
	    "opendistro.sql.query.response.format" : "json"
	  }
	}'

Result set::

	{
	  "acknowledged" : true,
	  "persistent" : { },
	  "transient" : {
	    "opendistro" : {
	      "sql" : {
	        "query" : {
	          "response" : {
	            "format" : "json"
	          }
	        }
	      }
	    }
	  }
	}

Example 2
---------

Query result after the setting updated is like:

SQL query::

	>> curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql -d '{
	  "query" : "SELECT firstname, lastname, age FROM accounts ORDER BY age LIMIT 2"
	}'

Result set::

	{
	  "_shards" : {
	    "total" : 5,
	    "failed" : 0,
	    "successful" : 5,
	    "skipped" : 0
	  },
	  "hits" : {
	    "hits" : [
	      {
	        "_index" : "accounts",
	        "_type" : "_doc",
	        "_source" : {
	          "firstname" : "Nanette",
	          "age" : 28,
	          "lastname" : "Bates"
	        },
	        "_id" : "13",
	        "sort" : [
	          28
	        ],
	        "_score" : null
	      },
	      {
	        "_index" : "accounts",
	        "_type" : "_doc",
	        "_source" : {
	          "firstname" : "Amber",
	          "age" : 32,
	          "lastname" : "Duke"
	        },
	        "_id" : "1",
	        "sort" : [
	          32
	        ],
	        "_score" : null
	      }
	    ],
	    "total" : {
	      "value" : 4,
	      "relation" : "eq"
	    },
	    "max_score" : null
	  },
	  "took" : 100,
	  "timed_out" : false
	}

opendistro.sql.cursor.enabled
=============================

Description
-----------

User can enable/disable pagination for all queries that are supported.

1. The default value is false.
2. This setting is node scope.
3. This setting can be updated dynamically.


Example
-------

You can update the setting with a new value like this.

SQL query::

	>> curl -H 'Content-Type: application/json' -X PUT localhost:9200/_opendistro/_sql/settings -d '{
	  "transient" : {
	    "opendistro.sql.cursor.enabled" : "true"
	  }
	}'

Result set::

	{
	  "acknowledged" : true,
	  "persistent" : { },
	  "transient" : {
	    "opendistro" : {
	      "sql" : {
	        "cursor" : {
	          "enabled" : "true"
	        }
	      }
	    }
	  }
	}

opendistro.sql.cursor.fetch_size
================================

Description
-----------

User can set the default fetch_size for all queries that are supported by pagination. Explicit `fetch_size` passed in request will override this value

1. The default value is 1000.
2. This setting is node scope.
3. This setting can be updated dynamically.


Example
-------

You can update the setting with a new value like this.

SQL query::

	>> curl -H 'Content-Type: application/json' -X PUT localhost:9200/_opendistro/_sql/settings -d '{
	  "transient" : {
	    "opendistro.sql.cursor.fetch_size" : "50"
	  }
	}'

Result set::

	{
	  "acknowledged" : true,
	  "persistent" : { },
	  "transient" : {
	    "opendistro" : {
	      "sql" : {
	        "cursor" : {
	          "fetch_size" : "50"
	        }
	      }
	    }
	  }
	}

opendistro.sql.cursor.keep_alive
================================

Description
-----------

User can set this value to indicate how long the cursor context should be kept open. Cursor contexts are resource heavy, and a lower value should be used if possible.

1. The default value is 1m.
2. This setting is node scope.
3. This setting can be updated dynamically.


Example
-------

You can update the setting with a new value like this.

SQL query::

	>> curl -H 'Content-Type: application/json' -X PUT localhost:9200/_opendistro/_sql/settings -d '{
	  "transient" : {
	    "opendistro.sql.cursor.keep_alive" : "5m"
	  }
	}'

Result set::

	{
	  "acknowledged" : true,
	  "persistent" : { },
	  "transient" : {
	    "opendistro" : {
	      "sql" : {
	        "cursor" : {
	          "keep_alive" : "5m"
	        }
	      }
	    }
	  }
	}

opendistro.sql.engine.new.enabled
=================================

Description
-----------

We are migrating existing functionalities to a new query engine under development. User can choose to enable the new engine if interested or disable if any issue found.

1. The default value is false.
2. This setting is node scope.
3. This setting can be updated dynamically.


Example
-------

You can update the setting with a new value like this.

SQL query::

	>> curl -H 'Content-Type: application/json' -X PUT localhost:9200/_opendistro/_sql/settings -d '{
	  "transient" : {
	    "opendistro.sql.engine.new.enabled" : "true"
	  }
	}'

Result set::

	{
	  "acknowledged" : true,
	  "persistent" : { },
	  "transient" : {
	    "opendistro" : {
	      "sql" : {
	        "engine" : {
	          "new" : {
	            "enabled" : "true"
	          }
	        }
	      }
	    }
	  }
	}


opendistro.query.size_limit
===========================

Description
-----------

The new engine fetches a default size of index from Elasticsearch set by this setting, the default value is 200. You can change the value to any value not greater than the max result window value in index level (10000 by default), here is an example::

	>> curl -H 'Content-Type: application/json' -X PUT localhost:9200/_cluster/settings -d '{
	  "transient" : {
	    "opendistro.query.size_limit" : 500
	  }
	}'

Result set::

    {
      "acknowledged" : true,
      "persistent" : { },
      "transient" : {
        "opendistro" : {
          "query" : {
            "size_limit" : "500"
          }
        }
      }
    }

