=================
Lexical Structure
=================

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Introduction
============

Generally lexical structure is defined by the syntax in language grammar. In our case, a SQL input statement, after lexical analysis, is parsed into a sequence of token. Typically each token is separated by whitespace (space or tab). A token can be a string literal, an identifier, a key word or a special character. The sections followed covers each token with more details.

String Literals
===============

TODO

Identifiers
===========

Unquoted Identifiers
--------------------

Basically an identifier can consist of underscore, letter and digit. In the case of Elasticsearch, the following identifiers are supported for convenience without the need of quotes:

1. Index name with ``-``: This is common for index name with date.
2. Index pattern with ``*``: TODO
3. Field name prefixed by ``@``: This is common for meta fields generated in Logstash ingestion.

Quoted Identifiers
------------------

For others with special character, the identifiers are required to be quoted by back ticks ````` or double quotes. Note the the difference between single quote and double quotes in SQL syntax. The former is used to enclose a string literal while the latter is for escaping special characters in an identifier.

Dot ``.`` is another common case for index name though it confuses parser. To use this kind of name in a ``FROM`` clause for example, you need to quote it so parser can differentiate it from a path discussed as below. For example, hidden index name prefixed by ``.`` like ``.kibana`` needs to be quoted.

Identifiers Path
----------------

In addition, period ``.`` can be used to separate multiple identifiers. TODO

Examples
--------

Here is an example for quoting index name by back ticks::

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

Keywords
========

TODO

Comments
========

TODO
