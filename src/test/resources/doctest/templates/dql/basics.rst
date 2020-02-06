.. highlight:: json

===========
Basic Query
===========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Introduction
============

``SELECT`` statement in SQL is the most common query that retrieves data from Elasticsearch index. In this documentation, only basic ``SELECT`` statement with single index and query involved is covered. A ``SELECT`` statement includes ``SELECT``, ``FROM``, ``WHERE``, ``GROUP BY``, ``HAVING``, ``ORDER BY`` and ``LIMIT`` clause. Among these elements, ``SELECT`` and ``FROM`` is the fundamental part to specify which fields to be fetched from which Elasticsearch index. All others are optional and used based on your needs. Please read on for their use cases in details.

The syntax of ``SELECT`` statement is as follows::

  SELECT [DISTINCT] (* | expression) [[AS] alias] [, ...]
  FROM index_name
  [WHERE predicates]
  [GROUP BY expression [, ...] [HAVING predicates]]
  [ORDER BY expression [IS [NOT] NULL] [ASC | DESC] [, ...]]
  [LIMIT [offset, ] size]

Note that the actual order of execution is very likely different from its appearance::

  FROM index
   WHERE predicates
    GROUP BY expressions
     HAVING predicates
      SELECT expressions
       ORDER BY expressions
        LIMIT size

Query specification:

.. image:: /docs/user/img/rdd/query_specification.png

`FROM` clause:

.. image:: /docs/user/img/rdd/from_clause.png

