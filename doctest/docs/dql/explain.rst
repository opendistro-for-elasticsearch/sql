Example
-------

Explain query::

	sh$ curl -sS -H 'Content-Type: application/json' \
	... -X POST localhost:9200/_opendistro/_sql/_explain \
	... -d '{"query" : "SELECT firstname, lastname FROM accounts WHERE age > 20"}'
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
	                "range": {
	                  "age": {
	                    "from": 20,
	                    "to": null,
	                    "include_lower": false,
	                    "include_upper": true,
	                    "boost": 1.0
	                  }
	                }
	              }
	            ],
	            "adjust_pure_negative": true,
	            "boost": 1.0
	          }
	        }
	      ],
	      "adjust_pure_negative": true,
	      "boost": 1.0
	    }
	  },
	  "_source": {
	    "includes": [
	      "firstname",
	      "lastname"
	    ],
	    "excludes": []
	  }
	}

