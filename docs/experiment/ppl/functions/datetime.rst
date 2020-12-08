=======================
Date and Time Functions
=======================

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 1

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

    od> source=people | eval `ADDDATE(DATE('2020-08-26'), INTERVAL 1 HOUR)` = ADDDATE(DATE('2020-08-26'), INTERVAL 1 HOUR), `ADDDATE(DATE('2020-08-26'), 1)` = ADDDATE(DATE('2020-08-26'), 1), `ADDDATE(TIMESTAMP('2020-08-26 01:01:01'), 1)` = ADDDATE(TIMESTAMP('2020-08-26 01:01:01'), 1) | fields `ADDDATE(DATE('2020-08-26'), INTERVAL 1 HOUR)`, `ADDDATE(DATE('2020-08-26'), 1)`, `ADDDATE(TIMESTAMP('2020-08-26 01:01:01'), 1)`
    fetched rows / total rows = 1/1
    +------------------------------------------------+----------------------------------+------------------------------------------------+
    | ADDDATE(DATE('2020-08-26'), INTERVAL 1 HOUR)   | ADDDATE(DATE('2020-08-26'), 1)   | ADDDATE(TIMESTAMP('2020-08-26 01:01:01'), 1)   |
    |------------------------------------------------+----------------------------------+------------------------------------------------|
    | 2020-08-26 01:00:00                            | 2020-08-27                       | 2020-08-27 01:01:01                            |
    +------------------------------------------------+----------------------------------+------------------------------------------------+


DATE
----

Description
>>>>>>>>>>>

Usage: date(expr) constructs a date type with the input string expr as a date. If the argument is of date/datetime/timestamp, it extracts the date value part from the expression.

Argument type: STRING/DATE/DATETIME/TIMESTAMP

Return type: DATE

Example::

    >od source=people | eval `DATE('2020-08-26')` = DATE('2020-08-26'), `DATE(TIMESTAMP('2020-08-26 13:49:00'))` = DATE(TIMESTAMP('2020-08-26 13:49:00')) | fields `DATE('2020-08-26')`, `DATE(TIMESTAMP('2020-08-26 13:49:00'))`
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

    od> source=people | eval `DATE_ADD(DATE('2020-08-26'), INTERVAL 1 HOUR)` = DATE_ADD(DATE('2020-08-26'), INTERVAL 1 HOUR), `DATE_ADD(DATE('2020-08-26'), 1)` = DATE_ADD(DATE('2020-08-26'), 1), `DATE_ADD(TIMESTAMP('2020-08-26 01:01:01'), 1)` = DATE_ADD(TIMESTAMP('2020-08-26 01:01:01'), 1) | fields `DATE_ADD(DATE('2020-08-26'), INTERVAL 1 HOUR)`, `DATE_ADD(DATE('2020-08-26'), 1)`, `DATE_ADD(TIMESTAMP('2020-08-26 01:01:01'), 1)`
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

    >od source=people | eval `DATE_FORMAT('1998-01-31 13:14:15.012345', '%T.%f')` = DATE_FORMAT('1998-01-31 13:14:15.012345', '%T.%f'), `DATE_FORMAT(TIMESTAMP('1998-01-31 13:14:15.012345'), '%Y-%b-%D %r')` = DATE_FORMAT(TIMESTAMP('1998-01-31 13:14:15.012345'), '%Y-%b-%D %r') | fields `DATE_FORMAT('1998-01-31 13:14:15.012345', '%T.%f')`, `DATE_FORMAT(TIMESTAMP('1998-01-31 13:14:15.012345'), '%Y-%b-%D %r')`
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

    od> source=people | eval `DATE_SUB(DATE('2008-01-02'), INTERVAL 31 DAY)` = DATE_SUB(DATE('2008-01-02'), INTERVAL 31 DAY), `DATE_SUB(DATE('2020-08-26'), 1)` = DATE_SUB(DATE('2020-08-26'), 1), `DATE_SUB(TIMESTAMP('2020-08-26 01:01:01'), 1)` = DATE_SUB(TIMESTAMP('2020-08-26 01:01:01'), 1) | fields `DATE_SUB(DATE('2008-01-02'), INTERVAL 31 DAY)`, `DATE_SUB(DATE('2020-08-26'), 1)`, `DATE_SUB(TIMESTAMP('2020-08-26 01:01:01'), 1)`
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

    od> source=people | eval `DAY(DATE('2020-08-26'))` = DAY(DATE('2020-08-26')) | fields `DAY(DATE('2020-08-26'))`
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

    od> source=people | eval `DAYNAME(DATE('2020-08-26'))` = DAYNAME(DATE('2020-08-26')) | fields `DAYNAME(DATE('2020-08-26'))`
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

    od> source=people | eval `DAYOFMONTH(DATE('2020-08-26'))` = DAYOFMONTH(DATE('2020-08-26')) | fields `DAYOFMONTH(DATE('2020-08-26'))`
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

    od> source=people | eval `DAYOFWEEK(DATE('2020-08-26'))` = DAYOFWEEK(DATE('2020-08-26')) | fields `DAYOFWEEK(DATE('2020-08-26'))`
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

    od> source=people | eval `DAYOFYEAR(DATE('2020-08-26'))` = DAYOFYEAR(DATE('2020-08-26')) | fields `DAYOFYEAR(DATE('2020-08-26'))`
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

    od> source=people | eval `FROM_DAYS(733687)` = FROM_DAYS(733687) | fields `FROM_DAYS(733687)`
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

    od> source=people | eval `HOUR(TIME('01:02:03'))` = HOUR(TIME('01:02:03')) | fields `HOUR(TIME('01:02:03'))`
    fetched rows / total rows = 1/1
    +--------------------------+
    | HOUR(TIME('01:02:03'))   |
    |--------------------------|
    | 1                        |
    +--------------------------+


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

    od> source=people | eval `MICROSECOND(TIME('01:02:03.123456'))` = MICROSECOND(TIME('01:02:03.123456')) | fields `MICROSECOND(TIME('01:02:03.123456'))`
    fetched rows / total rows = 1/1
    +----------------------------------------+
    | MICROSECOND(TIME('01:02:03.123456'))   |
    |----------------------------------------|
    | 123456                                 |
    +----------------------------------------+


MINUTE
------

Description
>>>>>>>>>>>

Usage: minute(time) returns the minute for time, in the range 0 to 59.

Argument type: STRING/TIME/DATETIME/TIMESTAMP

Return type: INTEGER

Example::

    od> source=people | eval `MINUTE(TIME('01:02:03'))` =  MINUTE(TIME('01:02:03')) | fields `MINUTE(TIME('01:02:03'))`
    fetched rows / total rows = 1/1
    +----------------------------+
    | MINUTE(TIME('01:02:03'))   |
    |----------------------------|
    | 2                          |
    +----------------------------+


MONTH
-----

Description
>>>>>>>>>>>

Usage: month(date) returns the month for date, in the range 1 to 12 for January to December. The dates with value 0 such as '0000-00-00' or '2008-00-00' are invalid.

Argument type: STRING/DATE/DATETIME/TIMESTAMP

Return type: INTEGER

Example::

    od> source=people | eval `MONTH(DATE('2020-08-26'))` =  MONTH(DATE('2020-08-26')) | fields `MONTH(DATE('2020-08-26'))`
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

    od> source=people | eval `MONTHNAME(DATE('2020-08-26'))` = MONTHNAME(DATE('2020-08-26')) | fields `MONTHNAME(DATE('2020-08-26'))`
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

    od> source=people | eval `QUARTER(DATE('2020-08-26'))` = QUARTER(DATE('2020-08-26')) | fields `QUARTER(DATE('2020-08-26'))`
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

    od> source=people | eval `SECOND(TIME('01:02:03'))` = SECOND(TIME('01:02:03')) | fields `SECOND(TIME('01:02:03'))`
    fetched rows / total rows = 1/1
    +----------------------------+
    | SECOND(TIME('01:02:03'))   |
    |----------------------------|
    | 3                          |
    +----------------------------+


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

    od> source=people | eval `SUBDATE(DATE('2008-01-02'), INTERVAL 31 DAY)` = SUBDATE(DATE('2008-01-02'), INTERVAL 31 DAY), `SUBDATE(DATE('2020-08-26'), 1)` = SUBDATE(DATE('2020-08-26'), 1), `SUBDATE(TIMESTAMP('2020-08-26 01:01:01'), 1)` = SUBDATE(TIMESTAMP('2020-08-26 01:01:01'), 1) | fields `SUBDATE(DATE('2008-01-02'), INTERVAL 31 DAY)`, `SUBDATE(DATE('2020-08-26'), 1)`, `SUBDATE(TIMESTAMP('2020-08-26 01:01:01'), 1)`
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

    >od source=people | eval `TIME('13:49:00')` = TIME('13:49:00'), `TIME(TIMESTAMP('2020-08-26 13:49:00'))` = TIME(TIMESTAMP('2020-08-26 13:49:00')) | fields `TIME('13:49:00')`, `TIME(TIMESTAMP('2020-08-26 13:49:00'))`
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

    od> source=people | eval `TIME_TO_SEC(TIME('22:23:00'))` = TIME_TO_SEC(TIME('22:23:00')) | fields `TIME_TO_SEC(TIME('22:23:00'))`
    fetched rows / total rows = 1/1
    +---------------------------------+
    | TIME_TO_SEC(TIME('22:23:00'))   |
    |---------------------------------|
    | 80580                           |
    +---------------------------------+


TIMESTAMP
---------

Description
>>>>>>>>>>>

Usage: timestamp(expr) construct a timestamp type with the input string expr as an timestamp. If the argument is of date/datetime/timestamp type, cast expr to timestamp type with default timezone UTC.

Argument type: STRING/DATE/DATETIME/TIMESTAMP

Return type: TIMESTAMP

Example::

    >od source=people | eval `TIMESTAMP('2020-08-26 13:49:00')` = TIMESTAMP('2020-08-26 13:49:00') | fields `TIMESTAMP('2020-08-26 13:49:00')`
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

    od> source=people | eval `TO_DAYS(DATE('2008-10-07'))` = TO_DAYS(DATE('2008-10-07')) | fields `TO_DAYS(DATE('2008-10-07'))`
    fetched rows / total rows = 1/1
    +-------------------------------+
    | TO_DAYS(DATE('2008-10-07'))   |
    |-------------------------------|
    | 733687                        |
    +-------------------------------+


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

    >od source=people | eval `WEEK(DATE('2008-02-20'))` = WEEK(DATE('2008-02-20')), `WEEK(DATE('2008-02-20'), 1)` = WEEK(DATE('2008-02-20'), 1) | fields `WEEK(DATE('2008-02-20'))`, `WEEK(DATE('2008-02-20'), 1)`
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

    od> source=people | eval `YEAR(DATE('2020-08-26'))` = YEAR(DATE('2020-08-26')) | fields `YEAR(DATE('2020-08-26'))`
    fetched rows / total rows = 1/1
    +----------------------------+
    | YEAR(DATE('2020-08-26'))   |
    |----------------------------|
    | 2020                       |
    +----------------------------+


