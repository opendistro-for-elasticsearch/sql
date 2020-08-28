
===============
Troubleshooting
===============

.. contents::
   :local:
   :depth: 2


Narrative
=========

SQL plugin is stateless for now so mostly the troubleshooting is focused on why a single query fails.


Syntax Analysis / Semantic Analysis Exceptions
----------------------------------------------

**Symptoms**

When you end up with exceptions similar to as follows:

Query:

.. code-block:: JSON

	POST /_opendistro/_sql
	{
	  "query" : "SELECT * FROM sample:data"
	}

Result:

.. code-block:: JSON

    {
      "reason": "Invalid SQL query",
      "details": "Failed to parse query due to offending symbol [:] at: 'SELECT * FROM xxx WHERE xxx:' <--- HERE...
        More details: Expecting tokens in {<EOF>, 'AND', 'BETWEEN', 'GROUP', 'HAVING', 'IN', 'IS', 'LIKE', 'LIMIT',
        'NOT', 'OR', 'ORDER', 'REGEXP', '*', '/', '%', '+', '-', 'DIV', 'MOD', '=', '>', '<', '!',
        '|', '&', '^', '.', DOT_ID}",
      "type": "SyntaxAnalysisException"
    }

**Workaround**

You need to confirm if the syntax is not supported and disable query analysis if that's the case by the following steps:

1. Check if there is any identifier (e.g. index name, column name etc.) that contains special characters (e.g. ".", " ", "&", "@",...). The SQL parser gets confused when parsing these characters within the identifiers and throws exception. If you do have such identifiers, try to quote them with backticks to pass the syntax and semantic analysis. For example, assume the index name in the query above is "sample:data" but the plugin fails the analysis, you may well try:

.. code-block:: JSON

    POST /_opendistro/_sql
	{
	  "query" : "SELECT * FROM `sample:data`"
	}

Go to the step 2 if not working.

2. Identify syntax error in failed query, and correct the syntax if the query does not follow MySQL grammar. Go to step 3 if your query is correct in syntax but it still ends up syntax exception.

#. Disable strict query analysis in new ANTLR parser with the following code block.

#. Verify if the query can pass now. If the query fails as well, please create an issue in our `GitHub Issues <https://github.com/opendistro-for-elasticsearch/sql/issues>`_ section to report bugs fixing or request new features.

.. code-block:: JSON

    #Disable query analysis
    curl -H 'Content-Type: application/json' -X PUT localhost:9200/_cluster/settings -d '{
      "persistent" : {
        "opendistro.sql.query.analysis.enabled" : false
      }
    }'

    #Verify if the query can pass the verification now
    curl -H 'Content-Type: application/json' -X POST localhost:9200/_opendistro/_sql -d '{
      "query" : "SELECT * FROM ..."
    }'


Index Mapping Verification Exception
------------------------------------

**Symptoms**

.. code-block:: JSON

    {
      "error": {
        "reason": "There was internal problem at backend",
        "details": "When using multiple indices, the mappings must be identical.",
        "type": "VerificationException"
      },
      "status": 503
    }

**Workaround**

If index in query is not an index pattern (index name ends with wildcard), check if the index has multiple types. If nothing works during your workaround, please create an issue in our `GitHub Issues <https://github.com/opendistro-for-elasticsearch/sql/issues>`_ section so that we can provide you with our suggestions and help.