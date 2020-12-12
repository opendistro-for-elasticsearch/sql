=========
Functions
=========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2

Introduction
============

There is support for a wide variety of functions shared by SQL/PPL. We are intend to generate this part of documentation automatically from our type system. However, the type system is missing descriptive information for now. So only formal specifications of all functions supported are listed at the moment. More details will be added in future.

Most of the specifications can be self explained just as a regular function with data type as argument. The only notation that needs elaboration is generic type ``T`` which binds to an actual type and can be used as return type. For example, ``ABS(NUMBER T) -> T`` means function ``ABS`` accepts an numerical argument of type ``T`` which could be any sub-type of ``NUMBER`` type and returns the actual type of ``T`` as return type. The actual type binds to generic type at runtime dynamically.


Type Conversion
===============

CAST
----

Description
>>>>>>>>>>>

Usage: cast(expr as dateType) cast the expr to dataType. return the value of dataType. The following conversion rules are used:

+------------+--------+--------+---------+-------------+--------+--------+
| Src/Target | STRING | NUMBER | BOOLEAN | TIMESTAMP   | DATE   | TIME   |
+------------+--------+--------+---------+-------------+--------+--------+
| STRING     |        | Note1  | Note1   | TIMESTAMP() | DATE() | TIME() |
+------------+--------+--------+---------+-------------+--------+--------+
| NUMBER     | Note1  |        | v!=0    | N/A         | N/A    | N/A    |
+------------+--------+--------+---------+-------------+--------+--------+
| BOOLEAN    | Note1  | v?1:0  |         | N/A         | N/A    | N/A    |
+------------+--------+--------+---------+-------------+--------+--------+
| TIMESTAMP  | Note1  | N/A    | N/A     |             | DATE() | TIME() |
+------------+--------+--------+---------+-------------+--------+--------+
| DATE       | Note1  | N/A    | N/A     | N/A         |        | N/A    |
+------------+--------+--------+---------+-------------+--------+--------+
| TIME       | Note1  | N/A    | N/A     | N/A         | N/A    |        |
+------------+--------+--------+---------+-------------+--------+--------+

Note1: the conversion follow the JDK specification.

Cast to string example::

    od> SELECT cast(true as string) as cbool, cast(1 as string) as cint, cast(DATE '2012-08-07' as string) as cdate
    fetched rows / total rows = 1/1
    +---------+--------+------------+
    | cbool   | cint   | cdate      |
    |---------+--------+------------|
    | true    | 1      | 2012-08-07 |
    +---------+--------+------------+

Cast to number example::

    od> SELECT cast(true as int) as cbool, cast('1' as int) as cstring
    fetched rows / total rows = 1/1
    +---------+-----------+
    | cbool   | cstring   |
    |---------+-----------|
    | 1       | 1         |
    +---------+-----------+

Cast to date example::

    od> SELECT cast('2012-08-07' as date) as cdate, cast('01:01:01' as time) as ctime, cast('2012-08-07 01:01:01' as timestamp) as ctimestamp
    fetched rows / total rows = 1/1
    +------------+----------+---------------------+
    | cdate      | ctime    | ctimestamp          |
    |------------+----------+---------------------|
    | 2012-08-07 | 01:01:01 | 2012-08-07 01:01:01 |
    +------------+----------+---------------------+

Cast function can be chained::

    od> SELECT cast(cast(true as string) as boolean) as cbool
    fetched rows / total rows = 1/1
    +---------+
    | cbool   |
    |---------|
    | True    |
    +---------+


Mathematical Functions
======================

ABS
---

Description
>>>>>>>>>>>

Specifications:

1. ABS(NUMBER T) -> T


ACOS
----

Description
>>>>>>>>>>>

Usage: acos(x) calculate the arc cosine of x. Returns NULL if x is not in the range -1 to 1.

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type: DOUBLE

Example::

    od> SELECT ACOS(0)
    fetched rows / total rows = 1/1
    +--------------------+
    | ACOS(0)            |
    |--------------------|
    | 1.5707963267948966 |
    +--------------------+


ADD
---

Description
>>>>>>>>>>>

Specifications:

1. ADD(NUMBER T, NUMBER) -> T


ASIN
----

Description
>>>>>>>>>>>

Usage: asin(x) calculate the arc sine of x. Returns NULL if x is not in the range -1 to 1.

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type: DOUBLE

Example::

    od> SELECT ASIN(0)
    fetched rows / total rows = 1/1
    +-----------+
    | ASIN(0)   |
    |-----------|
    | 0.0       |
    +-----------+


ATAN
----

Description
>>>>>>>>>>>

Usage: atan(x) calculates the arc tangent of x. atan(y, x) calculates the arc tangent of y / x, except that the signs of both arguments are used to determine the quadrant of the result.

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type: DOUBLE

Example::

    od> SELECT ATAN(2), ATAN(2, 3)
    fetched rows / total rows = 1/1
    +--------------------+--------------------+
    | ATAN(2)            | ATAN(2, 3)         |
    |--------------------+--------------------|
    | 1.1071487177940904 | 0.5880026035475675 |
    +--------------------+--------------------+


ATAN2
-----

Description
>>>>>>>>>>>

Usage: atan2(y, x) calculates the arc tangent of y / x, except that the signs of both arguments are used to determine the quadrant of the result.

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type: DOUBLE

Example::

    od> SELECT ATAN2(2, 3)
    fetched rows / total rows = 1/1
    +--------------------+
    | ATAN2(2, 3)        |
    |--------------------|
    | 0.5880026035475675 |
    +--------------------+


CBRT
----

Description
>>>>>>>>>>>

Specifications:

1. CBRT(NUMBER T) -> T


CEIL
----

Description
>>>>>>>>>>>

Specifications:

1. CEIL(NUMBER T) -> T


CONV
----

Description
>>>>>>>>>>>

Usage: CONV(x, a, b) converts the number x from a base to b base.

Argument type: x: STRING, a: INTEGER, b: INTEGER

Return type: STRING

Example::

    od> SELECT CONV('12', 10, 16), CONV('2C', 16, 10), CONV(12, 10, 2), CONV(1111, 2, 10)
    fetched rows / total rows = 1/1
    +----------------------+----------------------+-------------------+---------------------+
    | CONV('12', 10, 16)   | CONV('2C', 16, 10)   | CONV(12, 10, 2)   | CONV(1111, 2, 10)   |
    |----------------------+----------------------+-------------------+---------------------|
    | c                    | 44                   | 1100              | 15                  |
    +----------------------+----------------------+-------------------+---------------------+

COS
---

Description
>>>>>>>>>>>

Usage: cos(x) calculate the cosine of x, where x is given in radians.

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type: DOUBLE

Example::

    od> SELECT COS(0)
    fetched rows / total rows = 1/1
    +----------+
    | COS(0)   |
    |----------|
    | 1.0      |
    +----------+


COSH
----

Description
>>>>>>>>>>>

Specifications:

1. COSH(NUMBER T) -> DOUBLE


COT
---

Description
>>>>>>>>>>>

Usage: cot(x) calculate the cotangent of x. Returns out-of-range error if x equals to 0.

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type: DOUBLE

Example::

    od> SELECT COT(1)
    fetched rows / total rows = 1/1
    +--------------------+
    | COT(1)             |
    |--------------------|
    | 0.6420926159343306 |
    +--------------------+


CRC32
-----

Description
>>>>>>>>>>>

Usage: Calculates a cyclic redundancy check value and returns a 32-bit unsigned value.

Argument type: STRING

Return type: LONG

Example::

    od> SELECT CRC32('MySQL')
    fetched rows / total rows = 1/1
    +------------------+
    | CRC32('MySQL')   |
    |------------------|
    | 3259397556       |
    +------------------+


DEGREES
-------

Description
>>>>>>>>>>>

Usage: degrees(x) converts x from radians to degrees.

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type: DOUBLE

Example::

    od> SELECT DEGREES(1.57)
    fetched rows / total rows  = 1/1
    +-------------------+
    | DEGREES(1.57)     |
    |-------------------|
    | 89.95437383553924 |
    +-------------------+


DIVIDE
------

Description
>>>>>>>>>>>

Specifications:

1. DIVIDE(NUMBER T, NUMBER) -> T


E
-

Description
>>>>>>>>>>>

Usage: E() returns the Euler's number

Return type: DOUBLE

Example::

    od> SELECT E()
    fetched rows / total rows = 1/1
    +-------------------+
    | E()               |
    |-------------------|
    | 2.718281828459045 |
    +-------------------+


EXP
---

Description
>>>>>>>>>>>

Specifications:

1. EXP(NUMBER T) -> T


EXPM1
-----

Description
>>>>>>>>>>>

Specifications:

1. EXPM1(NUMBER T) -> T


FLOOR
-----

Description
>>>>>>>>>>>

Specifications:

1. FLOOR(NUMBER T) -> T


LN
--

Description
>>>>>>>>>>>

Specifications:

1. LN(NUMBER T) -> DOUBLE


LOG
---

Description
>>>>>>>>>>>

Specifications:

1. LOG(NUMBER T) -> DOUBLE
2. LOG(NUMBER T, NUMBER) -> DOUBLE


LOG2
----

Description
>>>>>>>>>>>

Specifications:

1. LOG2(NUMBER T) -> DOUBLE


LOG10
-----

Description
>>>>>>>>>>>

Specifications:

1. LOG10(NUMBER T) -> DOUBLE


MOD
---

Description
>>>>>>>>>>>

Usage: MOD(n, m) calculates the remainder of the number n divided by m.

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type: Wider type between types of n and m if m is nonzero value. If m equals to 0, then returns NULL.

Example::

    od> SELECT MOD(3, 2), MOD(3.1, 2)
    fetched rows / total rows = 1/1
    +-------------+---------------+
    | MOD(3, 2)   | MOD(3.1, 2)   |
    |-------------+---------------|
    | 1           | 1.1           |
    +-------------+---------------+


MULTIPLY
--------

Description
>>>>>>>>>>>

Specifications:

1. MULTIPLY(NUMBER T, NUMBER) -> NUMBER

PI
--

Description
>>>>>>>>>>>

Usage: PI() returns the constant pi

Return type: DOUBLE

Example::

    od> SELECT PI()
    fetched rows / total rows = 1/1
    +-------------------+
    | PI()              |
    |-------------------|
    | 3.141592653589793 |
    +-------------------+


POW
---

Description
>>>>>>>>>>>

Usage: POW(x, y) calculates the value of x raised to the power of y. Bad inputs return NULL result.

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type: DOUBLE

Synonyms: `POWER`_

Example::

    od> SELECT POW(3, 2), POW(-3, 2), POW(3, -2)
    fetched rows / total rows = 1/1
    +-------------+--------------+--------------------+
    | POW(3, 2)   | POW(-3, 2)   | POW(3, -2)         |
    |-------------+--------------+--------------------|
    | 9.0         | 9.0          | 0.1111111111111111 |
    +-------------+--------------+--------------------+


POWER
-----

Description
>>>>>>>>>>>

Usage: POWER(x, y) calculates the value of x raised to the power of y. Bad inputs return NULL result.

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type: DOUBLE

Synonyms: `POW`_

Example::

    od> SELECT POWER(3, 2), POWER(-3, 2), POWER(3, -2)
    fetched rows / total rows = 1/1
    +---------------+----------------+--------------------+
    | POWER(3, 2)   | POWER(-3, 2)   | POWER(3, -2)       |
    |---------------+----------------+--------------------|
    | 9.0           | 9.0            | 0.1111111111111111 |
    +---------------+----------------+--------------------+


RADIANS
-------

Description
>>>>>>>>>>>

Usage: radians(x) converts x from degrees to radians.

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type: DOUBLE

Example::

    od> SELECT RADIANS(90)
    fetched rows / total rows  = 1/1
    +--------------------+
    | RADIANS(90)        |
    |--------------------|
    | 1.5707963267948966 |
    +--------------------+


RAND
----

Description
>>>>>>>>>>>

Usage: RAND()/RAND(N) returns a random floating-point value in the range 0 <= value < 1.0. If integer N is specified, the seed is initialized prior to execution. One implication of this behavior is with identical argument N, rand(N) returns the same value each time, and thus produces a repeatable sequence of column values.

Argument type: INTEGER

Return type: FLOAT

Example::

    od> SELECT RAND(3)
    fetched rows / total rows = 1/1
    +------------+
    | RAND(3)    |
    |------------|
    | 0.73105735 |
    +------------+


RINT
----

Description
>>>>>>>>>>>

Specifications:

1. RINT(NUMBER T) -> T


ROUND
-----

Description
>>>>>>>>>>>

Usage: ROUND(x, d) rounds the argument x to d decimal places, d defaults to 0 if not specified

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type map:

(INTEGER/LONG [,INTEGER]) -> LONG
(FLOAT/DOUBLE [,INTEGER]) -> LONG

Example::

    od> SELECT ROUND(12.34), ROUND(12.34, 1), ROUND(12.34, -1), ROUND(12, 1)
    fetched rows / total rows = 1/1
    +----------------+-------------------+--------------------+----------------+
    | ROUND(12.34)   | ROUND(12.34, 1)   | ROUND(12.34, -1)   | ROUND(12, 1)   |
    |----------------+-------------------+--------------------+----------------|
    | 12.0           | 12.3              | 10.0               | 12             |
    +----------------+-------------------+--------------------+----------------+


SIGN
----

Description
>>>>>>>>>>>

Usage: Returns the sign of the argument as -1, 0, or 1, depending on whether the number is negative, zero, or positive

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type: INTEGER

Example::

    od> SELECT SIGN(1), SIGN(0), SIGN(-1.1)
    fetched rows / total rows = 1/1
    +-----------+-----------+--------------+
    | SIGN(1)   | SIGN(0)   | SIGN(-1.1)   |
    |-----------+-----------+--------------|
    | 1         | 0         | -1           |
    +-----------+-----------+--------------+


SIGNUM
------

Description
>>>>>>>>>>>

Specifications:

1. SIGNUM(NUMBER T) -> T


SIN
---

Description
>>>>>>>>>>>

Usage: sin(x) calculate the sine of x, where x is given in radians.

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type: DOUBLE

Example::

    od> SELECT SIN(0)
    fetched rows / total rows = 1/1
    +----------+
    | SIN(0)   |
    |----------|
    | 0.0      |
    +----------+


SINH
----

Description
>>>>>>>>>>>

Specifications:

1. SINH(NUMBER T) -> DOUBLE


SQRT
----

Description
>>>>>>>>>>>

Usage: Calculates the square root of a non-negative number

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type map:

(Non-negative) INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
(Negative) INTEGER/LONG/FLOAT/DOUBLE -> NULL

Example::

    od> SELECT SQRT(4), SQRT(4.41)
    fetched rows / total rows = 1/1
    +-----------+--------------+
    | SQRT(4)   | SQRT(4.41)   |
    |-----------+--------------|
    | 2.0       | 2.1          |
    +-----------+--------------+


STRCMP
------

Description
>>>>>>>>>>>

Usage: strcmp(str1, str2) returns 0 if strings are same, -1 if first arg < second arg according to current sort order, and 1 otherwise.

Argument type: STRING, STRING

Return type: INTEGER

Example::

    od> SELECT STRCMP('hello', 'world'), STRCMP('hello', 'hello')
    fetched rows / total rows = 1/1
    +----------------------------+----------------------------+
    | STRCMP('hello', 'world')   | STRCMP('hello', 'hello')   |
    |----------------------------+----------------------------|
    | -1                         | 0                          |
    +----------------------------+----------------------------+



SUBTRACT
--------

Description
>>>>>>>>>>>

Specifications:

1. SUBTRACT(NUMBER T, NUMBER) -> T


TAN
---

Description
>>>>>>>>>>>

Usage: tan(x) calculate the tangent of x, where x is given in radians.

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type: DOUBLE

Example::

    od> SELECT TAN(0)
    fetched rows / total rows = 1/1
    +----------+
    | TAN(0)   |
    |----------|
    | 0.0      |
    +----------+


TRUNCATE
--------

Description
>>>>>>>>>>>

Usage: TRUNCATE(x, d) returns the number x, truncated to d decimal place

Argument type: INTEGER/LONG/FLOAT/DOUBLE

Return type map:

INTEGER/LONG -> LONG
FLOAT/DOUBLE -> DOUBLE

Example::

    fetched rows / total rows = 1/1
    +----------------------+-----------------------+-------------------+
    | TRUNCATE(56.78, 1)   | TRUNCATE(56.78, -1)   | TRUNCATE(56, 1)   |
    |----------------------+-----------------------+-------------------|
    | 56.7                 | 50                    | 56                |
    +----------------------+-----------------------+-------------------+



Date and Time Functions
=======================

ADDDATE
-------

Description
>>>>>>>>>>>

Usage: adddate(date, INTERVAL expr unit)/ adddate(date, expr) adds the time interval of second argument to date; adddate(date, days) adds the second argument as integer number of days to date.

Argument type: DATE/DATETIME/TIMESTAMP/STRING, INTERVAL/LONG

Return type map:

(DATE/DATETIME/TIMESTAMP/STRING, INTERVAL) -> DATETIME

(DATE, LONG) -> DATE

(DATETIME/TIMESTAMP/STRING, LONG) -> DATETIME

Synonyms: `DATE_ADD`_

Example::

    od> SELECT ADDDATE(DATE('2020-08-26'), INTERVAL 1 HOUR), ADDDATE(DATE('2020-08-26'), 1), ADDDATE(TIMESTAMP('2020-08-26 01:01:01'), 1)
    fetched rows / total rows = 1/1
    +------------------------------------------------+----------------------------------+------------------------------------------------+
    | ADDDATE(DATE('2020-08-26'), INTERVAL 1 HOUR)   | ADDDATE(DATE('2020-08-26'), 1)   | ADDDATE(TIMESTAMP('2020-08-26 01:01:01'), 1)   |
    |------------------------------------------------+----------------------------------+------------------------------------------------|
    | 2020-08-26 01:00:00                            | 2020-08-27                       | 2020-08-27 01:01:01                            |
    +------------------------------------------------+----------------------------------+------------------------------------------------+


CURDATE
-------

Description
>>>>>>>>>>>

Specifications:

1. CURDATE() -> DATE


DATE
----

Description
>>>>>>>>>>>

Usage: date(expr) constructs a date type with the input string expr as a date. If the argument is of date/datetime/timestamp, it extracts the date value part from the expression.

Argument type: STRING/DATE/DATETIME/TIMESTAMP

Return type: DATE

Example::

    >od SELECT DATE('2020-08-26'), DATE(TIMESTAMP('2020-08-26 13:49:00'))
    fetched rows / total rows = 1/1
    +----------------------+------------------------------------------+
    | DATE('2020-08-26')   | DATE(TIMESTAMP('2020-08-26 13:49:00'))   |
    |----------------------+------------------------------------------|
    | DATE '2020-08-26'    | DATE '2020-08-26'                        |
    +----------------------+------------------------------------------+


DATE_ADD
--------

Description
>>>>>>>>>>>

Usage: date_add(date, INTERVAL expr unit)/ date_add(date, expr) adds the time interval expr to date

Argument type: DATE/DATETIME/TIMESTAMP/STRING, INTERVAL/LONG

Return type map:

DATE/DATETIME/TIMESTAMP/STRING, INTERVAL -> DATETIME

DATE, LONG -> DATE

DATETIME/TIMESTAMP/STRING, LONG -> DATETIME

Synonyms: `ADDDATE`_

Example::

    od> SELECT DATE_ADD(DATE('2020-08-26'), INTERVAL 1 HOUR), DATE_ADD(DATE('2020-08-26'), 1), DATE_ADD(TIMESTAMP('2020-08-26 01:01:01'), 1)
    fetched rows / total rows = 1/1
    +-------------------------------------------------+-----------------------------------+-------------------------------------------------+
    | DATE_ADD(DATE('2020-08-26'), INTERVAL 1 HOUR)   | DATE_ADD(DATE('2020-08-26'), 1)   | DATE_ADD(TIMESTAMP('2020-08-26 01:01:01'), 1)   |
    |-------------------------------------------------+-----------------------------------+-------------------------------------------------|
    | 2020-08-26 01:00:00                             | 2020-08-27                        | 2020-08-27 01:01:01                             |
    +-------------------------------------------------+-----------------------------------+-------------------------------------------------+


DATE_FORMAT
-----------

Description
>>>>>>>>>>>

Usage: date_format(date, format) formats the date argument using the specifiers in the format argument.

.. list-table:: The following table describes the available specifier arguments.
   :widths: 20 80
   :header-rows: 1

   * - Specifier
     - Description
   * - %a
     - Abbreviated weekday name (Sun..Sat)
   * - %b
     - Abbreviated month name (Jan..Dec)
   * - %c
     - Month, numeric (0..12)
   * - %D
     - Day of the month with English suffix (0th, 1st, 2nd, 3rd, …)
   * - %d
     - Day of the month, numeric (00..31)
   * - %e
     - Day of the month, numeric (0..31)
   * - %f
     - Microseconds (000000..999999)
   * - %H
     - Hour (00..23)
   * - %h
     - Hour (01..12)
   * - %I
     - Hour (01..12)
   * - %i
     - Minutes, numeric (00..59)
   * - %j
     - Day of year (001..366)
   * - %k
     - Hour (0..23)
   * - %l
     - Hour (1..12)
   * - %M
     - Month name (January..December)
   * - %m
     - Month, numeric (00..12)
   * - %p
     - AM or PM
   * - %r
     - Time, 12-hour (hh:mm:ss followed by AM or PM)
   * - %S
     - Seconds (00..59)
   * - %s
     - Seconds (00..59)
   * - %T
     - Time, 24-hour (hh:mm:ss)
   * - %U
     - Week (00..53), where Sunday is the first day of the week; WEEK() mode 0
   * - %u
     - Week (00..53), where Monday is the first day of the week; WEEK() mode 1
   * - %V
     - Week (01..53), where Sunday is the first day of the week; WEEK() mode 2; used with %X
   * - %v
     - Week (01..53), where Monday is the first day of the week; WEEK() mode 3; used with %x
   * - %W
     - Weekday name (Sunday..Saturday)
   * - %w
     - Day of the week (0=Sunday..6=Saturday)
   * - %X
     - Year for the week where Sunday is the first day of the week, numeric, four digits; used with %V
   * - %x
     - Year for the week, where Monday is the first day of the week, numeric, four digits; used with %v
   * - %Y
     - Year, numeric, four digits
   * - %y
     - Year, numeric (two digits)
   * - %%
     - A literal % character
   * - %x
     - x, for any “x” not listed above

Argument type: STRING/DATE/DATETIME/TIMESTAMP, STRING

Return type: STRING

Example::

    >od SELECT DATE_FORMAT('1998-01-31 13:14:15.012345', '%T.%f'), DATE_FORMAT(TIMESTAMP('1998-01-31 13:14:15.012345'), '%Y-%b-%D %r')
    fetched rows / total rows = 1/1
    +-----------------------------------------------+----------------------------------------------------------------+
    | DATE('1998-01-31 13:14:15.012345', '%T.%f')   | DATE(TIMESTAMP('1998-01-31 13:14:15.012345'), '%Y-%b-%D %r')   |
    |-----------------------------------------------+----------------------------------------------------------------|
    | '13:14:15.012345'                             | '1998-Jan-31st 01:14:15 PM'                                    |
    +-----------------------------------------------+----------------------------------------------------------------+


DATE_SUB
--------

Description
>>>>>>>>>>>

Usage: date_sub(date, INTERVAL expr unit)/ date_sub(date, expr) subtracts the time interval expr from date

Argument type: DATE/DATETIME/TIMESTAMP/STRING, INTERVAL/LONG

Return type map:

DATE/DATETIME/TIMESTAMP/STRING, INTERVAL -> DATETIME

DATE, LONG -> DATE

DATETIME/TIMESTAMP/STRING, LONG -> DATETIME

Synonyms: `SUBDATE`_

Example::

    od> SELECT DATE_SUB(DATE('2008-01-02'), INTERVAL 31 DAY), DATE_SUB(DATE('2020-08-26'), 1), DATE_SUB(TIMESTAMP('2020-08-26 01:01:01'), 1)
    fetched rows / total rows = 1/1
    +-------------------------------------------------+-----------------------------------+-------------------------------------------------+
    | DATE_SUB(DATE('2008-01-02'), INTERVAL 31 DAY)   | DATE_SUB(DATE('2020-08-26'), 1)   | DATE_SUB(TIMESTAMP('2020-08-26 01:01:01'), 1)   |
    |-------------------------------------------------+-----------------------------------+-------------------------------------------------|
    | 2007-12-02                                      | 2020-08-25                        | 2020-08-25 01:01:01                             |
    +-------------------------------------------------+-----------------------------------+-------------------------------------------------+


DAY
---

Description
>>>>>>>>>>>

Usage: day(date) extracts the day of the month for date, in the range 1 to 31. The dates with value 0 such as '0000-00-00' or '2008-00-00' are invalid.

Argument type: STRING/DATE/DATETIME/TIMESTAMP

Return type: INTEGER

Synonyms: DAYOFMONTH

Example::

    od> SELECT DAY(DATE('2020-08-26'))
    fetched rows / total rows = 1/1
    +---------------------------+
    | DAY(DATE('2020-08-26'))   |
    |---------------------------|
    | 26                        |
    +---------------------------+


DAYNAME
-------

Description
>>>>>>>>>>>

Usage: dayname(date) returns the name of the weekday for date, including Monday, Tuesday, Wednesday, Thursday, Friday, Saturday and Sunday.

Argument type: STRING/DATE/DATETIME/TIMESTAMP

Return type: STRING

Example::

    od> SELECT DAYNAME(DATE('2020-08-26'))
    fetched rows / total rows = 1/1
    +-------------------------------+
    | DAYNAME(DATE('2020-08-26'))   |
    |-------------------------------|
    | Wednesday                     |
    +-------------------------------+


DAYOFMONTH
----------

Description
>>>>>>>>>>>

Usage: dayofmonth(date) extracts the day of the month for date, in the range 1 to 31. The dates with value 0 such as '0000-00-00' or '2008-00-00' are invalid.

Argument type: STRING/DATE/DATETIME/TIMESTAMP

Return type: INTEGER

Synonyms: DAY

Example::

    od> SELECT DAYOFMONTH(DATE('2020-08-26'))
    fetched rows / total rows = 1/1
    +----------------------------------+
    | DAYOFMONTH(DATE('2020-08-26'))   |
    |----------------------------------|
    | 26                               |
    +----------------------------------+


DAYOFWEEK
---------

Description
>>>>>>>>>>>

Usage: dayofweek(date) returns the weekday index for date (1 = Sunday, 2 = Monday, …, 7 = Saturday).

Argument type: STRING/DATE/DATETIME/TIMESTAMP

Return type: INTEGER

Example::

    od> SELECT DAYOFWEEK(DATE('2020-08-26'))
    fetched rows / total rows = 1/1
    +---------------------------------+
    | DAYOFWEEK(DATE('2020-08-26'))   |
    |---------------------------------|
    | 4                               |
    +---------------------------------+



DAYOFYEAR
---------

Description
>>>>>>>>>>>

Usage:  dayofyear(date) returns the day of the year for date, in the range 1 to 366.

Argument type: STRING/DATE/DATETIME/TIMESTAMP

Return type: INTEGER

Example::

    od> SELECT DAYOFYEAR(DATE('2020-08-26'))
    fetched rows / total rows = 1/1
    +---------------------------------+
    | DAYOFYEAR(DATE('2020-08-26'))   |
    |---------------------------------|
    | 239                             |
    +---------------------------------+


FROM_DAYS
---------

Description
>>>>>>>>>>>

Usage: from_days(N) returns the date value given the day number N.

Argument type: INTEGER/LONG

Return type: DATE

Example::

    od> SELECT FROM_DAYS(733687)
    fetched rows / total rows = 1/1
    +---------------------+
    | FROM_DAYS(733687)   |
    |---------------------|
    | 2008-10-07          |
    +---------------------+


HOUR
----

Description
>>>>>>>>>>>

Usage: hour(time) extracts the hour value for time. Different from the time of day value, the time value has a large range and can be greater than 23, so the return value of hour(time) can be also greater than 23.

Argument type: STRING/TIME/DATETIME/TIMESTAMP

Return type: INTEGER

Example::

    od> SELECT HOUR((TIME '01:02:03'))
    fetched rows / total rows = 1/1
    +---------------------------+
    | HOUR((TIME '01:02:03'))   |
    |---------------------------|
    | 1                         |
    +---------------------------+


MAKETIME
--------

Description
>>>>>>>>>>>

Specifications:

1. MAKETIME(INTEGER, INTEGER, INTEGER) -> DATE


MICROSECOND
-----------

Description
>>>>>>>>>>>

Usage: microsecond(expr) returns the microseconds from the time or datetime expression expr as a number in the range from 0 to 999999.

Argument type: STRING/TIME/DATETIME/TIMESTAMP

Return type: INTEGER

Example::

    od> SELECT MICROSECOND((TIME '01:02:03.123456'))
    fetched rows / total rows = 1/1
    +-----------------------------------------+
    | MICROSECOND((TIME '01:02:03.123456'))   |
    |-----------------------------------------|
    | 123456                                  |
    +-----------------------------------------+


MINUTE
------

Description
>>>>>>>>>>>

Usage: minute(time) returns the minute for time, in the range 0 to 59.

Argument type: STRING/TIME/DATETIME/TIMESTAMP

Return type: INTEGER

Example::

    od> SELECT MINUTE((TIME '01:02:03'))
    fetched rows / total rows = 1/1
    +-----------------------------+
    | MINUTE((TIME '01:02:03'))   |
    |-----------------------------|
    | 2                           |
    +-----------------------------+


MONTH
-----

Description
>>>>>>>>>>>

Usage: month(date) returns the month for date, in the range 1 to 12 for January to December. The dates with value 0 such as '0000-00-00' or '2008-00-00' are invalid.

Argument type: STRING/DATE/DATETIME/TIMESTAMP

Return type: INTEGER

Example::

    od> SELECT MONTH(DATE('2020-08-26'))
    fetched rows / total rows = 1/1
    +-----------------------------+
    | MONTH(DATE('2020-08-26'))   |
    |-----------------------------|
    | 8                           |
    +-----------------------------+


MONTHNAME
---------

Description
>>>>>>>>>>>

Usage: monthname(date) returns the full name of the month for date.

Argument type: STRING/DATE/DATETIME/TIMESTAMP

Return type: STRING

Example::

    od> SELECT MONTHNAME(DATE('2020-08-26'))
    fetched rows / total rows = 1/1
    +---------------------------------+
    | MONTHNAME(DATE('2020-08-26'))   |
    |---------------------------------|
    | August                          |
    +---------------------------------+


NOW
---

Description
>>>>>>>>>>>

Specifications:

1. NOW() -> DATE


QUARTER
-------

Description
>>>>>>>>>>>

Usage: quarter(date) returns the quarter of the year for date, in the range 1 to 4.

Argument type: STRING/DATE/DATETIME/TIMESTAMP

Return type: INTEGER

Example::

    od> SELECT QUARTER(DATE('2020-08-26'))
    fetched rows / total rows = 1/1
    +-------------------------------+
    | QUARTER(DATE('2020-08-26'))   |
    |-------------------------------|
    | 3                             |
    +-------------------------------+


SECOND
------

Description
>>>>>>>>>>>

Usage: second(time) returns the second for time, in the range 0 to 59.

Argument type: STRING/TIME/DATETIME/TIMESTAMP

Return type: INTEGER

Example::

    od> SELECT SECOND((TIME '01:02:03'))
    fetched rows / total rows = 1/1
    +-----------------------------+
    | SECOND((TIME '01:02:03'))   |
    |-----------------------------|
    | 3                           |
    +-----------------------------+


SUBDATE
-------

Description
>>>>>>>>>>>

Usage: subdate(date, INTERVAL expr unit)/ subdate(date, expr) subtracts the time interval expr from date

Argument type: DATE/DATETIME/TIMESTAMP/STRING, INTERVAL/LONG

Return type map:

DATE/DATETIME/TIMESTAMP/STRING, INTERVAL -> DATETIME

DATE, LONG -> DATE

DATETIME/TIMESTAMP/STRING, LONG -> DATETIME

Synonyms: `DATE_SUB`_

Example::

    od> SELECT SUBDATE(DATE('2008-01-02'), INTERVAL 31 DAY), SUBDATE(DATE('2020-08-26'), 1), SUBDATE(TIMESTAMP('2020-08-26 01:01:01'), 1)
    fetched rows / total rows = 1/1
    +------------------------------------------------+----------------------------------+------------------------------------------------+
    | SUBDATE(DATE('2008-01-02'), INTERVAL 31 DAY)   | SUBDATE(DATE('2020-08-26'), 1)   | SUBDATE(TIMESTAMP('2020-08-26 01:01:01'), 1)   |
    |------------------------------------------------+----------------------------------+------------------------------------------------|
    | 2007-12-02                                     | 2020-08-25                       | 2020-08-25 01:01:01                            |
    +------------------------------------------------+----------------------------------+------------------------------------------------+


TIME
----

Description
>>>>>>>>>>>

Usage: time(expr) constructs a time type with the input string expr as a time. If the argument is of date/datetime/time/timestamp, it extracts the time value part from the expression.

Argument type: STRING/DATE/DATETIME/TIME/TIMESTAMP

Return type: TIME

Example::

    >od SELECT TIME('13:49:00'), TIME(TIMESTAMP('2020-08-26 13:49:00'))
    fetched rows / total rows = 1/1
    +--------------------+------------------------------------------+
    | TIME('13:49:00')   | TIME(TIMESTAMP('2020-08-26 13:49:00'))   |
    |--------------------+------------------------------------------|
    | TIME '13:49:00'    | TIME '13:49:00'                          |
    +--------------------+------------------------------------------+


TIME_TO_SEC
-----------

Description
>>>>>>>>>>>

Usage: time_to_sec(time) returns the time argument, converted to seconds.

Argument type: STRING/TIME/DATETIME/TIMESTAMP

Return type: LONG

Example::

    od> SELECT TIME_TO_SEC(TIME '22:23:00')
    fetched rows / total rows = 1/1
    +--------------------------------+
    | TIME_TO_SEC(TIME '22:23:00')   |
    |--------------------------------|
    | 80580                          |
    +--------------------------------+


TIMESTAMP
---------

Description
>>>>>>>>>>>

Usage: timestamp(expr) construct a timestamp type with the input string expr as an timestamp. If the argument is of date/datetime/timestamp type, cast expr to timestamp type with default timezone UTC.

Argument type: STRING/DATE/DATETIME/TIMESTAMP

Return type: TIMESTAMP

Example::

    >od SELECT TIMESTAMP('2020-08-26 13:49:00')
    fetched rows / total rows = 1/1
    +------------------------------------+
    | TIMESTAMP('2020-08-26 13:49:00')   |
    |------------------------------------|
    | TIMESTAMP '2020-08-26 13:49:00     |
    +------------------------------------+


TO_DAYS
-------

Description
>>>>>>>>>>>

Usage: to_days(date) returns the day number (the number of days since year 0) of the given date. Returns NULL if date is invalid.

Argument type: STRING/DATE/DATETIME/TIMESTAMP

Return type: LONG

Example::

    od> SELECT TO_DAYS(DATE '2008-10-07')
    fetched rows / total rows = 1/1
    +------------------------------+
    | TO_DAYS(DATE '2008-10-07')   |
    |------------------------------|
    | 733687                       |
    +------------------------------+


WEEK
----

Description
>>>>>>>>>>>

Usage: week(date[, mode]) returns the week number for date. If the mode argument is omitted, the default mode 0 is used.

.. list-table:: The following table describes how the mode argument works.
   :widths: 25 50 25 75
   :header-rows: 1

   * - Mode
     - First day of week
     - Range
     - Week 1 is the first week …
   * - 0
     - Sunday
     - 0-53
     - with a Sunday in this year
   * - 1
     - Monday
     - 0-53
     - with 4 or more days this year
   * - 2
     - Sunday
     - 1-53
     - with a Sunday in this year
   * - 3
     - Monday
     - 1-53
     - with 4 or more days this year
   * - 4
     - Sunday
     - 0-53
     - with 4 or more days this year
   * - 5
     - Monday
     - 0-53
     - with a Monday in this year
   * - 6
     - Sunday
     - 1-53
     - with 4 or more days this year
   * - 7
     - Monday
     - 1-53
     - with a Monday in this year

Argument type: DATE/DATETIME/TIMESTAMP/STRING

Return type: INTEGER

Example::

    >od SELECT WEEK(DATE('2008-02-20')), WEEK(DATE('2008-02-20'), 1)
    fetched rows / total rows = 1/1
    +----------------------------+-------------------------------+
    | WEEK(DATE('2008-02-20'))   | WEEK(DATE('2008-02-20'), 1)   |
    |----------------------------|-------------------------------|
    | 7                          | 8                             |
    +----------------------------+-------------------------------+


YEAR
----

Description
>>>>>>>>>>>

Usage: year(date) returns the year for date, in the range 1000 to 9999, or 0 for the “zero” date.

Argument type: STRING/DATE/DATETIME/TIMESTAMP

Return type: INTEGER

Example::

    od> SELECT YEAR(DATE('2020-08-26'))
    fetched rows / total rows = 1/1
    +----------------------------+
    | YEAR(DATE('2020-08-26'))   |
    |----------------------------|
    | 2020                       |
    +----------------------------+


String Functions
================

ASCII
-----

Description
>>>>>>>>>>>

Specifications:

1. ASCII(STRING T) -> INTEGER


CONCAT
------

Description
>>>>>>>>>>>

Usage: CONCAT(str1, str2) returns str1 and str strings concatenated together.

Argument type: STRING, STRING

Return type: STRING

Example::

    od> SELECT CONCAT('hello', 'world')
    fetched rows / total rows = 1/1
    +----------------------------+
    | CONCAT('hello', 'world')   |
    |----------------------------|
    | helloworld                 |
    +----------------------------+


CONCAT_WS
---------

Description
>>>>>>>>>>>

Usage: CONCAT_WS(sep, str1, str2) returns str1 concatenated with str2 using sep as a separator between them.

Argument type: STRING, STRING, STRING

Return type: STRING

Example::

    od> SELECT CONCAT_WS(',', 'hello', 'world')
    fetched rows / total rows = 1/1
    +------------------------------------+
    | CONCAT_WS(',', 'hello', 'world')   |
    |------------------------------------|
    | hello,world                        |
    +------------------------------------+


LEFT
----

Description
>>>>>>>>>>>

Specifications:

1. LEFT(STRING T, INTEGER) -> T


LENGTH
------

Description
>>>>>>>>>>>

Specifications:

1. LENGTH(STRING) -> INTEGER

Usage: length(str) returns length of string measured in bytes.

Argument type: STRING

Return type: INTEGER

Example::

    od> SELECT LENGTH('helloworld')
    fetched rows / total rows = 1/1
    +------------------------+
    | LENGTH('helloworld')   |
    |------------------------|
    | 10                     |
    +------------------------+


LOCATE
------

Description
>>>>>>>>>>>

Specifications:

1. LOCATE(STRING, STRING, INTEGER) -> INTEGER
2. LOCATE(STRING, STRING) -> INTEGER


LOWER
-----

Description
>>>>>>>>>>>

Usage: lower(string) converts the string to lowercase.

Argument type: STRING

Return type: STRING

Example::

    od> SELECT LOWER('helloworld'), LOWER('HELLOWORLD')
    fetched rows / total rows = 1/1
    +-----------------------+-----------------------+
    | LOWER('helloworld')   | LOWER('HELLOWORLD')   |
    |-----------------------+-----------------------|
    | helloworld            | helloworld            |
    +-----------------------+-----------------------+


LTRIM
-----

Description
>>>>>>>>>>>

Usage: ltrim(str) trims leading space characters from the string.

Argument type: STRING

Return type: STRING

Example::

    od> SELECT LTRIM('   hello'), LTRIM('hello   ')
    fetched rows / total rows = 1/1
    +---------------------+---------------------+
    | LTRIM('   hello')   | LTRIM('hello   ')   |
    |---------------------+---------------------|
    | hello               | hello               |
    +---------------------+---------------------+


REPLACE
-------

Description
>>>>>>>>>>>

Specifications:

1. REPLACE(STRING T, STRING, STRING) -> T


RIGHT
-----

Description
>>>>>>>>>>>

Specifications:

1. RIGHT(STRING T, INTEGER) -> T


RTRIM
-----

Description
>>>>>>>>>>>

Usage: rtrim(str) trims trailing space characters from the string.

Argument type: STRING

Return type: STRING

Example::

    od> SELECT RTRIM('   hello'), RTRIM('hello   ')
    fetched rows / total rows = 1/1
    +---------------------+---------------------+
    | RTRIM('   hello')   | RTRIM('hello   ')   |
    |---------------------+---------------------|
    |    hello            | hello               |
    +---------------------+---------------------+


SUBSTRING
---------

Description
>>>>>>>>>>>

Usage: substring(str, start) or substring(str, start, length) returns substring using start and length. With no length, entire string from start is returned.

Argument type: STRING, INTEGER, INTEGER

Return type: STRING

Synonyms: SUBSTR

Example::

    od> SELECT SUBSTRING('helloworld', 5), SUBSTRING('helloworld', 5, 3)
    fetched rows / total rows = 1/1
    +------------------------------+---------------------------------+
    | SUBSTRING('helloworld', 5)   | SUBSTRING('helloworld', 5, 3)   |
    |------------------------------+---------------------------------|
    | oworld                       | owo                             |
    +------------------------------+---------------------------------+


TRIM
----

Description
>>>>>>>>>>>

Argument Type: STRING

Return type: STRING

Example::

    od> SELECT TRIM('   hello'), TRIM('hello   ')
    fetched rows / total rows = 1/1
    +--------------------+--------------------+
    | TRIM('   hello')   | TRIM('hello   ')   |
    |--------------------+--------------------|
    | hello              | hello              |
    +--------------------+--------------------+


UPPER
-----

Description
>>>>>>>>>>>

Usage: upper(string) converts the string to uppercase.

Argument type: STRING

Return type: STRING

Example::

    od> SELECT UPPER('helloworld'), UPPER('HELLOWORLD')
    fetched rows / total rows = 1/1
    +-----------------------+-----------------------+
    | UPPER('helloworld')   | UPPER('HELLOWORLD')   |
    |-----------------------+-----------------------|
    | HELLOWORLD            | HELLOWORLD            |
    +-----------------------+-----------------------+

Conditional Functions
=====================

IF
--

Description
>>>>>>>>>>>

Specifications:

1. IF(BOOLEAN, ES_TYPE, ES_TYPE) -> ES_TYPE


IFNULL
------

Description
>>>>>>>>>>>

Specifications:

1. IFNULL(ES_TYPE, ES_TYPE) -> ES_TYPE


ISNULL
------

Description
>>>>>>>>>>>

Specifications:

1. ISNULL(ES_TYPE) -> INTEGER


CASE
----

Description
>>>>>>>>>>>

``CASE`` statement has two forms with slightly different syntax: Simple Case and Searched Case.

Simple case syntax compares a case value expression with each compare expression in ``WHEN`` clause and return its result if matched. Otherwise, result expression's value in ``ELSE`` clause is returned (or ``NULL`` if absent)::

   CASE case_value_expression
     WHEN compare_expression THEN result_expression
     [WHEN compare_expression THEN result_expression] ...
     [ELSE result_expression]
   END

Similarly, searched case syntax evaluates each search condition and return result if true. A search condition must be a predicate that returns a bool when evaluated::

   CASE
     WHEN search_condition THEN result_expression
     [WHEN search_condition THEN result_expression] ...
     [ELSE result_expression]
   END

Type Check
>>>>>>>>>>

All result types in ``WHEN`` and ``ELSE`` clause are required to be exactly the same. Otherwise, take the following query for example, you'll see an semantic analysis exception thrown::

   CASE age
     WHEN 30 THEN 'Thirty'
     WHEN 50 THEN true
   END

Examples
>>>>>>>>

Here are examples for simple case syntax::

    od> SELECT
    ...   CASE 1
    ...     WHEN 1 THEN 'One'
    ...   END AS simple_case,
    ...   CASE ABS(-2)
    ...     WHEN 1 THEN 'One'
    ...     WHEN 2 THEN 'Absolute two'
    ...   END AS func_case_value,
    ...   CASE ABS(-3)
    ...     WHEN 1 THEN 'One'
    ...     ELSE TRIM(' Absolute three ')
    ...   END AS func_result;
    fetched rows / total rows = 1/1
    +---------------+-------------------+----------------+
    | simple_case   | func_case_value   | func_result    |
    |---------------+-------------------+----------------|
    | One           | Absolute two      | Absolute three |
    +---------------+-------------------+----------------+

Here are examples for searched case syntax::

    od> SELECT
    ...   CASE
    ...     WHEN 1 = 1 THEN 'One'
    ...   END AS single_search,
    ...   CASE
    ...     WHEN 2 = 1 THEN 'One'
    ...     WHEN 'hello' = 'hello' THEN 'Hello' END AS multi_searches,
    ...   CASE
    ...     WHEN 2 = 1 THEN 'One'
    ...     WHEN 'hello' = 'world' THEN 'Hello'
    ...   END AS no_else;
    fetched rows / total rows = 1/1
    +-----------------+------------------+-----------+
    | single_search   | multi_searches   | no_else   |
    |-----------------+------------------+-----------|
    | One             | Hello            | null      |
    +-----------------+------------------+-----------+
