/*
MySQL (Positive Technologies) grammar
The MIT License (MIT).
Copyright (c) 2015-2017, Ivan Kochurkin (kvanttt@gmail.com), Positive Technologies.
Copyright (c) 2017, Ivan Khudyashev (IHudyashov@ptsecurity.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

parser grammar OpenDistroSQLParser;

import OpenDistroSQLIdentifierParser;

options { tokenVocab=OpenDistroSQLLexer; }


// Top Level Description

//    Root rule
root
    : sqlStatement? SEMI? EOF
    ;

//    Only SELECT
sqlStatement
    : dmlStatement | adminStatement
    ;

dmlStatement
    : selectStatement
    ;


// Data Manipulation Language

//    Primary DML Statements

selectStatement
    : querySpecification                                 #simpleSelect
    ;

adminStatement
    : showStatement
    | describeStatement
    ;

showStatement
    : SHOW TABLES tableFilter?
    ;

describeStatement
    : DESCRIBE TABLES tableFilter columnFilter?
    ;

columnFilter
    : COLUMNS LIKE showDescribePattern
    ;

tableFilter
    : LIKE showDescribePattern
    ;

showDescribePattern
    : oldID=compatibleID | stringLiteral
    ;

compatibleID
    : (MODULE | ID)+?
    ;

//    Select Statement's Details

querySpecification
    : selectClause
      fromClause?
      limitClause?
    ;

selectClause
    : SELECT selectSpec? selectElements
    ;

selectSpec
    : (ALL | DISTINCT)
    ;

selectElements
    : (star=STAR | selectElement) (COMMA selectElement)*
    ;

selectElement
    : expression (AS? alias)?
    ;

fromClause
    : FROM relation
      (whereClause)?
      (groupByClause)?
      (havingClause)?
      (orderByClause)? // Place it under FROM for now but actually not necessary ex. A UNION B ORDER BY
    ;

relation
    : tableName (AS? alias)?                                                #tableAsRelation
    | LR_BRACKET subquery=querySpecification RR_BRACKET AS? alias           #subqueryAsRelation
    ;

whereClause
    : WHERE expression
    ;

groupByClause
    : GROUP BY groupByElements
    ;

groupByElements
    : groupByElement (COMMA groupByElement)*
    ;

groupByElement
    : expression
    ;

havingClause
    : HAVING expression
    ;

orderByClause
    : ORDER BY orderByElement (COMMA orderByElement)*
    ;

orderByElement
    : expression order=(ASC | DESC)? (NULLS (FIRST | LAST))?
    ;

limitClause
    : LIMIT (offset=decimalLiteral COMMA)? limit=decimalLiteral
    | LIMIT limit=decimalLiteral OFFSET offset=decimalLiteral
    ;

//  Window Function's Details
windowFunction
    : function=rankingWindowFunction overClause
    ;

rankingWindowFunction
    : functionName=(ROW_NUMBER | RANK | DENSE_RANK) LR_BRACKET RR_BRACKET
    ;

overClause
    : OVER LR_BRACKET partitionByClause? orderByClause? RR_BRACKET
    ;

partitionByClause
    : PARTITION BY expression (COMMA expression)*
    ;


//    Literals

constant
    : stringLiteral             #string
    | sign? decimalLiteral      #signedDecimal
    | sign? realLiteral         #signedReal
    | booleanLiteral            #boolean
    | datetimeLiteral           #datetime
    | intervalLiteral           #interval
    | nullLiteral               #null
    // Doesn't support the following types for now
    //| BIT_STRING
    //| NOT? nullLiteral=(NULL_LITERAL | NULL_SPEC_LITERAL)
    //| LEFT_BRACE dateType=(D | T | TS | DATE | TIME | TIMESTAMP) stringLiteral RIGHT_BRACE
    ;

decimalLiteral
    : DECIMAL_LITERAL | ZERO_DECIMAL | ONE_DECIMAL | TWO_DECIMAL
    ;

stringLiteral
    : STRING_LITERAL
    ;

booleanLiteral
    : TRUE | FALSE
    ;

realLiteral
    : REAL_LITERAL
    ;

sign
    : PLUS | MINUS
    ;

nullLiteral
    : NULL_LITERAL
    ;

// Date and Time Literal, follow ANSI 92
datetimeLiteral
    : dateLiteral
    | timeLiteral
    | timestampLiteral
    ;

dateLiteral
    : DATE date=stringLiteral
    ;

timeLiteral
    : TIME time=stringLiteral
    ;

timestampLiteral
    : TIMESTAMP timestamp=stringLiteral
    ;

intervalLiteral
    : INTERVAL expression intervalUnit
    ;

intervalUnit
    : MICROSECOND | SECOND | MINUTE | HOUR | DAY | WEEK | MONTH | QUARTER | YEAR | SECOND_MICROSECOND
    | MINUTE_MICROSECOND | MINUTE_SECOND | HOUR_MICROSECOND | HOUR_SECOND | HOUR_MINUTE | DAY_MICROSECOND
    | DAY_SECOND | DAY_MINUTE | DAY_HOUR | YEAR_MONTH
    ;

//    Expressions, predicates

// Simplified approach for expression
expression
    : NOT expression                                                #notExpression
    | left=expression AND right=expression                          #andExpression
    | left=expression OR right=expression                           #orExpression
    | predicate                                                     #predicateExpression
    ;

predicate
    : expressionAtom                                                #expressionAtomPredicate
    | left=predicate comparisonOperator right=predicate             #binaryComparisonPredicate
    | predicate IS nullNotnull                                      #isNullPredicate
    | left=predicate NOT? LIKE right=predicate                      #likePredicate
    | left=predicate REGEXP right=predicate                         #regexpPredicate
    ;

expressionAtom
    : constant                                                      #constantExpressionAtom
    | columnName                                                    #fullColumnNameExpressionAtom
    | functionCall                                                  #functionCallExpressionAtom
    | LR_BRACKET expression RR_BRACKET                              #nestedExpressionAtom
    | left=expressionAtom mathOperator right=expressionAtom         #mathExpressionAtom
    ;

mathOperator
    : PLUS | MINUS | STAR | DIVIDE | MODULE
    ;

comparisonOperator
    : '=' | '>' | '<' | '<' '=' | '>' '='
    | '<' '>' | '!' '='
    ;

nullNotnull
    : NOT? NULL_LITERAL
    ;

functionCall
    : scalarFunctionName LR_BRACKET functionArgs? RR_BRACKET        #scalarFunctionCall
    | specificFunction                                              #specificFunctionCall
    | windowFunction                                                #windowFunctionCall
    | aggregateFunction                                             #aggregateFunctionCall
    ;

scalarFunctionName
    : mathematicalFunctionName
    | dateTimeFunctionName
    | textFunctionName
    ;

specificFunction
    : CASE expression caseFuncAlternative+
        (ELSE elseArg=functionArg)? END                               #caseFunctionCall
    | CASE caseFuncAlternative+
        (ELSE elseArg=functionArg)? END                               #caseFunctionCall
    ;

caseFuncAlternative
    : WHEN condition=functionArg
      THEN consequent=functionArg
    ;

aggregateFunction
    : functionName=aggregationFunctionName LR_BRACKET functionArg RR_BRACKET #regularAggregateFunctionCall
    | COUNT LR_BRACKET STAR RR_BRACKET                                       #countStarFunctionCall
    ;

aggregationFunctionName
    : AVG | COUNT | SUM | MIN | MAX
    ;

mathematicalFunctionName
    : ABS | CEIL | CEILING | CONV | CRC32 | E | EXP | FLOOR | LN | LOG | LOG10 | LOG2 | MOD | PI | POW | POWER
    | RAND | ROUND | SIGN | SQRT | TRUNCATE
    | trigonometricFunctionName
    ;

trigonometricFunctionName
    : ACOS | ASIN | ATAN | ATAN2 | COS | COT | DEGREES | RADIANS | SIN | TAN
    ;

dateTimeFunctionName
    : ADDDATE | DATE | DATE_ADD | DATE_SUB | DAY | DAYNAME | DAYOFMONTH | DAYOFWEEK | DAYOFYEAR | FROM_DAYS
    | HOUR | MICROSECOND | MINUTE | MONTH | MONTHNAME | QUARTER | SECOND | SUBDATE | TIME | TIME_TO_SEC
    | TIMESTAMP | TO_DAYS | YEAR | WEEK | DATE_FORMAT
    ;

textFunctionName
    : SUBSTR | SUBSTRING | TRIM | LTRIM | RTRIM | LOWER | UPPER
    | CONCAT | CONCAT_WS | SUBSTR | LENGTH | STRCMP
    ;

functionArgs
    : functionArg (COMMA functionArg)*
    ;

functionArg
    : expression
    ;

