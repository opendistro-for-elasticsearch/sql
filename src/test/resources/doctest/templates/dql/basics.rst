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

Syntax
------

The syntax of ``SELECT`` statement is as follows

::

  SELECT [DISTINCT] (* | expression) [[AS] alias] [, ...]
  FROM index_name
  [WHERE predicates]
  [GROUP BY expression [, ...]
   [HAVING predicates]]
  [ORDER BY expression [IS [NOT] NULL] [ASC | DESC] [, ...]]
  [LIMIT [offset, ] size]

Fundamentals
------------

Besides built-in keyword, the most basic element is literal and identifier. Literal is decimal, real, string, date or boolean constant. Identifier represents Elasticsearch index or field name. With arithmetic operators and SQL functions, the basic literals and identifiers can be built into complex expression.

Rule ``expressionAtom``:

.. image:: /docs/user/img/rdd/expressionAtom.png

The expression in turn can be combined into predicate with logical operator. Typically predicate is used in ``WHERE`` and ``HAVING`` clause to filter out data by conditions specified.

Rule ``expression``:

.. image:: /docs/user/img/rdd/expression.png

Rule ``predicate``:

.. image:: /docs/user/img/rdd/predicate.png

Order of Execution
------------------

Note that the actual order of execution is very likely different from its appearance

::

  FROM index
   WHERE predicates
    GROUP BY expressions
     HAVING predicates
      SELECT expressions
       ORDER BY expressions
        LIMIT size

