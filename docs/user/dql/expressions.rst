===========
Expressions
===========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


Introduction
============

The expression are expressions that return a scalar value. It can be used in many statements, for example

Literal Value
=============

Description
-----------

A literal is a symbol that represents a value. The most common literal values include:

1. Numeric literals: specify numeric values such as integer and floating-point numbers.
2. String literals: specify a string enclosed by single or double quotes.
3. Boolean literals: `true` or `false`.

Examples
--------

Here is an example for different type of literals::

    od> SELECT 123, 'hello', false, -4.567;
    fetched rows / total rows = 1/1
    +-------+-----------+---------+----------+
    | 123   | "hello"   | false   | -4.567   |
    |-------+-----------+---------+----------|
    | 123   | hello     | False   | -4.567   |
    +-------+-----------+---------+----------+

Limitations
-----------

The current implementation has the following limitations at the moment:

1. Only literals of data types listed as above are supported for now. Other type of literals, such as date and NULL, will be added in future.
2. Expression of literals, such as arithmetic expressions, will be supported later.
3. Standard ANSI `VALUES` clause is not supported, although this is implemented by a Values operator internally.

