
===========
Limitations
===========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Introduction
============

In this doc, the restrictions and limitations of SQL plugin is covered as follows.


Limitations on Aggregations
===========================

Aggregation over expression is not supported for now. You can only apply aggregation on fields, aggregations can't accept an expression as a parameter. For example, `avg(log(age))` is not supported.

Here's a link to the Github issue - [Issue #288](https://github.com/opendistro-for-elasticsearch/sql/issues/288).


Limitations on Subqueries
=========================

Subqueries in the FROM clause
-----------------------------

Subquery in the `FROM` clause in this format: `SELECT outer FROM (SELECT inner)` is supported only when the query is merged into one query. For example, the following query is supported::

    SELECT t.f, t.d
    FROM (
        SELECT FlightNum as f, DestCountry as d
        FROM kibana_sample_data_flights
        WHERE OriginCountry = 'US') t

But, if the outer query has `GROUP BY` or `ORDER BY`, then it's not supported.


Limitations on JOINs
====================

JOIN does not support aggregations on the joined result. The `join` query does not support aggregations on the joined result.
For example, e.g. `SELECT depo.name, avg(empo.age) FROM empo JOIN depo WHERE empo.id == depo.id GROUP BY depo.name` is not supported.

Here's a link to the Github issue - [Issue 110](https://github.com/opendistro-for-elasticsearch/sql/issues/110).


Limitations on Pagination
=========================

Pagination only supports basic queries for now. The pagination query enables you to get back paginated responses.
Currently, the pagination only supports basic queries. For example, the following query returns the data with cursor id::

    POST _opendistro/_sql/
    {
      "fetch_size" : 5,
      "query" : "SELECT OriginCountry, DestCountry FROM kibana_sample_data_flights ORDER BY OriginCountry ASC"
    }

The response in JDBC format with cursor id::

    {
      "schema": [
        {
          "name": "OriginCountry",
          "type": "keyword"
        },
        {
          "name": "DestCountry",
          "type": "keyword"
        }
      ],
      "cursor": "d:eyJhIjp7fSwicyI6IkRYRjFaWEo1UVc1a1JtVjBZMmdCQUFBQUFBQUFCSllXVTJKVU4yeExiWEJSUkhsNFVrdDVXVEZSYkVKSmR3PT0iLCJjIjpbeyJuYW1lIjoiT3JpZ2luQ291bnRyeSIsInR5cGUiOiJrZXl3b3JkIn0seyJuYW1lIjoiRGVzdENvdW50cnkiLCJ0eXBlIjoia2V5d29yZCJ9XSwiZiI6MSwiaSI6ImtpYmFuYV9zYW1wbGVfZGF0YV9mbGlnaHRzIiwibCI6MTMwNTh9",
      "total": 13059,
      "datarows": [[
        "AE",
        "CN"
      ]],
      "size": 1,
      "status": 200
    }

The query with `aggregation` and `join` does not support pagination for now.


Limitations on Query Optimizations
==================================

Multi-fields in WHERE Conditions
--------------------------------

The filter expressions in ``WHERE`` clause may be pushed down to Elasticsearch DSL queries to avoid large amounts of data retrieved. In this case, for Elasticsearch multi-field (a text field with another keyword field inside), assumption is made that the keyword field name is always "keyword" which is true by default.

