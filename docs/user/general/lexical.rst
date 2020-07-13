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

1. Hidden index name prefixed by ``.``: For example a query with ``.kibana`` involved.
2. Index name with ``-``: This is common for index name with date.
3. Index pattern with ``*``: TODO
4. Field name prefixed by ``@``: This is common for meta fields generated in Logstash ingestion.

Quoted Identifiers
------------------

For others with special character, the identifiers are required to be quoted by back ticks ````` or double quotes. Note the the difference between single quote and double quotes in SQL syntax. The former is used to enclose a string literal while the latter is for escaping special characters in an identifier.

Dot ``.`` in the middle is another common case for index name. To use this kind of name in a ``FROM`` clause for example, you need to quote it so parser can differentiate it from a path discussed as below.

Identifiers Path
----------------

In addition, period ``.`` can be used to separate multiple identifiers. TODO

Examples
--------

TODO

Keywords
========

TODO

Comments
========

TODO
