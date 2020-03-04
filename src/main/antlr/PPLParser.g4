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

parser grammar PPLParser;
options { tokenVocab=PPLLexer; }

root
    : pplStatement? EOF
    ;

pplStatement
    : searchCommands | reportsCommands
    (PIPE resultsCommands)?
    (PIPE (searchCommands | reportsCommands))*
    EOF
    ;

searchCommands
    : searchCommand
    ;

reportsCommands
    : topCommand
    ;

resultsCommands
    : filteringCommands
    ;

filteringCommands
    : fromCommand | whereCommand
    ;

searchCommand
    : (SEARCH)? fromClause logicalExpression*
    | (SEARCH)? logicalExpression fromClause
    ;

fromClause
    : SOURCE EQUAL_SYMBOL valueExpression
    | INDEX EQUAL_SYMBOL valueExpression
    ;
topCommand
    : TOP decimalLiteral? topOptions? fieldList byClause*
    ;

fromCommand
    : FROM datasetType COLON datasetName
    | FROM datasetType datasetName
    ;

whereCommand
    : WHERE evalExpression
    ;

logicalExpression
    : booleanExpression
    | comparisonExpression
    | evalExpression
    | NOT logicalExpression
    | logicalExpression OR logicalExpression
    | logicalExpression (AND)? logicalExpression
    ;

booleanExpression
    : LT_PRTHS booleanExpression RT_PRTHS
    | booleanLiteral
    ;

comparisonExpression
    : fieldExpression comparisonOperator valueExpression
    | fieldExpression IN valueList
    ;

evalExpression
    : literalValue EQUAL_SYMBOL literalValue
    | evalFunctionCall
    ;

expression
    : logicalExpression
    | booleanExpression
    | comparisonExpression
    | evalExpression
    ;

evalFunctionCall
    : evalFunctionName LT_PRTHS functionArgs RT_PRTHS
    ;

topOptions
    : COUNTFIELD EQUAL_SYMBOL stringLiteral                              #countfieldTopOption
    | LIMIT EQUAL_SYMBOL decimalLiteral                                  #limitTopOption
    ;

datasetType
    : DATAMODEL | LOOKUP | SAVEDSEARCH
    ;

datasetName
    : ID
    ;

byClause
    : BY fieldList
    ;

booleanLiteral
    : TRUE | FALSE
    ;

comparisonOperator
    : EQUAL_SYMBOL | NOT_EQUAL | LESS_SYMBOL | NOT_LESS | GREATER_SYMBOL | NOT_GREATER
    ;

evalFunctionName
    : evalFunctionNameBase
    ;

functionArgs
    : functionArg (COMMA functionArg)*
    ;

functionArg
    : constant | fullColumnName | expression | evalFunctionCall
    ;

fieldExpression
    : stringLiteral
    ;

fieldList
    : fieldExpression (COMMA fieldExpression)*
    ;

valueExpression
    : literalValue
    ;

valueList
    : LT_PRTHS literalValue (COMMA literalValue)* RT_PRTHS
    ;

constant
    : stringLiteral | decimalLiteral
    | MINUS decimalLiteral
    | booleanLiteral
    ;

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

fullColumnName
    : simpleId DOT_ID*
    ;

simpleId
    : ID
    | DOT_ID
    | STRING_LITERAL
    ;

evalFunctionNameBase
    : POW | ABS
    ;



