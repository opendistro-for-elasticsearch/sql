Example
-------

Test query::

    sh$ curl -sS -H 'Content-Type: application/json' \
    ... -X POST localhost:9200/_opendistro/_ppl \
    ... -d '{"query" : "source=demo-log | where region="us-east-1" | eval addOneStatus=status+1 | fields region, addOneStatus"}'
    {
      "schema": [
        {
          "name": "region",
          "type": "string"
        },
        {
          "name": "addOneStatus",
          "type": "long"
        }
      ],
      "total": 2,
      "datarows": [
        {
          "row": [
            "us-east-1",
            201
          ]
        },
        {
          "row": [
            "us-east-1",
            405
          ]
        }
      ],
      "size": 2
    }
