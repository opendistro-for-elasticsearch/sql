===========
Identifiers
===========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Introduction
============

Lexical structure is defined by the syntax in language grammar. After lexical analysis, a SQL input statement is parsed into a sequence of token. In original text, each token is typically separated by whitespace (space or tab). There are different types of token, such as string literal, identifier, key word or special character. The sections followed will cover each type of token with more details.


Regular Identifiers
===================

Description
-----------

According to ANSI SQL-92 standard, a regular identifier can start with lower or upper ASCII letter, digit. The subsequent character can consist of lower or upper ASCII letter, digit, underscore (``_``). It cannot be a reversed key word. And whitespace and special characters (except what's shown in next sub-section) are not allowed.

Special Cases
-------------

Additionally for Elasticsearch, the following identifiers are supported by our SQL parser for convenience without the need of delimiting:

1. Identifiers prefixed by dot ``.``: this is called hidden index in Elasticsearch, for example ``.kibana``.
2. Identifier prefixed by at sign ``@``: this is common for meta fields generated in Logstash ingestion.
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

A delimited identifier is an identifier enclosed in back ticks ````` or double quotes ``"``. In this case, the identifier enclosed is not necessarily a regular identifier. In other words, it can contain any special character not allowed by regular identifier.

Please note the difference between single quote and double quotes in SQL syntax. The former is used to enclose a string literal while the latter is for escaping special characters in an identifier.

Use Cases
---------

Here are typical examples of the use of delimited identifiers:

1. Identifiers of reserved key word name
2. Identifiers with dot ``.`` present: similarly as ``-`` in index name to include date information, it is required to be quoted so parser can differentiate it from a path discussed as below.

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

In SQL-92, regular identifiers are case insensitive and converted to upper case automatically just like key word. While characters in a delimited identifier appear as they are. In our SQL implementation, identifiers are treated in case sensitive manner. So it must be exactly same as what is stored in Elasticsearch which is different from ANSI standard.

Examples
--------

Here is an example to show you index name is case sensitive:


Qualified Identifiers
=====================

In addition, period ``.`` can be used to separate multiple identifiers. TODO



