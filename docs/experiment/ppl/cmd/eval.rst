=============
eval
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Description
============
| The ``eval`` command evaluate the expression and append the result to the search result.


Syntax
============
eval <field>=<expression> ["," <field>=<expression> ]...

* field: mandatory. If the field name not exist, a new field is added. If the field name already exists, it will be overrided.
* expression: mandatory. Any expression support by the system.

Example 1: Create the new field
===============================

The example show to create new field doubleAge for each document. The new doubleAge is the evaluation result of age multiply by 2.

PPL query::

    od> source=accounts | eval doubleAge = age * 2 | fields age, doubleAge ;
    fetched rows / total rows = 4/4
    +-------+-------------+
    | age   | doubleAge   |
    |-------+-------------|
    | 32    | 64          |
    | 36    | 72          |
    | 28    | 56          |
    | 33    | 66          |
    +-------+-------------+


Example 2: Override the existing field
======================================

The example show to override the exist age field with age plus 1.

PPL query::

    od> source=accounts | eval age = age + 1 | fields age ;
    fetched rows / total rows = 4/4
    +-------+
    | age   |
    |-------|
    | 33    |
    | 37    |
    | 29    |
    | 34    |
    +-------+

Example 3: Create the new field with field defined in eval
==========================================================

The example show to create a new field ddAge with field defined in eval command. The new field ddAge is the evaluation result of doubleAge multiply by 2, the doubleAge is defined in the eval command.

PPL query::

    od> source=accounts | eval doubleAge = age * 2, ddAge = doubleAge * 2 | fields age, doubleAge, ddAge ;
    fetched rows / total rows = 4/4
    +-------+-------------+---------+
    | age   | doubleAge   | ddAge   |
    |-------+-------------+---------|
    | 32    | 64          | 128     |
    | 36    | 72          | 144     |
    | 28    | 56          | 112     |
    | 33    | 66          | 132     |
    +-------+-------------+---------+