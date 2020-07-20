
=============
SQL Functions
=============

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 1

Introduction
============

There is support for a wide variety of SQL functions. We are intend to generate this part of documentation automatically from our type system. However, the type system is missing descriptive information for now. So only formal specifications of all SQL functions supported are listed at the moment. More details will be added in future.

Most of the specifications can be self explained just as a regular function with data type as argument. The only notation that needs elaboration is generic type ``T`` which binds to an actual type and can be used as return type. For example, ``ABS(NUMBER T) -> T`` means function ``ABS`` accepts an numerical argument of type ``T`` which could be any sub-type of ``NUMBER`` type and returns the actual type of ``T`` as return type. The actual type binds to generic type at runtime dynamically.

ABS
===

Description
-----------

Specifications: 

1. ABS(NUMBER T) -> T


ACOS
====

Description
-----------

Specifications: 

1. ACOS(NUMBER T) -> DOUBLE


ADD
===

Description
-----------

Specifications: 

1. ADD(NUMBER T, NUMBER) -> T


ASCII
=====

Description
-----------

Specifications: 

1. ASCII(STRING T) -> INTEGER


ASIN
====

Description
-----------

Specifications: 

1. ASIN(NUMBER T) -> DOUBLE


ATAN
====

Description
-----------

Specifications: 

1. ATAN(NUMBER T) -> DOUBLE


ATAN2
=====

Description
-----------

Specifications: 

1. ATAN2(NUMBER T, NUMBER) -> DOUBLE


CAST
====

Description
-----------

Specification is undefined and type check is skipped for now

CBRT
====

Description
-----------

Specifications: 

1. CBRT(NUMBER T) -> T


CEIL
====

Description
-----------

Specifications: 

1. CEIL(NUMBER T) -> T


CONCAT
======

Description
-----------

Specification is undefined and type check is skipped for now

CONCAT_WS
=========

Description
-----------

Specification is undefined and type check is skipped for now


CONV
====

Description
-----------

Usage: CONV(x, a, b) converts the number x from a base to b base.

Argument type: x: STRING, a: INTEGER, b: INTEGER

Return type: STRING

Example::

    od> SELECT CONV('12', 10, 16), CONV('2C', 16, 10), CONV(12, 10, 2), CONV(1111, 2, 10)
    fetched rows / total rows = 1/1
    +----------------------+----------------------+-------------------+---------------------+
    | conv("12", 10, 16)   | conv("2C", 16, 10)   | conv(12, 10, 2)   | conv(1111, 2, 10)   |
    |----------------------+----------------------+-------------------+---------------------|
    | c                    | 44                   | 1100              | 15                  |
    +----------------------+----------------------+-------------------+---------------------+

COS
===

Description
-----------

Specifications: 

1. COS(NUMBER T) -> DOUBLE


COSH
====

Description
-----------

Specifications: 

1. COSH(NUMBER T) -> DOUBLE


COT
===

Description
-----------

Specifications: 

1. COT(NUMBER T) -> DOUBLE


CRC32
=====

Description
-----------

Usage: Calculates a cyclic redundancy check value and returns a 32-bit unsigned value.

Argument type: STRING

Return type: LONG

Example::

    od> SELECT CRC32('MySQL')
    fetched rows / total rows = 1/1
    +------------------+
    | crc32("MySQL")   |
    |------------------|
    | 3259397556       |
    +------------------+


CURDATE
=======

Description
-----------

Specifications: 

1. CURDATE() -> DATE


DATE
====

Description
-----------

Specifications: 

1. DATE(DATE) -> DATE


DATE_FORMAT
===========

Description
-----------

Specifications: 

1. DATE_FORMAT(DATE, STRING) -> STRING
2. DATE_FORMAT(DATE, STRING, STRING) -> STRING


DAYOFMONTH
==========

Description
-----------

Specifications: 

1. DAYOFMONTH(DATE) -> INTEGER


DEGREES
=======

Description
-----------

Specifications: 

1. DEGREES(NUMBER T) -> DOUBLE


DIVIDE
======

Description
-----------

Specifications: 

1. DIVIDE(NUMBER T, NUMBER) -> T


E
=

Description
-----------

Specifications: 

1. E() -> DOUBLE


EXP
===

Description
-----------

Specifications: 

1. EXP(NUMBER T) -> T


EXPM1
=====

Description
-----------

Specifications: 

1. EXPM1(NUMBER T) -> T


FLOOR
=====

Description
-----------

Specifications: 

1. FLOOR(NUMBER T) -> T


IF
==

Description
-----------

Specifications: 

1. IF(BOOLEAN, ES_TYPE, ES_TYPE) -> ES_TYPE


IFNULL
======

Description
-----------

Specifications: 

1. IFNULL(ES_TYPE, ES_TYPE) -> ES_TYPE


ISNULL
======

Description
-----------

Specifications: 

1. ISNULL(ES_TYPE) -> INTEGER


LEFT
====

Description
-----------

Specifications: 

1. LEFT(STRING T, INTEGER) -> T


LENGTH
======

Description
-----------

Specifications: 

1. LENGTH(STRING) -> INTEGER


LN
==

Description
-----------

Specifications: 

1. LN(NUMBER T) -> DOUBLE


LOCATE
======

Description
-----------

Specifications: 

1. LOCATE(STRING, STRING, INTEGER) -> INTEGER
2. LOCATE(STRING, STRING) -> INTEGER


LOG
===

Description
-----------

Specifications: 

1. LOG(NUMBER T) -> DOUBLE
2. LOG(NUMBER T, NUMBER) -> DOUBLE


LOG2
====

Description
-----------

Specifications: 

1. LOG2(NUMBER T) -> DOUBLE


LOG10
=====

Description
-----------

Specifications: 

1. LOG10(NUMBER T) -> DOUBLE


LOWER
=====

Description
-----------

Specifications: 

1. LOWER(STRING T) -> T
2. LOWER(STRING T, STRING) -> T


LTRIM
=====

Description
-----------

Specifications: 

1. LTRIM(STRING T) -> T


MAKETIME
========

Description
-----------

Specifications: 

1. MAKETIME(INTEGER, INTEGER, INTEGER) -> DATE


MOD
=======

Description
-----------

Usage: MOD(n, m) calculates the remainder of the number n divided by m.

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type: Wider type between types of n and m if m is nonzero value. If m equals to 0, then returns NULL.

Example::

    od> SELECT MOD(3, 2), MOD(3.1, 2)
    fetched rows / total rows = 1/1
    +-------------+---------------+
    | mod(3, 2)   | mod(3.1, 2)   |
    |-------------+---------------|
    | 1           | 1.1           |
    +-------------+---------------+


MONTH
=====

Description
-----------

Specifications: 

1. MONTH(DATE) -> INTEGER


MONTHNAME
=========

Description
-----------

Specifications: 

1. MONTHNAME(DATE) -> STRING


MULTIPLY
========

Description
-----------

Specifications: 

1. MULTIPLY(NUMBER T, NUMBER) -> NUMBER


NOW
===

Description
-----------

Specifications: 

1. NOW() -> DATE


PI
==

Description
-----------

Specifications: 

1. PI() -> DOUBLE


POW
===

Description
-----------

Usage: POW(x, y) calculates the value of x raised to the power of y. Bad inputs return NULL result.

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type: DOUBLE

Example::

    od> SELECT POW(3, 2), POW(-3, 2), POW(3, -2)
    fetched rows / total rows = 1/1
    +-------------+--------------+--------------------+
    | pow(3, 2)   | pow(-3, 2)   | pow(3, -2)         |
    |-------------+--------------+--------------------|
    | 9           | 9            | 0.1111111111111111 |
    +-------------+--------------+--------------------+


POWER
=====

Description
-----------

Usage: POWER(x, y) calculates the value of x raised to the power of y. Bad inputs return NULL result.

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type: DOUBLE

Example::

    od> SELECT POWER(3, 2), POWER(-3, 2), POWER(3, -2)
    fetched rows / total rows = 1/1
    +---------------+----------------+--------------------+
    | power(3, 2)   | power(-3, 2)   | power(3, -2)       |
    |---------------+----------------+--------------------|
    | 9             | 9              | 0.1111111111111111 |
    +---------------+----------------+--------------------+


RADIANS
=======

Description
-----------

Specifications: 

1. RADIANS(NUMBER T) -> DOUBLE


RAND
====

Description
-----------

Specifications: 

1. RAND() -> NUMBER
2. RAND(NUMBER T) -> T


REPLACE
=======

Description
-----------

Specifications: 

1. REPLACE(STRING T, STRING, STRING) -> T


RIGHT
=====

Description
-----------

Specifications: 

1. RIGHT(STRING T, INTEGER) -> T


RINT
====

Description
-----------

Specifications: 

1. RINT(NUMBER T) -> T


ROUND
=====

Description
-----------

Usage: ROUND(x, d) rounds the argument x to d decimal places, d defaults to 0 if not specified

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type map:

(INTEGER/LONG [,INTEGER]) -> LONG
(FLOAT/DOUBLE [,INTEGER]) -> LONG

Example::

    od> SELECT ROUND(12.34), ROUND(12.34, 1), ROUND(12.34, -1), ROUND(12, 1)
    fetched rows / total rows = 1/1
    +----------------+-------------------+--------------------+----------------+
    | round(12.34)   | round(12.34, 1)   | round(12.34, -1)   | round(12, 1)   |
    |----------------+-------------------+--------------------+----------------|
    | 12             | 12.3              | 10                 | 12             |
    +----------------+-------------------+--------------------+----------------+


RTRIM
=====

Description
-----------

Specifications: 

1. RTRIM(STRING T) -> T


SIGN
====

Description
-----------

Usage: Returns the sign of the argument as -1, 0, or 1, depending on whether the number is negative, zero, or positive

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type: INTEGER

Example::

    od> SELECT SIGN(1), SIGN(0), SIGN(-1.1)
    fetched rows / total rows = 1/1
    +-----------+-----------+--------------+
    | sign(1)   | sign(0)   | sign(-1.1)   |
    |-----------+-----------+--------------|
    | 1         | 0         | -1           |
    +-----------+-----------+--------------+


SIGNUM
======

Description
-----------

Specifications: 

1. SIGNUM(NUMBER T) -> T


SIN
===

Description
-----------

Specifications: 

1. SIN(NUMBER T) -> DOUBLE


SINH
====

Description
-----------

Specifications: 

1. SINH(NUMBER T) -> DOUBLE


SQRT
====

Description
-----------

Usage: Calculates the square root of a non-negative number

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type map:

(Non-negative) INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
(Negative) INTEGER/LONG/FLOAT/DOUBLE -> NULL

Example::

    od> SELECT SQRT(4), SQRT(4.41)
    fetched rows / total rows = 1/1
    +-----------+--------------+
    | sqrt(4)   | sqrt(4.41)   |
    |-----------+--------------|
    | 2         | 2.1          |
    +-----------+--------------+


SUBSTRING
=========

Description
-----------

Specifications: 

1. SUBSTRING(STRING T, INTEGER, INTEGER) -> T


SUBTRACT
========

Description
-----------

Specifications: 

1. SUBTRACT(NUMBER T, NUMBER) -> T


TAN
===

Description
-----------

Specifications: 

1. TAN(NUMBER T) -> DOUBLE


TIMESTAMP
=========

Description
-----------

Specifications: 

1. TIMESTAMP(DATE) -> DATE


TRIM
====

Description
-----------

Specifications: 

1. TRIM(STRING T) -> T


TRUNCATE
========

Description
-----------

Usage: TRUNCATE(x, d) returns the number x, truncated to d decimal place

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type map:

INTEGER/LONG -> LONG
FLOAT/DOUBLE -> DOUBLE

Example::

    fetched rows / total rows = 1/1
    +----------------------+-----------------------+-------------------+
    | truncate(56.78, 1)   | truncate(56.78, -1)   | truncate(56, 1)   |
    |----------------------+-----------------------+-------------------|
    | 56.7                 | 50                    | 56                |
    +----------------------+-----------------------+-------------------+


UPPER
=====

Description
-----------

Specifications: 

1. UPPER(STRING T) -> T
2. UPPER(STRING T, STRING) -> T


YEAR
====

Description
-----------

Specifications: 

1. YEAR(DATE) -> INTEGER


