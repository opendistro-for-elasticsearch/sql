/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */


parser grammar OpenDistroPPLParser;
options { tokenVocab=OpenDistroPPLLexer; }

root
    : pplStatement? EOF
    ;

/** statement */
pplStatement
    : searchCommand (PIPE commands)*
    ;

/** commands */
commands
    : whereCommand | fieldsCommand | renameCommand | statsCommand | dedupCommand | sortCommand | evalCommand
    ;

searchCommand
    : (SEARCH)? fromClause                                          #searchFrom
    | (SEARCH)? fromClause logicalExpression                        #searchFromFilter
    | (SEARCH)? logicalExpression fromClause                        #searchFilterFrom
    ;

whereCommand
    : WHERE logicalExpression
    ;

fieldsCommand
    : FIELDS (PLUS | MINUS)? wcFieldList
    ;

renameCommand
    : RENAME orignalField=wcFieldExpression AS renamedField=wcFieldExpression
    ;

statsCommand
    : STATS (statsArgument)* statsAggTerm (byClause)? (dedupSplitValues)?
    ;

dedupCommand
    : DEDUP (number=decimalLiteral)? fieldList (dedupArgument)* (SORTBY sortbyClause)?
    ;

sortCommand
    : SORT (count=decimalLiteral)? sortbyClause (D | DESC)?
    ;

evalCommand
    : EVAL evalExpression (COMMA evalExpression)*
    ;

/** arguments */
dedupArgument
    : KEEPEVENTS EQUAL booleanLiteral                                #keepevents
    | KEEPEMPTY EQUAL booleanLiteral                                 #keepempty
    | CONSECUTIVE EQUAL booleanLiteral                               #consecutive
    ;

dedupSplitValues
    : DEDUP_SPLITVALUES EQUAL booleanLiteral
    ;

statsArgument
    : PARTITIONS EQUAL decimalLiteral                                #partitions
    | ALLNUM EQUAL booleanLiteral                                    #allnum
    | DELIM EQUAL stringLiteral                                      #delim
    ;

/** clauses */
fromClause
    : SOURCE EQUAL tableSource
    | INDEX EQUAL tableSource
    ;

byClause
    : BY fieldList
    ;

sortbyClause
    : (PLUS | MINUS)? sortField (COMMA (PLUS | MINUS)? sortField)*
    ;

/** aggregation terms */
statsAggTerm
    : statsFunction
    ;

sparklineAggTerm
    : sparklineAggregation | sparklineFunction
    ;

/** aggregation functions */
statsFunction
    : aggregationFunction | eventOrderFunction | multivalueStatsChartFunction | timeFunction
    ;

aggregationFunction
    : aggFunctionName LT_PRTHS fieldExpression RT_PRTHS             #aggFunctionCall
    | percentileAggFunction                                         #percentileAggFunctionCall
    ;

aggFunctionName
    : AVG | COUNT | DISTINCT_COUNT | ESTDC | ESTDC_ERROR | MAX | MEAN | MEDIAN | MIN | MODE | RANGE
    | STDEV | STDEVP | SUM | SUMSQ | VAR | VARP
    ;

percentileAggFunction
    : PERCENTILE LESS value=decimalLiteral GREATER LT_PRTHS aggField=fieldExpression RT_PRTHS
    ;

eventOrderFunction
    : FIRST LT_PRTHS fieldExpression RT_PRTHS
    | LAST LT_PRTHS fieldExpression RT_PRTHS
    ;

multivalueStatsChartFunction
    : LIST LT_PRTHS fieldExpression RT_PRTHS
    | VALUES LT_PRTHS fieldExpression RT_PRTHS
    ;

timeFunction
    : timeFunctionName LT_PRTHS fieldExpression RT_PRTHS
    ;

timeFunctionName
    : EARLIEST | EARLIEST_TIME | LATEST | LATEST_TIME | PER_DAY | PER_HOUR | PER_MINUTE | PER_SECOND | RATE
    ;

sparklineAggregation
    : SPARKLINE LT_PRTHS COUNT LT_PRTHS wcFieldExpression RT_PRTHS COMMA spanLength=decimalLiteral RT_PRTHS
    | SPARKLINE RT_PRTHS sparklineFunction LT_PRTHS wcFieldExpression RT_PRTHS COMMA spanLength=decimalLiteral RT_PRTHS
    ;

sparklineFunction
    : sparklineFunctionName LT_PRTHS fieldExpression RT_PRTHS
    ;

sparklineFunctionName
    :
//    | C | COUNT | DC | MEAN | AVG | STDEV | STDEVP | VAR | VARP | SUM | SUMSQ | MIN | MAX | RANGE
    ;

/** expressions */
expression
    : logicalExpression
    | booleanExpression
    | comparisonExpression
    | evalExpression
    ;

logicalExpression
    : booleanExpression                                             #booleanLabel
    | comparisonExpression                                          #comparsion
    | evalExpression                                                #eval
    | NOT logicalExpression                                         #logicalNot
    | left=logicalExpression OR right=logicalExpression             #logicalOr
    | left=logicalExpression (AND)? right=logicalExpression         #logicalAnd
    ;

evalExpression
    : fieldExpression EQUAL evalFunctionCall
    ;

comparisonExpression
    : left=fieldExpression comparisonOperator right=literalValue    #compareExpr
    | fieldExpression IN valueList                                  #inExpr
    ;

booleanExpression
    : LT_PRTHS booleanLiteral RT_PRTHS
    | booleanLiteral
    ;

/** tables */
tableSource
    : ident
    | stringLiteral
    ;

/** fields */
fieldList
    : fieldExpression (COMMA fieldExpression)*
    ;

wcFieldList
    : wcFieldExpression (COMMA wcFieldExpression)*
    ;

sortField
    : fieldExpression                                               #defaultSort
    | AUTO LT_PRTHS fieldExpression RT_PRTHS                        #autoSort
    | STR LT_PRTHS fieldExpression RT_PRTHS                         #strSort
    | IP LT_PRTHS fieldExpression RT_PRTHS                          #ipSort
    | NUM LT_PRTHS fieldExpression RT_PRTHS                         #numSort
    ;

fieldExpression
    : ident
    | SINGLE_QUOTE ident SINGLE_QUOTE
    | DOUBLE_QUOTE ident DOUBLE_QUOTE
    | BACKTICK ident BACKTICK
    ;

wcFieldExpression
    : wildcard
    ;


/** functions */
evalFunctionCall
    : evalFunctionName LT_PRTHS functionArgs RT_PRTHS
    ;

evalFunctionName
    : basicFunctionNameBase | esFunctionNameBase
    ;

basicFunctionNameBase
    : ABS | ACOS | ADD | ASCII | ASIN | ATAN | ATAN2 | CBRT | CEIL | CONCAT | CONCAT_WS
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

functionArgs
    : functionArg (COMMA functionArg)*
    ;

functionArg
    : expression | fieldExpression | literalValue
    ;

/** operators */
comparisonOperator
    : EQUAL | NOT_EQUAL | LESS | NOT_LESS | GREATER | NOT_GREATER
    ;

/** literals and values*/
literalValue
    : stringLiteral
    | (PLUS | MINUS)? integerLiteral
    | (PLUS | MINUS)? decimalLiteral
    | booleanLiteral
    ;

stringLiteral
    : DQUOTA_STRING | SQUOTA_STRING | BQUOTA_STRING
    ;

integerLiteral
    : INTEGER_LITERAL
    ;

decimalLiteral
    : DECIMAL_LITERAL
    ;

booleanLiteral
    : TRUE | FALSE
    ;

valueList
    : LT_PRTHS literalValue (COMMA literalValue)* RT_PRTHS
    ;

ident
    : (DOT)? ID
    ;

wildcard
    : (MODULE | ident)+
    ;
