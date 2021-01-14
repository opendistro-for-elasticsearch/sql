==========
Data Types
==========

.. rubric:: Table of contents

.. contents::
   :local:
   :depth: 2


NULL and MISSING Values
=======================
ODFE SQL has two ways to represent missing information. (1) The presence of the field with a NULL for its value. and (2) the absence of the field.

Please note, when response is in table format, the MISSING value is translate to NULL value.

Here is an example, Nanette doesn't have email field and Dail has employer filed with NULL value::

    od> SELECT firstname, employer, email FROM accounts;
    fetched rows / total rows = 4/4
    +-------------+------------+-----------------------+
    | firstname   | employer   | email                 |
    |-------------+------------+-----------------------|
    | Amber       | Pyrami     | amberduke@pyrami.com  |
    | Hattie      | Netagy     | hattiebond@netagy.com |
    | Nanette     | Quility    | null                  |
    | Dale        | null       | daleadams@boink.com   |
    +-------------+------------+-----------------------+


NULL Literals
-------------

The type of a null literal is special and different from any existing one. In this case, an ``UNDEFINED`` type is in use when the type cannot be inferred at "compile time" (during query parsing and analyzing). Because this undefined type is compatible with any other type by design, a null literal can be accepted as a valid operand or function argument.

Here are examples for NULL literal and expressions with NULL literal involved::

    od> SELECT NULL, NULL = NULL, 1 + NULL, LENGTH(NULL);
    fetched rows / total rows = 1/1
    +--------+---------------+------------+----------------+
    | NULL   | NULL = NULL   | 1 + NULL   | LENGTH(NULL)   |
    |--------+---------------+------------+----------------|
    | null   | null          | null       | null           |
    +--------+---------------+------------+----------------+


General NULL and MISSING Values Handling
----------------------------------------
In general, if any operand evaluates to a MISSING value, the enclosing operator will return MISSING; if none of operands evaluates to a MISSING value but there is an operand evaluates to a NULL value, the enclosing operator will return NULL.

To handle null value properly, you can use special operators such as ``IS (NOT) NULL`` or conditional functions such as ``IFNULL``. Please find more details in the SQL function documentation.

Here is an example::

    od> SELECT firstname, employer LIKE 'Quility', email LIKE '%com' FROM accounts;
    fetched rows / total rows = 4/4
    +-------------+---------------------------+---------------------+
    | firstname   | employer LIKE 'Quility'   | email LIKE '%com'   |
    |-------------+---------------------------+---------------------|
    | Amber       | False                     | True                |
    | Hattie      | False                     | True                |
    | Nanette     | True                      | null                |
    | Dale        | null                      | True                |
    +-------------+---------------------------+---------------------+


Special NULL and MISSING Values Handling
----------------------------------------
THe AND, OR and NOT have special logic to handling NULL and MISSING value.

The following table is the truth table for AND and OR.

+---------+---------+---------+---------+
| A       | B       | A AND B | A OR B  |
+---------+---------+---------+---------+
| TRUE    | TRUE    | TRUE    | TRUE    |
+---------+---------+---------+---------+
| TRUE    | FALSE   | FALSE   | TRUE    |
+---------+---------+---------+---------+
| TRUE    | NULL    | NULL    | TRUE    |
+---------+---------+---------+---------+
| TRUE    | MISSING | MISSING | TRUE    |
+---------+---------+---------+---------+
| FALSE   | FALSE   | FALSE   | FALSE   |
+---------+---------+---------+---------+
| FALSE   | NULL    | FALSE   | NULL    |
+---------+---------+---------+---------+
| FALSE   | MISSING | FALSE   | MISSING |
+---------+---------+---------+---------+
| NULL    | NULL    | NULL    | NULL    |
+---------+---------+---------+---------+
| NULL    | MISSING | MISSING | NULL    |
+---------+---------+---------+---------+
| MISSING | MISSING | MISSING | MISSING |
+---------+---------+---------+---------+

The following table is the truth table for NOT.

+---------+---------+
| A       | NOT A   |
+---------+---------+
| TRUE    | FALSE   |
+---------+---------+
| FALSE   | TRUE    |
+---------+---------+
| NULL    | NULL    |
+---------+---------+
| MISSING | MISSING |
+---------+---------+
