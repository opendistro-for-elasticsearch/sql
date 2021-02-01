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

    od> source=accounts | eval result = isnull(employer) | fields result, employer, firstname
    fetched rows / total rows = 4/4
    +----------+------------+-------------+
    | result   | employer   | firstname   |
    |----------+------------+-------------|
    | False    | Pyrami     | Amber       |
    | False    | Netagy     | Hattie      |
    | False    | Quility    | Nanette     |
    | True     | null       | Dale        |
    +----------+------------+-------------+

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

IFNULL
------

Description
>>>>>>>>>>>

Usage: ifnull(field1, field2) return field2 if field1 is null.

Argument type: all the supported data type, (NOTE : if two parameters has different type, you will fail semantic check.)

Return type: any

Example::

    od> source=accounts | eval result = ifnull(employer, 'default') | fields result, employer, firstname
    fetched rows / total rows = 4/4
    +----------+------------+-------------+
    | result   | employer   | firstname   |
    |----------+------------+-------------|
    | Pyrami   | Pyrami     | Amber       |
    | Netagy   | Netagy     | Hattie      |
    | Quility  | Quility    | Nanette     |
    | default  | null       | Dale        |
    +----------+------------+-------------+

NULLIF
------

Description
>>>>>>>>>>>

Usage: nullif(field1, field2) return null if two parameters are same, otherwiser return field1.

Argument type: all the supported data type, (NOTE : if two parameters has different type, if two parameters has different type, you will fail semantic check)

Return type: any

Example::

    od> source=accounts | eval result = nullif(employer, 'Pyrami') | fields result, employer, firstname
    fetched rows / total rows = 4/4
    +----------+------------+-------------+
    | result   | employer   | firstname   |
    |----------+------------+-------------|
    | null     | Pyrami     | Amber       |
    | Netagy   | Netagy     | Hattie      |
    | Quility  | Quility    | Nanette     |
    | null     | null       | Dale        |
    +----------+------------+-------------+


ISNULL
------

Description
>>>>>>>>>>>

Usage: isnull(field1, field2) return null if two parameters are same, otherwise return field1.

Argument type: all the supported data type

Return type: any

Example::

    od> source=accounts | eval result = isnull(employer) | fields result, employer, firstname
    fetched rows / total rows = 4/4
    +----------+------------+-------------+
    | result   | employer   | firstname   |
    |----------+------------+-------------|
    | False    | Pyrami     | Amber       |
    | False    | Netagy     | Hattie      |
    | False    | Quility    | Nanette     |
    | True     | null       | Dale        |
    +----------+------------+-------------+

IF
------

Description
>>>>>>>>>>>

Usage: if(condition, expr1, expr2) return expr1 if condition is true, otherwiser return expr2.

Argument type: all the supported data type, (NOTE : if expr1 and expr2 are different type,  you will fail semantic check

Return type: any

Example::

    od> source=accounts | eval result = if(true, firstname, lastname) | fields result, firstname, lastname
    fetched rows / total rows = 4/4
    +----------+-------------+------------+
    | result   | firstname   | lastname   |
    |----------+-------------+------------|
    | Amber    | Amber       | Duke       |
    | Hattie   | Hattie      | Bond       |
    | Nanette  | Nanette     | Bates      |
    | Dale     | Dale        | Adams      |
    +----------+-------------+------------+

    od> source=accounts | eval result = if(false, firstname, lastname) | fields result, firstname, lastname
    fetched rows / total rows = 4/4
    +----------+-------------+------------+
    | result   | firstname   | lastname   |
    |----------+-------------+------------|
    | Duke     | Amber       | Duke       |
    | Bond     | Hattie      | Bond       |
    | Bates    | Nanette     | Bates      |
    | Adams    | Dale        | Adams      |
    +----------+-------------+------------+
