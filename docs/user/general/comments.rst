
========
Comments
========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Overview
========

ODFE SQL supports ANSI SQL and MySQL style comments, including single-line comments or block comments across multiple lines. Both can be used on separate line(s) or within a SQL statement if needed.


Single-line Comments
====================

A single-line comment starts with either ``#`` or ``--``. All characters in the sequence to the end of the line are commented::

    od> #comments
    ... SELECT
    ... -- comments
    ... 123; -- comments
    fetched rows / total rows = 1/1
    +-------+
    | 123   |
    |-------|
    | 123   |
    +-------+

Note that double-dash style requires at least one whitespace followed.


Block Comments
==============

A block comment is enclosed within ``/*`` and ``*/`` across one or multiple lines. Nested comments are not supported::

    od> /*
    ...  comment1
    ...  comment2
    ... */
    ... SELECT
    ... /* comments */
    ... 123;
    fetched rows / total rows = 1/1
    +-------+
    | 123   |
    |-------|
    | 123   |
    +-------+

Additionally, ``/*! ... */`` is supported though ignored for now. This may be used to support optimization hints in future.
