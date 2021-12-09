#ifndef _ES_STATEMENT_H_
#define _ES_STATEMENT_H_

#include "es_parse_result.h"
#include "qresult.h"
#include "statement.h"

#ifdef __cplusplus
extern "C" {
#endif
RETCODE RePrepareStatement(StatementClass *stmt);
RETCODE PrepareStatement(StatementClass* stmt, const SQLCHAR *stmt_str, SQLINTEGER stmt_sz);
RETCODE ExecuteStatement(StatementClass *stmt, BOOL commit);
QResultClass *SendQueryGetResult(StatementClass *stmt, BOOL commit);
RETCODE AssignResult(StatementClass *stmt);
SQLRETURN ESAPI_Cancel(HSTMT hstmt);
SQLRETURN GetNextResultSet(StatementClass *stmt);
void ClearESResult(void *es_result);
#ifdef __cplusplus
}
#endif

#endif
