===========
Identifiers
===========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Introduction
============

Identifiers are used for naming your database objects, such as index name, field name, alias etc. Basically there are two types of identifiers: regular identifiers and delimited identifiers.


Regular Identifiers
===================

Description
-----------

According to ANSI SQL standard, a regular identifier is a string of characters that must start with ASCII letter (lower or upper case). The subsequent character can be a combination of letter, digit, underscore (``_``). It cannot be a reversed key word. And whitespace and other special characters are not allowed. Additionally in our SQL parser, we make extension to the rule for Elasticsearch storage as shown in next sub-section.

Extensions
----------

For Elasticsearch, the following identifiers are supported extensionally by our SQL parser for convenience (without the need of being delimited as shown in next section):

1. Identifiers prefixed by dot ``.``: this is called hidden index in Elasticsearch, for example ``.kibana``.
2. Identifiers prefixed by at sign ``@``: this is common for meta fields generated in Logstash ingestion.
3. Identifiers with ``-`` in the middle: this is mostly the case for index name with date information.
4. Identifiers with star ``*`` present: this is mostly an index pattern for wildcard match.

Examples
--------

Here are examples for using index pattern directly without quotes::

    od> SELECT * FROM *cc*nt*;
    fetched rows / total rows = 4/4
    +------------------+-------------+----------------------+-----------+----------+--------+------------+---------+-------+-----------------------+------------+
    | account_number   | firstname   | address              | balance   | gender   | city   | employer   | state   | age   | email                 | lastname   |
    |------------------+-------------+----------------------+-----------+----------+--------+------------+---------+-------+-----------------------+------------|
    | 1                | Amber       | 880 Holmes Lane      | 39225     | M        | Brogan | Pyrami     | IL      | 32    | amberduke@pyrami.com  | Duke       |
    | 6                | Hattie      | 671 Bristol Street   | 5686      | M        | Dante  | Netagy     | TN      | 36    | hattiebond@netagy.com | Bond       |
    | 13               | Nanette     | 789 Madison Street   | 32838     | F        | Nogal  | Quility    | VA      | 28    | null                  | Bates      |
    | 18               | Dale        | 467 Hutchinson Court | 4180      | M        | Orick  | null       | MD      | 33    | daleadams@boink.com   | Adams      |
    +------------------+-------------+----------------------+-----------+----------+--------+------------+---------+-------+-----------------------+------------+


Delimited Identifiers
=====================

Description
-----------

A delimited identifier is an identifier enclosed in back ticks ````` or double quotation marks ``"``. In this case, the identifier enclosed is not necessarily a regular identifier. In other words, it can contain any special character not allowed by regular identifier.

Please note the difference between single quote and double quotes in SQL syntax. Single quote is used to enclose a string literal while double quotes have same purpose as back ticks to escape special characters in an identifier.

Use Cases
---------

Here are typical examples of the use of delimited identifiers:

1. Identifiers of reserved key word name
2. Identifiers with dot ``.`` present: similarly as ``-`` in index name to include date information, it is required to be quoted so parser can differentiate it from identifier with qualifiers.
3. Identifiers with other special character: Elasticsearch has its own rule which allows more special character, for example Unicode character is supported in index name.

Examples
--------

Here are examples for quoting an index name by back ticks::

    od> SELECT * FROM `accounts`;
    fetched rows / total rows = 4/4
    +------------------+-------------+----------------------+-----------+----------+--------+------------+---------+-------+-----------------------+------------+
    | account_number   | firstname   | address              | balance   | gender   | city   | employer   | state   | age   | email                 | lastname   |
    |------------------+-------------+----------------------+-----------+----------+--------+------------+---------+-------+-----------------------+------------|
    | 1                | Amber       | 880 Holmes Lane      | 39225     | M        | Brogan | Pyrami     | IL      | 32    | amberduke@pyrami.com  | Duke       |
    | 6                | Hattie      | 671 Bristol Street   | 5686      | M        | Dante  | Netagy     | TN      | 36    | hattiebond@netagy.com | Bond       |
    | 13               | Nanette     | 789 Madison Street   | 32838     | F        | Nogal  | Quility    | VA      | 28    | null                  | Bates      |
    | 18               | Dale        | 467 Hutchinson Court | 4180      | M        | Orick  | null       | MD      | 33    | daleadams@boink.com   | Adams      |
    +------------------+-------------+----------------------+-----------+----------+--------+------------+---------+-------+-----------------------+------------+


Case Sensitivity
================

Description
-----------

In SQL-92, regular identifiers are case insensitive and converted to upper case automatically just like key word. While characters in a delimited identifier appear as they are. However, in our SQL implementation, identifiers are treated in case sensitive manner. So it must be exactly same as what is stored in Elasticsearch which is different from ANSI standard.

Examples
--------

For example, if you run ``SELECT * FROM ACCOUNTS``, it will end up with an index not found exception from our plugin because the actual index name is under lower case.


Identifier Qualifiers
=====================

Description
-----------

An identifier can be qualified by qualifier(s) or not. The qualifier is meant to avoid ambiguity when interpreting the identifier name. Thus, the name symbol can be associated with a concrete field in Elasticsearch correctly.

In particular, identifier qualifiers follow the specification as below:

1. **Definitions**: A qualified name consists of multiple individual identifiers separated by dot ``.``. An unqualified name can only be a single identifier.
2. **Qualifier types**: For now, index identifier does not support qualification. Field identifier can be qualified by either full index name or its alias specified in ``FROM`` clause.
3. **Delimitation**: If necessary, delimit identifiers in each part of a qualified name separately. Do not enclose the entire name which would be interpreted as a single identifier mistakenly. For example, use ``"table"."column"`` rather than ``"table.column"``.

Examples
--------

The first example is to show a column name qualified by full table name originally in ``FROM`` clause. The qualifier is optional if no ambiguity::

    od> SELECT city, accounts.age, ABS(accounts.balance) FROM accounts WHERE accounts.age < 30;
    fetched rows / total rows = 1/1
    +--------+-------+-------------------------+
    | city   | age   | ABS(accounts.balance)   |
    |--------+-------+-------------------------|
    | Nogal  | 28    | 32838                   |
    +--------+-------+-------------------------+

The second example is to show a field name qualified by index alias specified. Similarly, the alias qualifier is optional in this case::

    od> SELECT city, acc.age, ABS(acc.balance) FROM accounts AS acc WHERE acc.age > 30;
    fetched rows / total rows = 3/3
    +--------+-------+--------------------+
    | city   | age   | ABS(acc.balance)   |
    |--------+-------+--------------------|
    | Brogan | 32    | 39225              |
    | Dante  | 36    | 5686               |
    | Orick  | 33    | 4180               |
    +--------+-------+--------------------+

Note that in both examples above, the qualifier is removed in response. This happens only when identifiers selected is a simple field name. In other cases, expressions rather than an atom field, the column name in response is exactly the same as the text in ``SELECT``clause.
