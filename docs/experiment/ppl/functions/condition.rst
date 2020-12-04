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

