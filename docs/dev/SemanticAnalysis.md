# Semantic Analysis

---
## 1.Overview

Previously SQL plugin didn't do semantic analysis, for example field doesn't exist, function call on field with wrong type etc. So it had to rely on Elasticsearch engine to perform the "check" which is actual execution. 
This led to bad user experience because of missing careful verification, cost of actual execution and confusing error message. In this work, we built a new semantic analyzer based on the new ANTLR generated parser introduced recently.
With the new semantic analyzer, we manage to perform various verification in terms of meaning of the query and return clear and helpful message to user for troubleshoot.

---
## 2.Use Cases

Here are a few examples of semantic check with our analyzer implemented. You could get a taste of what is the benefit of new semantic analyzer:

### 2.1 Field Does Not Exist

```
POST _opendistro/_sql
{
  "query": "SELECT balace FROM accounts"
}

{
  "error": {
    "reason": "Invalid SQL query",
    "details": "Field [balace] cannot be found or used here. Did you mean [balance]?",
    "type": "SemanticAnalysisException"
  },
  "status": 400
}
```

### 2.2 Function Call on Wrong Field Type

```
POST _opendistro/_sql
{
  "query": "SELECT * FROM accounts WHERE SUBSTRING(balance, 0, 1) = 'test'"
}

{
  "error": {
    "reason": "Invalid SQL query",
    "details": "Function [SUBSTRING] cannot work with [LONG, INTEGER, INTEGER]. Usage: SUBSTRING(STRING T, INTEGER, INTEGER) -> T",
    "type": "SemanticAnalysisException"
  },
  "status": 400
}
```

### 2.3 An index Join Non-nested Field

```
POST _opendistro/_sql
{
  "query": "SELECT * FROM accounts a, a.firstname"
}

{
  "error": {
    "reason": "Invalid SQL query",
    "details": "Field [a.firstname] is [TEXT] type but nested type is required.",
    "type": "SemanticAnalysisException"
  },
  "status": 400
}
```

### 2.4 Wrong Reference in Subquery

```
POST _opendistro/_sql
{
  "query": "SELECT * FROM accounts a WHERE EXISTS (SELECT * FROM accounts b WHERE b.address LIKE 'Seattle') AND b.age > 10"
}

{
  "error": {
    "reason": "Invalid SQL query",
    "details": "Field [b.age] cannot be found or used here. Did you mean [a.age]?",
    "type": "SemanticAnalysisException"
  },
  "status": 400
}
```

### 2.5 Operator Use on Wrong Field Type

```
POST _opendistro/_sql
{
  "query": "SELECT * FROM accounts WHERE lastname IS FALSE"
}


```

---
## 3.Design

The semantic analyzer consists of 2 core components: semantic context and type system.

### 3.1 Semantic Context

Semantic context manages Environment in a stack for scope management for nested subquery. Precisely, an Environment is the storage of symbols associated with its type. To perform type analysis, looking up in the environments in the semantic context is first step.

### 3.2 Type System

Type system allows for type check for all symbols present in the SQL query. First of all, we need to define what is type. Typically, type consists of 2 kinds:

 * **Base type**: is based on Elasticsearch data type and organized into hierarchy with "class" type internally. For example, INTEGER and LONG belongs to NUMBER, TEXT and KEYWORD belongs to STRING.
 * **Type expression**: is expression of multiple base type as argument type along with a constructor, for example, array constructor can construct integer to integer array, struct constructor can construct couple of base type into a new struct type. Similarly, function and comparison operator accepts arguments and generate result type. 

What needs to be covered:

 * **Generic type**:
 * **Vararg**:
 * **Overloaded**:
 * **Named argument**:
 * **Optional argument**:

### 3.3 Parse Tree Visitor


---
## 4.What's Next