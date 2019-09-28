# Semantic Analysis

---
## 1.Overview

Previously SQL plugin didn't do semantic analysis, for example field doesn't exist, function call on field with wrong type etc. So it had to rely on Elasticsearch engine to perform the "check" which is actual execution. 
This led to bad user experience because of missing careful verification, cost of actual execution and confusing error message. In this work, we built a new semantic analyzer based on the new ANTLR generated parser introduced recently.
With the new semantic analyzer, we manage to perform various verification in terms of meaning of the query and return clear and helpful message to user for troubleshoot.

Here are a few examples of semantic check with our analyzer implemented:

a) Field doesn't exist:

```

```

---
## 2.Design

The semantic analyzer consists of 2 core components: semantic context and type system.

### 2.1 Semantic Context

Semantic context manages Environment in a stack for scope management for nested subquery.

### 2.2 Type System

Type system allows for type check for all symbols present in the SQL query.

---
## 3.What's Next