=============
rename
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Description
============
| Using ``rename`` command to rename one in the search result.


Syntax
============
rename <source-field> AS <target-field>

* source-field: mandatory. The name of the field you want to rename.
* field list: mandatory. The name you want to rename to.


Example 1: Rename one field
==============================================

The example show rename one field.

PPL query::

    od> source=accounts | rename account_number as an | fields an;
    fetched rows / total rows = 4/4
    +------+
    | an   |
    |------|
    | 1    |
    | 6    |
    | 13   |
    | 18   |
    +------+

