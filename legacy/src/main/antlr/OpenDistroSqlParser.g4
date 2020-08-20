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

parser grammar OpenDistroSqlParser;

options { tokenVocab=OpenDistroSqlLexer; }


// Top Level Description

//    Root rule
root
    : sqlStatement? SEMI? EOF
    ;

//    Only SELECT, DELETE, SHOW and DSCRIBE are supported for now
sqlStatement
    : dmlStatement | administrationStatement | utilityStatement
    ;

dmlStatement
    : selectStatement | deleteStatement
    ;


// Data Manipulation Language

//    Primary DML Statements

selectStatement
    : querySpecification                                 #simpleSelect
    | queryExpression                                    #parenthesisSelect
    | querySpecification unionStatement+
        orderByClause? limitClause?                      #unionSelect
    | querySpecification minusStatement+
        orderByClause? limitClause?                      #minusSelect
    ;

deleteStatement
    : singleDeleteStatement
    ;

//    Detailed DML Statements

singleDeleteStatement
    : DELETE FROM tableName
      (WHERE expression)?
      orderByClause? (LIMIT decimalLiteral)?
    ;

orderByClause
    : ORDER BY orderByExpression (',' orderByExpression)*
    ;

orderByExpression
    : expression order=(ASC | DESC)?
    ;

tableSources
    : tableSource (',' tableSource)*
    ;

tableSource
    : tableSourceItem joinPart*                                     #tableSourceBase
    | '(' tableSourceItem joinPart* ')'                             #tableSourceNested
    ;

tableSourceItem
    : tableName (AS? alias=uid)?                                    #atomTableItem
    | (
      selectStatement
      | '(' parenthesisSubquery=selectStatement ')'
      )
      AS? alias=uid                                                 #subqueryTableItem
    | '(' tableSources ')'                                          #tableSourcesItem
    ;

joinPart
    : (INNER | CROSS)? JOIN tableSourceItem
      (
        ON expression
        | USING '(' uidList ')'
      )?                                                            #innerJoin
    | (LEFT | RIGHT) OUTER? JOIN tableSourceItem
        (
          ON expression
          | USING '(' uidList ')'
        )?                                                          #outerJoin
    | NATURAL ((LEFT | RIGHT) OUTER?)? JOIN tableSourceItem         #naturalJoin
    ;

//    Select Statement's Details

queryExpression
    : '(' querySpecification ')'
    | '(' queryExpression ')'
    ;

querySpecification
    : SELECT selectSpec* selectElements
      fromClause orderByClause? limitClause?
    ;

unionStatement
    : UNION unionType=(ALL | DISTINCT)?
      (querySpecification | queryExpression)
    ;

minusStatement
    : EXCEPT (querySpecification | queryExpression)
    ;

selectSpec
    : (ALL | DISTINCT)
    ;

selectElements
    : (star='*' | selectElement ) (',' selectElement)*
    ;

selectElement
    : fullId '.' '*'                                                #selectStarElement
    | fullColumnName (AS? uid)?                                     #selectColumnElement
    | functionCall (AS? uid)?                                       #selectFunctionElement
    | expression (AS? uid)?                                         #selectExpressionElement
    | NESTED '(' fullId DOT STAR ')'                                #selectNestedStarElement
    ;

fromClause
    : FROM tableSources
      (WHERE whereExpr=expression)?
      (
        GROUP BY
        groupByItem (',' groupByItem)*
      )?
      (HAVING havingExpr=expression)?
    ;

groupByItem
    : expression order=(ASC | DESC)?
    ;

limitClause
    : LIMIT
    (
      (offset=limitClauseAtom ',')? limit=limitClauseAtom
      | limit=limitClauseAtom OFFSET offset=limitClauseAtom
    )
    ;

limitClauseAtom
	: decimalLiteral
	;


//    SHOW/DESCIRBE statements

administrationStatement
    : showStatement
    ;

showStatement
    : SHOW showSchemaEntity
        (schemaFormat=(FROM | IN) uid)? showFilter?
    ;

utilityStatement
    : simpleDescribeStatement
    ;

simpleDescribeStatement
    : command=DESCRIBE tableName
      (column=uid | pattern=STRING_LITERAL)?
    ;

showFilter
    : LIKE STRING_LITERAL
    | WHERE expression
    ;

showSchemaEntity
    : FULL? TABLES
    ;


// Common Clauses

//    DB Objects

fullId
    : uid (DOT_ID | '.' uid)?
    ;

tableName
    : fullId                                #simpleTableName
    | uid STAR                              #tableNamePattern
    | uid DIVIDE uid                        #tableAndTypeName
    ;

fullColumnName
    : uid dottedId*
    ;

uid
    : simpleId
    | REVERSE_QUOTE_ID
    ;

simpleId
    : ID
    | DOT_ID  // note: the current scope by adding DOT_ID to simpleId is large, move DOT_ID upwards tablename if needed
    | DOUBLE_QUOTE_ID
    | BACKTICK_QUOTE_ID
    | keywordsCanBeId
    | functionNameBase
    ;

dottedId
    : DOT_ID
    | '.' uid
    ;

//    Literals

decimalLiteral
    : DECIMAL_LITERAL | ZERO_DECIMAL | ONE_DECIMAL | TWO_DECIMAL
    ;

stringLiteral
    : (
        STRING_LITERAL
        | START_NATIONAL_STRING_LITERAL
      ) STRING_LITERAL+
    | (
        STRING_LITERAL
        | START_NATIONAL_STRING_LITERAL
      )
    ;

booleanLiteral
    : TRUE | FALSE;

nullNotnull
    : NOT? (NULL_LITERAL | NULL_SPEC_LITERAL)
    ;

constant
    : stringLiteral | decimalLiteral
    | '-' decimalLiteral
    | booleanLiteral
    | REAL_LITERAL | BIT_STRING
    | NOT? nullLiteral=(NULL_LITERAL | NULL_SPEC_LITERAL)
    | LEFT_BRACE dateType=(D | T | TS | DATE | TIME | TIMESTAMP) stringLiteral RIGHT_BRACE
    ;


//    Common Lists

uidList
    : uid (',' uid)*
    ;

expressions
    : expression (',' expression)*
    ;

aggregateFunction
    : functionAsAggregatorFunction                                  #functionAsAggregatorFunctionCall
    | aggregateWindowedFunction                                     #aggregateWindowedFunctionCall
    ;

scalarFunction
    : scalarFunctionName '(' nestedFunctionArgs+ ')'                #nestedFunctionCall
    | scalarFunctionName '(' functionArgs? ')'                      #scalarFunctionCall
    ;

functionCall
    : aggregateFunction                                             #aggregateFunctionCall
    | scalarFunctionName '(' aggregateWindowedFunction ')'          #aggregationAsArgFunctionCall
    | scalarFunction                                                #scalarFunctionsCall
    | specificFunction                                              #specificFunctionCall
    | fullId '(' functionArgs? ')'                                  #udfFunctionCall
    ;

specificFunction
    : CAST '(' expression AS convertedDataType ')'                  #dataTypeFunctionCall
    | CASE expression caseFuncAlternative+
      (ELSE elseArg=functionArg)? END                               #caseFunctionCall
    | CASE caseFuncAlternative+
      (ELSE elseArg=functionArg)? END                               #caseFunctionCall
    ;

caseFuncAlternative
    : WHEN condition=functionArg
      THEN consequent=functionArg
    ;

convertedDataType
    : typeName=DATETIME
    | typeName=INT
    | typeName=DOUBLE
    | typeName=LONG
    | typeName=FLOAT
    | typeName=STRING
    ;

aggregateWindowedFunction
    : (AVG | MAX | MIN | SUM)
      '(' aggregator=(ALL | DISTINCT)? functionArg ')'
    | COUNT '(' (starArg='*' | aggregator=ALL? functionArg) ')'
    | COUNT '(' aggregator=DISTINCT functionArgs ')'
    ;

functionAsAggregatorFunction
    : (AVG | MAX | MIN | SUM)
      '(' aggregator=(ALL | DISTINCT)? functionCall ')'
    | COUNT '(' aggregator=(ALL | DISTINCT)? functionCall ')'
    ;

scalarFunctionName
    : functionNameBase
    ;

/*
Separated aggregate to function-aggregator and nonfunction-aggregator aggregations.

Current related rules: aggregateWindowedFunction, functionAsAggregatorFunction, aggregateFunction, functionCall
Original rules: functionCall (as is shown in below changes), no aggregateWindowFunction, no functionAsAggregatorFunction,
                no aggregateFunction

====

Separated function argument rule to nonFunctionCall and functionCall
functions with functionCall arguments are taken as nested functions

Current related rules: functionArgs, functionArg, nestedFunctionArgs
Original rules:
functionArgs
    : (constant | fullColumnName | functionCall | expression)
    (
      ','
      (constant | fullColumnName | functionCall | expression)
    )*
    ;

functionArg
    : constant | fullColumnName | functionCall | expression
    ;

====

Accordingly functionCall rule is changed by separating scalar functions
to nested functions and non-nested functons.
Current related rules: functionCall, scalarFunction
Original rule:
functionCall
    : specificFunction                                              #specificFunctionCall
    | aggregateWindowedFunction                                     #aggregateFunctionCall
    | scalarFunctionName '(' functionArgs? ')'                      #scalarFunctionCall
    | fullId '(' functionArgs? ')'                                  #udfFunctionCall
    ;
*/

functionArgs
    : (constant | fullColumnName | expression)
    (
      ','
      (constant | fullColumnName | expression)
    )*
    ;

functionArg
    : constant | fullColumnName |  expression
    ;

nestedFunctionArgs
    : functionCall (',' functionArgs)?
    ;


//    Expressions, predicates

// Simplified approach for expression
expression
    : notOperator=(NOT | '!') expression                            #notExpression
    | expression logicalOperator expression                         #logicalExpression
    | predicate IS NOT? testValue=(TRUE | FALSE | MISSING)          #isExpression
    | predicate                                                     #predicateExpression
    ;

predicate
    : predicate NOT? IN '(' (selectStatement | expressions) ')'     #inPredicate
    | predicate IS nullNotnull                                      #isNullPredicate
    | left=predicate comparisonOperator right=predicate             #binaryComparisonPredicate
    | predicate NOT? BETWEEN predicate AND predicate                #betweenPredicate
    | predicate NOT? LIKE predicate                                 #likePredicate
    | predicate NOT? regex=REGEXP predicate                         #regexpPredicate
    | expressionAtom                                                #expressionAtomPredicate
    ;


// Add in ASTVisitor nullNotnull in constant
expressionAtom
    : constant                                                      #constantExpressionAtom
    | fullColumnName                                                #fullColumnNameExpressionAtom
    | functionCall                                                  #functionCallExpressionAtom
    | unaryOperator expressionAtom                                  #unaryExpressionAtom
    | '(' expression (',' expression)* ')'                          #nestedExpressionAtom
    | EXISTS '(' selectStatement ')'                                #existsExpessionAtom
    | '(' selectStatement ')'                                       #subqueryExpessionAtom
    | left=expressionAtom bitOperator right=expressionAtom          #bitExpressionAtom
    | left=expressionAtom mathOperator right=expressionAtom         #mathExpressionAtom
    ;

unaryOperator
    : '!' | '~' | '+' | '-' | NOT
    ;

comparisonOperator
    : '=' | '>' | '<' | '<' '=' | '>' '='
    | '<' '>' | '!' '='
    ;

logicalOperator
    : AND | '&' '&' | OR | '|' '|'
    ;

bitOperator
    : '<' '<' | '>' '>' | '&' | '^' | '|'
    ;

mathOperator
    : '*' | '/' | '%' | DIV | MOD | '+' | '-'
    ;


//    Simple id sets
//     (that keyword, which can be id)

keywordsCanBeId
    : FULL
    | FIELD | D | T | TS // OD SQL and ODBC special
    | COUNT | MIN | MAX | AVG | SUM
    ;

functionNameBase
    : esFunctionNameBase
    | ABS | ACOS | ADD | ASCII | ASIN | ATAN | ATAN2 | CBRT | CEIL | CONCAT | CONCAT_WS
    | COS | COSH | COT | CURDATE | DATE | DATE_FORMAT | DAYOFMONTH | DEGREES
    | E | EXP | EXPM1 | FLOOR | IF | IFNULL | ISNULL | LEFT | LENGTH | LN | LOCATE | LOG
    | LOG10 | LOG2 | LOWER | LTRIM | MAKETIME | MODULUS | MONTH | MONTHNAME | MULTIPLY
    | NOW | PI | POW | POWER | RADIANS | RAND | REPLACE | RIGHT | RINT | ROUND | RTRIM
    | SIGN | SIGNUM | SIN | SINH | SQRT | SUBSTRING | SUBTRACT | TAN | TIMESTAMP | TRIM
    | UPPER | YEAR | ADDDATE | ADDTIME | GREATEST | LEAST
    ;

esFunctionNameBase
    : DATE_HISTOGRAM | DAY_OF_MONTH | DAY_OF_YEAR | DAY_OF_WEEK | EXCLUDE
    | EXTENDED_STATS | FILTER | GEO_BOUNDING_BOX | GEO_CELL | GEO_DISTANCE | GEO_DISTANCE_RANGE | GEO_INTERSECTS
    | GEO_POLYGON | INCLUDE | IN_TERMS | HISTOGRAM | HOUR_OF_DAY
    | MATCHPHRASE | MATCH_PHRASE | MATCHQUERY | MATCH_QUERY | MINUTE_OF_DAY
    | MINUTE_OF_HOUR | MISSING | MONTH_OF_YEAR | MULTIMATCH | MULTI_MATCH | NESTED
    | PERCENTILES | QUERY | RANGE | REGEXP_QUERY | REVERSE_NESTED | SCORE
    | SECOND_OF_MINUTE | STATS | TERM | TERMS | TOPHITS
    | WEEK_OF_YEAR | WILDCARDQUERY | WILDCARD_QUERY
    ;
