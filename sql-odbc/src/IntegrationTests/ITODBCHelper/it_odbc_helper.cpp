/*
 * Copyright <2019> Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

#include "it_odbc_helper.h"

#include <codecvt>
#include <locale>

#define EXECUTION_HANDLER(throw_on_error, log_diag, handle_type, handle, \
                          ret_code, statement, error_msg)                \
    do {                                                                 \
        (ret_code) = (statement);                                        \
        if ((log_diag))                                                  \
            LogAnyDiagnostics((handle_type), (handle), (ret_code));      \
        if ((throw_on_error) && !SQL_SUCCEEDED((ret_code)))              \
            throw std::runtime_error((error_msg));                       \
    } while (0);

void AllocConnection(SQLHENV* db_environment, SQLHDBC* db_connection,
                     bool throw_on_error, bool log_diag) {
    SQLRETURN ret_code;
    EXECUTION_HANDLER(
        throw_on_error, log_diag, SQL_HANDLE_ENV, *db_environment, ret_code,
        SQLAllocHandle(SQL_HANDLE_ENV, SQL_NULL_HANDLE, db_environment),
        "Failed to allocate handle for environment.");
    EXECUTION_HANDLER(throw_on_error, log_diag, SQL_ATTR_ODBC_VERSION,
                      *db_environment, ret_code,
                      SQLSetEnvAttr(*db_environment, SQL_ATTR_ODBC_VERSION,
                                    (void*)SQL_OV_ODBC3, 0),
                      "Failed to set attributes for environment.");
    EXECUTION_HANDLER(
        throw_on_error, log_diag, SQL_HANDLE_DBC, *db_connection, ret_code,
        SQLAllocHandle(SQL_HANDLE_DBC, *db_environment, db_connection),
        "Failed to allocate handle for db connection.");
}

void ITDriverConnect(SQLTCHAR* connection_string, SQLHENV* db_environment,
                     SQLHDBC* db_connection, bool throw_on_error,
                     bool log_diag) {
    SQLRETURN ret_code;
    EXECUTION_HANDLER(
        throw_on_error, log_diag, SQL_HANDLE_ENV, *db_environment, ret_code,
        SQLAllocHandle(SQL_HANDLE_ENV, SQL_NULL_HANDLE, db_environment),
        "Failed to allocate handle for environment.");
    EXECUTION_HANDLER(throw_on_error, log_diag, SQL_ATTR_ODBC_VERSION,
                      *db_environment, ret_code,
                      SQLSetEnvAttr(*db_environment, SQL_ATTR_ODBC_VERSION,
                                    (void*)SQL_OV_ODBC3, 0),
                      "Failed to set attributes for environment.");
    EXECUTION_HANDLER(
        throw_on_error, log_diag, SQL_HANDLE_DBC, *db_connection, ret_code,
        SQLAllocHandle(SQL_HANDLE_DBC, *db_environment, db_connection),
        "Failed to allocate handle for db connection.");

    SQLTCHAR out_conn_string[1024];
    SQLSMALLINT out_conn_string_length;

    EXECUTION_HANDLER(
        throw_on_error, log_diag, SQL_HANDLE_DBC, *db_connection, ret_code,
        SQLDriverConnect(*db_connection, NULL, connection_string, SQL_NTS,
                         out_conn_string, IT_SIZEOF(out_conn_string),
                         &out_conn_string_length, SQL_DRIVER_COMPLETE),
        "Failed to connect to driver.");
}

void AllocStatement(SQLTCHAR* connection_string, SQLHENV* db_environment,
                    SQLHDBC* db_connection, SQLHSTMT* h_statement,
                    bool throw_on_error, bool log_diag) {
    SQLRETURN ret_code;
    ITDriverConnect(connection_string, db_environment, db_connection,
                    throw_on_error, log_diag);
    EXECUTION_HANDLER(
        throw_on_error, log_diag, SQL_HANDLE_STMT, h_statement, ret_code,
        SQLAllocHandle(SQL_HANDLE_STMT, *db_connection, h_statement),
        "Failed to allocate handle for statement.");
}

void LogAnyDiagnostics(SQLSMALLINT handle_type, SQLHANDLE handle, SQLRETURN ret,
                       SQLTCHAR* msg_return, const SQLSMALLINT sz) {
    if (handle == NULL) {
        printf("Failed to log diagnostics, handle is NULL\n");
        return;
    }

    // Only log diagnostics when there's something to log.
    switch (ret) {
        case SQL_SUCCESS_WITH_INFO:
            printf("SQL_SUCCESS_WITH_INFO: ");
            break;
        case SQL_ERROR:
            printf("SQL_ERROR: ");
            break;
        default:
            return;
    }

    SQLRETURN diag_ret;
    SQLTCHAR sqlstate[6];
    SQLINTEGER native_error_code;
    SQLTCHAR diag_message[SQL_MAX_MESSAGE_LENGTH];
    SQLSMALLINT message_length;

    SQLSMALLINT rec_number = 0;
    do {
        rec_number++;
        diag_ret = SQLGetDiagRec(
            handle_type, handle, rec_number, sqlstate, &native_error_code,
            msg_return == NULL ? diag_message : msg_return,
            msg_return == NULL ? IT_SIZEOF(diag_message) : sz, &message_length);
        if (diag_ret == SQL_INVALID_HANDLE)
            printf("Invalid handle\n");
        else if (SQL_SUCCEEDED(diag_ret))
            printf("SQLState: %S: %S\n", sqlstate,
                   (msg_return == NULL) ? diag_message : msg_return);
    } while (diag_ret == SQL_SUCCESS);

    if (diag_ret == SQL_NO_DATA && rec_number == 1)
        printf("No error information\n");
}

bool CheckSQLSTATE(SQLSMALLINT handle_type, SQLHANDLE handle,
                   SQLWCHAR* expected_sqlstate, bool log_message) {
    (void)log_message;

    SQLWCHAR sqlstate[6] = {0};
    SQLINTEGER native_diag_code;
    SQLWCHAR diag_message[SQL_MAX_MESSAGE_LENGTH] = {0};
    SQLSMALLINT message_length;

    SQLSMALLINT record_number = 0;
    SQLRETURN diag_ret;
    do {
        record_number++;
        diag_ret = SQLGetDiagRec(handle_type, handle, record_number, sqlstate,
                                 &native_diag_code, diag_message,
                                 IT_SIZEOF(diag_message), &message_length);

        if (1) {
            std::wcout << "SQLState " << sqlstate << ": " << diag_message
                       << std::endl;
        }
        // Only return if this SQLSTATE is the expected state, otherwise keep
        // checking
        if (std::wstring(sqlstate) == std::wstring(expected_sqlstate)) {
            return true;
        }
    } while (diag_ret == SQL_SUCCESS);

    // Could not find expected SQLSTATE in available diagnostic records
    return false;
}

bool CheckSQLSTATE(SQLSMALLINT handle_type, SQLHANDLE handle,
                   SQLWCHAR* expected_sqlstate) {
    return CheckSQLSTATE(handle_type, handle, expected_sqlstate, false);
}

std::wstring QueryBuilder(const std::wstring& column,
                          const std::wstring& dataset,
                          const std::wstring& count) {
    return L"SELECT " + column + L" FROM " + dataset + L" LIMIT " + count;
}

std::wstring QueryBuilder(const std::wstring& column,
                          const std::wstring& dataset) {
    return L"SELECT " + column + L" FROM " + dataset;
}

void CloseCursor(SQLHSTMT* h_statement, bool throw_on_error, bool log_diag) {
    SQLRETURN ret_code;
    EXECUTION_HANDLER(throw_on_error, log_diag, SQL_HANDLE_STMT, *h_statement,
                      ret_code, SQLCloseCursor(*h_statement),
                      "Failed to set allocate handle for statement.");
}

// std::wstring SQLTCHAR_to_string(SQLTCHAR* src) {
// 	// std::wstring _src = (char16_t*)src;
// 	return wstring_to_string(_src);
// }

std::string u16string_to_string(const std::u16string& src) {
    return std::wstring_convert< std::codecvt_utf8_utf16< char16_t >,
                                 char16_t >{}
        .to_bytes(src);
}

std::u16string string_to_u16string(const std::string& src) {
    return std::wstring_convert< std::codecvt_utf8_utf16< char16_t >,
                                 char16_t >{}
        .from_bytes(src);
}
