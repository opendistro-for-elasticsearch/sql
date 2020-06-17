=============
fields
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Description
============
| Using ``field`` command to keep or remove fields from the search result.


Syntax
============
field [+|-] <field-list>

* index: optional. if the plus (+) is used, only the fields specified in the field list will be keep. if the minus (-) is used, all the fields specified in the field list will be removed. **Default** +
* field list: mandatory. comma-delimited keep or remove fields.


Example 1: Select specified fields from result
==============================================

The example show fetch account_number, firstname and lastname fields from search results.

PPL query::

    od> source=accounts | fields account_number, firstname, lastname;
    fetched rows / total rows = 4/4
    +------------------+-------------+------------+
    | account_number   | firstname   | lastname   |
    |------------------+-------------+------------|
    | 1                | Amber       | Duke       |
    | 6                | Hattie      | Bond       |
    | 13               | Nanette     | Bates      |
    | 18               | Dale        | Adams      |
    +------------------+-------------+------------+

Example 2: Remove specified fields from result
==============================================

The example show fetch remove account_number field from search results.

PPL query::

    od> source=accounts | fields account_number, firstname, lastname | fields - account_number ;
    fetched rows / total rows = 4/4
    +-------------+------------+
    | firstname   | lastname   |
    |-------------+------------|
    | Amber       | Duke       |
    | Hattie      | Bond       |
    | Nanette     | Bates      |
    | Dale        | Adams      |
    +-------------+------------+

