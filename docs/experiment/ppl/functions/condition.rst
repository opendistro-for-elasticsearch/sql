===================
Condition Functions
===================

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 1

ISNULL
------

Description
>>>>>>>>>>>

Usage: isnull(field) return true if field is null.

Argument type: all the supported data type.

Return type: BOOLEAN

Example::

    od> source=accounts | where isnull(employer) | fields account_number, employer
    fetched rows / total rows = 1/1
    +------------------+------------+
    | account_number   | employer   |
    |------------------+------------|
    | 18               | null       |
    +------------------+------------+

ISNOTNULL
---------

Description
>>>>>>>>>>>

Usage: isnotnull(field) return true if field is not null.

Argument type: all the supported data type.

Return type: BOOLEAN

Example::

    od> source=accounts | where not isnotnull(employer) | fields account_number, employer
    fetched rows / total rows = 1/1
    +------------------+------------+
    | account_number   | employer   |
    |------------------+------------|
    | 18               | null       |
    +------------------+------------+

EXISTS
------

`Because Elasticsearch doesn't differentiate null and missing <https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-exists-query.html>`_. so we can't provide function like ismissing/isnotmissing to test field exist or not. But you can still use isnull/isnotnull for such purpose.

Example, the account 13 doesn't have email field::

    od> source=accounts | where isnull(email) | fields account_number, email
    fetched rows / total rows = 1/1
    +------------------+---------+
    | account_number   | email   |
    |------------------+---------|
    | 13               | null    |
    +------------------+---------+

