
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


MODULUS
=======

Description
-----------

Specifications: 

1. MODULUS(NUMBER T, NUMBER) -> T


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

Specifications: 

1. POW(NUMBER T) -> T
2. POW(NUMBER T, NUMBER) -> T


POWER
=====

Description
-----------

Specifications: 

1. POWER(NUMBER T) -> T
2. POWER(NUMBER T, NUMBER) -> T


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

Specifications: 

1. ROUND(NUMBER T) -> T


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

Specifications: 

1. SIGN(NUMBER T) -> T


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

Specifications: 

1. SQRT(NUMBER T) -> T


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


