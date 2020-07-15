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

TODO


Regular Identifiers
===================

Description
-----------

According to ANSI SQL standard, a regular identifier can start with ASCII letter (lower or upper case) or digit. The subsequent character can consist of letter, digit, underscore (``_``). It cannot be a reversed key word. And whitespace and other special characters are not allowed. In our SQL parser, we make extension to the rule for Elasticsearch storage as shown in next sub-section.

Extensions
----------

Additionally for Elasticsearch, the following identifiers are supported extensionally by our SQL parser for convenience (without the need of being delimited as shown in next section):

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

Please note the difference between single quote and double quotes in SQL syntax. The former is used to enclose a string literal while the latter is for escaping special characters in an identifier.

Use Cases
---------

Here are typical examples of the use of delimited identifiers:

1. Identifiers of reserved key word name
2. Identifiers with dot ``.`` present: similarly as ``-`` in index name to include date information, it is required to be quoted so parser can differentiate it from a path discussed as below.
3. Identifiers with special character due to gap between ours and Elasticsearch's rule: this is possible because Elasticsearch has its own rule and even support Unicode characters.

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

For example, if you run ``SELECT * FROM ACCOUNTS``, it will end up with an index not found exception from our plugin because actually the index name is under lower case.


Identifier Qualifiers
=====================

For now, we do not support using Elasticsearch cluster name as catalog name to qualify an index name, such as ``my-cluster.logs``.

TODO: field name qualifiers


