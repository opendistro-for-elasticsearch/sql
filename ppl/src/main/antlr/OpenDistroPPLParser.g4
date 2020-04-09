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
    : (SEARCH)? fromClause                                          #searchWithoutFilter
    | (SEARCH)? fromClause logicalExpression                        #searchFromClauseLogicExpr
    | (SEARCH)? logicalExpression fromClause                        #searchLogicExprFromClause
    ;

whereCommand
    : WHERE evalExpression
    ;

fieldsCommand
    : FIELDS fieldList
    ;

renameCommand
    : RENAME wcField AS wcField
    ;

statsCommand
    : STATS (statsArgument)* (statsAggTerm | sparklineAggTerm) (byClause)? (dedupSplitValues)?
    ;

dedupCommand
    : DEDUP (number=decimalLiteral)? fieldList (dedupArgument)* (SORTBY sortbyClause)?
    ;

sortCommand
    : SORT (count=decimalLiteral)? sortbyClause (D | DESC)?
    ;

evalCommand
    : EVAL fieldExpression EQUAL evalExpression (COMMA fieldExpression EQUAL evalExpression)
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
    : (PLUS | MINUS) sortField (COMMA (PLUS | MINUS) sortField)*
    ;

/** aggregation terms */
aggregationTerm
    : statsAggTerm | sparklineAggTerm
    ;

statsAggTerm
    : statsFunction LT_PRTHS (fieldExpression)? RT_PRTHS
    ;

sparklineAggTerm
    : sparklineAggregation | sparklineFunction
    ;

/** aggregation functions */
statsFunction
    : aggregationFunction | eventOrderFunction | multivalueStatsChartFunction | timeFunction
    ;

aggregationFunction
    : aggFunctionName LT_PRTHS fieldExpression RT_PRTHS
    | percentileAggFunction
    ;

aggFunctionName
    : AVG | COUNT | DISTINCT_COUNT | ESTDC | ESTDC_ERROR | MAX | MEAN | MEDIAN | MIN | MODE | RANGE
    | STDEV | STDEVP | SUM | SUMSQ | VAR | VARP
    ;

percentileAggFunction
    : PERCENTILE LESS fieldExpression GREATER LT_PRTHS fieldExpression RT_PRTHS
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
    : SPARKLINE LT_PRTHS COUNT LT_PRTHS wcField RT_PRTHS COMMA spanLength=decimalLiteral RT_PRTHS
    | SPARKLINE RT_PRTHS sparklineFunction LT_PRTHS wcField RT_PRTHS COMMA spanLength=decimalLiteral RT_PRTHS
    ;

sparklineFunction
    : sparklineFunctionName LT_PRTHS fieldExpression RT_PRTHS
    ;

sparklineFunctionName
    : C | COUNT | DC | MEAN | AVG | STDEV | STDEVP | VAR | VARP | SUM | SUMSQ | MIN | MAX | RANGE
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
    | left=logicalExpression OR right=logicalExpression             #logicalOrBinary
    | left=logicalExpression (AND)? right=logicalExpression         #logicalAndBinary
    ;

evalExpression
    : literalValue EQUAL literalValue
    | evalFunctionCall
    ;

comparisonExpression
    : left=fieldExpression comparisonOperator right=literalValue
    | fieldExpression IN valueList
    ;

booleanExpression
    : LT_PRTHS booleanExpression RT_PRTHS
    | booleanLiteral
    ;

/** tables */
tableSource
    : ident
    ;

/** fields */
fieldList
    : fieldExpression (COMMA fieldExpression)*
    ;

sortField
    : fieldExpression                                               #defaultSort
    | AUTO LT_PRTHS fieldExpression RT_PRTHS                        #autoSort
    | STR LT_PRTHS fieldExpression RT_PRTHS                         #strSort
    | IP LT_PRTHS fieldExpression RT_PRTHS                          #ipSort
    | NUM LT_PRTHS fieldExpression RT_PRTHS                         #numSort
    ;

wcField
    : WCFIELD
    ;

fieldExpression
    : ident
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
    : constant | fullColumnName | expression | evalFunctionCall
    ;

/** operators */
comparisonOperator
    : EQUAL | NOT_EQUAL | LESS | NOT_LESS | GREATER | NOT_GREATER
    ;

/** literals and values*/
literalValue
    : stringLiteral
    | decimalLiteral
    ;

stringLiteral
    : STRING_LITERAL
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

fullColumnName
    : simpleId DOT_ID*
    ;

constant
    : stringLiteral | decimalLiteral
    | MINUS decimalLiteral
    | booleanLiteral
    ;

simpleId
    : ID
    | DOT_ID
    | STRING_LITERAL
    ;

ident
    : ID
    | DOT_ID
    ;

/** dataset */
datasetType
    : DATAMODEL | LOOKUP | SAVEDSEARCH
    ;

datasetName
    :
    ;
