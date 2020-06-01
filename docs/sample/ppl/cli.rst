=============
Basic Queries
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Introduction
============



Example 2: Selecting Specific Fields
------------------------------------

More often you would give specific field name(s) in ``fields`` clause to avoid large and unnecessary data retrieved.

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
