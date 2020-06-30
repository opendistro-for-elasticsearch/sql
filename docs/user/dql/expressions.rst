===========
Expressions
===========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 3


Introduction
============

Expressions, particularly value expressions, are those which return a scalar value. Expressions have different types and forms. For example, there are literal values as atom expression and arithmetic, predicate and function expression built on top of them. And also expressions can be used in different clauses, such as using arithmetic expression in ``SELECT``, ``WHERE`` or ``HAVING`` clause.

Literal Values
==============

Description
-----------

A literal is a symbol that represents a value. The most common literal values include:

1. Numeric literals: specify numeric values such as integer and floating-point numbers.
2. String literals: specify a string enclosed by single or double quotes.
3. Boolean literals: ``true`` or ``false``.

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
3. Standard ANSI ``VALUES`` clause is not supported, although the ``SELECT`` literal example above is implemented by a Values operator internally.

Arithmetic Expressions
======================

Description
-----------

Operators
`````````

Arithmetic expression is an expression formed by numeric literals and binary arithmetic operators as follows:

1. ``+``: Add.
2. ``-``: Subtract.
3. ``*``: Multiply.
4. ``/``: Divide. For integers, the result is an integer with fractional part discarded.
5. ``%``: Modulo. This can be used with integers only with remainder of the division as result.

Precedence
``````````

Parentheses can be used to control the precedence of arithmetic operators. Otherwise, operators of higher precedence is performed first.

Type Conversion
```````````````

Implicit type conversion is performed when looking up operator signature. For example, an integer ``+`` a real number matches signature ``+(double,double)`` which results in a real number. This rule also applies to function call discussed below.

Examples
--------

Here is an example for different type of arithmetic expressions::

    od> SELECT 1 + 2, (9 - 1) % 3, 2 * 4 / 3;
    fetched rows / total rows = 1/1
    +---------+-------------+-------------+
    | 1 + 2   | 9 - 1 % 3   | 2 * 4 / 3   |
    |---------+-------------+-------------|
    | 3       | 2           | 2           |
    +---------+-------------+-------------+


Function Call
=============

Description
-----------

A function call is declared by function name followed by its arguments. The arguments are enclosed in parentheses and separated by comma. For complete function list supported, please see also: `SQL Functions <functions.rst>`_

Syntax
``````

A typical function call is in the following form::

 function_name ( [ expression [, expression]* ]? )

Null Handling
`````````````

TODO

Examples
--------

Here is an example for different type of arithmetic expressions::

    od> SELECT abs(-1.234), abs(-1 * abs(-5));
    fetched rows / total rows = 1/1
    +---------------+---------------------+
    | abs(-1.234)   | abs(-1 * abs(-5))   |
    |---------------+---------------------|
    | 1.234         | 5                   |
    +---------------+---------------------+

Limitations
-----------

1. Only a subset of the SQL functions above is implemented in new engine for now. More function support are being added.
2. For now function name is required to be lowercase.

