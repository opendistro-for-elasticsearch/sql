
=============
Basic Queries
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Introduction
============

``SELECT`` statement in SQL is the most common query that retrieves data from Elasticsearch index. In this doc, only simple ``SELECT`` statement with single index and query involved is covered. A ``SELECT`` statement includes ``SELECT``, ``FROM``, ``WHERE``, ``GROUP BY``, ``HAVING``, ``ORDER BY`` and ``LIMIT`` clause. Among these clauses, ``SELECT`` and ``FROM`` are the foundation to specify which fields to be fetched and which index they should be fetched from. All others are optional and used according to your needs. Please read on for their description, syntax and use cases in details.


Example 2: Selecting Specific Fields
------------------------------------

More often you would give specific field name(s) in ``SELECT`` clause to avoid large and unnecessary data retrieved.

PPL query::

    od> source=accounts | fields firstname, lastname;
    fetched rows / total rows = 4/4
    +-------------+------------+
    | firstname   | lastname   |
    |-------------+------------|
    | Amber       | Duke       |
    | Hattie      | Bond       |
    | Nanette     | Bates      |
    | Dale        | Adams      |
    +-------------+------------+
