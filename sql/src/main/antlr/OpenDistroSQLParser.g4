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

options { tokenVocab=OpenDistroSQLLexer; }


// Top Level Description

//    Root rule
root
    : sqlStatement? EOF
    ;

//    Only SELECT, DELETE, SHOW and DSCRIBE are supported for now
sqlStatement
    : dmlStatement
    ;

dmlStatement
    : selectStatement
    ;


// Data Manipulation Language

//    Primary DML Statements

selectStatement
    : SELECT selectElements fromClause whereClause?
    ;

//    Select Statement's Details

selectElement
    : expr=expression (AS? alias=uid)?
    ;

selectElements
    : selectElement (',' selectElement)*
    ;

fromClause
    : FROM tableName
    ;

whereClause
    : WHERE whereExpr=expression
    ;

tableName
    : uid
    ;

fullColumnName
    : uid
    ;

// Common Clauses

//    DB Objects

uid
    : simpleId
    | REVERSE_QUOTE_ID
    ;

simpleId
    : ID
    | DOT_ID  // note: the current scope by adding DOT_ID to simpleId is large, move DOT_ID upwards tablename if needed
    | STRING_LITERAL
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

// Simplified approach for expression
expression
    : expressionAtom; /*notOperator=(NOT | '!') expression                            #notExpression
    | expression logicalOperator expression                         #logicalExpression
    | predicate IS NOT? testValue=(TRUE | FALSE | MISSING)          #isExpression
    | predicate                                                     #predicateExpression
    ;
predicate
    : predicate NOT? IN '(' (selectStatement | expressions) ')'     #inPredicate
    | predicate IS nullNotnull                                      #isNullPredicate
    | left=predicate comparisonOperator right=predicate             #binaryComparisonPredicate
    | expressionAtom                                                #expressionAtomPredicate
    ;
*/

// Add in ASTVisitor nullNotnull in constant
expressionAtom
    : constant                                                      #constantExpressionAtom
    | fullColumnName                                                #fullColumnNameExpressionAtom
    | scalarFunction                                                #functionCallExpressionAtom
    | unaryOperator expressionAtom                                  #unaryExpressionAtom
    | '(' expression (',' expression)* ')'                          #nestedExpressionAtom
    | left=expressionAtom bitOperator right=expressionAtom          #bitExpressionAtom
    | left=expressionAtom mathOperator right=expressionAtom         #mathExpressionAtom
    | left=expressionAtom comparisonOperator right=expressionAtom   #binaryComparisonPredicate
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

scalarFunction
    : functionNameBase '(' functionArgs? ')'
    ;

functionArgs
    : (constant | fullColumnName | expression)
    (
      ','
      (constant | fullColumnName | expression)
    )*
    ;

functionNameBase
    : SUBSTRING
    ;