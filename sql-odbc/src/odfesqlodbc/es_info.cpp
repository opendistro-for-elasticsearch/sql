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
#include "es_info.h"

#include <stdio.h>
#include <string.h>

#include <algorithm>
#include <memory>
#include <sstream>
#include <string>
#include <unordered_map>
#include <vector>

// TODO #324 (SQL Plugin)- Update if Elasticsearch extends support for multiple
// tables
#define DEFAULT_TYPE_STR \
    { 'k', 'e', 'y', 'w', 'o', 'r', 'd', '\0' }
#define DEFAULT_TYPE_INT (SQL_WVARCHAR)
#define EMPTY_VARCHAR \
    { '\0' }
#define ES_UNINITIALIZED (-2)
#define COLUMN_TEMPLATE_COUNT 18
#define TABLE_TEMPLATE_COUNT 5

#define TABLE_CAT "TABLE_CAT"
#define TABLE_SCHEM "TABLE_SCHEM"
#define TABLE_NAME "TABLE_NAME"
#define COLUMN_NAME "COLUMN_NAME"
#define DATA_TYPE "DATA_TYPE"
#define TYPE_NAME "TYPE_NAME"
#define COLUMN_SIZE "COLUMN_SIZE"
#define BUFFER_LENGTH "BUFFER_LENGTH"
#define DECIMAL_DIGITS "DECIMAL_DIGITS"
#define NUM_PREC_RADIX "NUM_PREC_RADIX"
#define NULLABLE "NULLABLE"
#define REMARKS "REMARKS"
#define COLUMN_DEF "COLUMN_DEF"
#define SQL_DATA_TYPE "SQL_DATA_TYPE"
#define SQL_DATETIME_SUB "SQL_DATETIME_SUB"
#define CHAR_OCTET_LENGTH "CHAR_OCTET_LENGTH"
#define ORDINAL_POSITION "ORDINAL_POSITION"
#define IS_NULLABLE "IS_NULLABLE"
#define TABLE_QUALIFIER "TABLE_QUALIFIER"
#define TABLE_OWNER "TABLE_OWNER"
#define TABLE_TYPE "TABLE_TYPE"
#define PRECISION "PRECISION"
#define LITERAL_PREFIX "LITERAL_PREFIX"
#define LITERAL_SUFFIX "LITERAL_SUFFIX"
#define CREATE_PARAMS "CREATE_PARAMS"
#define CASE_SENSITIVE "CASE_SENSITIVE"
#define SEARCHABLE "SEARCHABLE"
#define UNSIGNED_ATTRIBUTE "UNSIGNED_ATTRIBUTE"
#define FIXED_PREC_SCALE "FIXED_PREC_SCALE"
#define AUTO_INCREMENT "AUTO_INCREMENT"
#define LOCAL_TYPE_NAME "LOCAL_TYPE_NAME"
#define MINIMUM_SCALE "MINIMUM_SCALE"
#define MAXIMUM_SCALE "MAXIMUM_SCALE"
#define INTERVAL_PRECISION "INTERVAL_PRECISION"

const std::unordered_map< int, std::vector< int > > sql_es_type_map = {
    {SQL_BIT, {ES_TYPE_BOOL}},
    {SQL_TINYINT, {ES_TYPE_INT1}},
    {SQL_SMALLINT, {ES_TYPE_INT2}},
    {SQL_INTEGER, {ES_TYPE_INT4}},
    {SQL_BIGINT, {ES_TYPE_INT8}},
    {SQL_REAL, {ES_TYPE_HALF_FLOAT, ES_TYPE_FLOAT4}},
    {SQL_DOUBLE, {ES_TYPE_FLOAT8, ES_TYPE_SCALED_FLOAT}},
    {SQL_WVARCHAR,
     {ES_TYPE_KEYWORD, ES_TYPE_TEXT, ES_TYPE_NESTED, ES_TYPE_OBJECT}},
    {SQL_TYPE_TIMESTAMP, {ES_TYPE_DATETIME}}};

// Boilerplate code for easy column bind handling
class BindTemplate {
   public:
    BindTemplate(const bool can_be_null, const SQLUSMALLINT ordinal)
        : m_len(ES_UNINITIALIZED), m_ordinal(ordinal) {
        if (!can_be_null)
            throw std::runtime_error(
                "Do not use this constructor for values that can be NULL. A "
                "constructor with "
                "supplied default value must be used if value can be NULL.");
    }
    BindTemplate(const bool can_be_null, const SQLUSMALLINT ordinal, const Int2)
        : m_len(ES_UNINITIALIZED), m_ordinal(ordinal) {
        (void)(can_be_null);
    }
    BindTemplate(const bool can_be_null, const SQLUSMALLINT ordinal, const Int4)
        : m_len(ES_UNINITIALIZED), m_ordinal(ordinal) {
        (void)(can_be_null);
    }
    BindTemplate(const bool can_be_null, const SQLUSMALLINT ordinal,
                 const std::vector< SQLCHAR > &)
        : m_len(ES_UNINITIALIZED), m_ordinal(ordinal) {
        (void)(can_be_null);
    }
    virtual ~BindTemplate() {
    }

    SQLPOINTER GetData() {
        if (m_len == ES_UNINITIALIZED)
            throw std::runtime_error(
                "Length is uninitialized - Fetch must be executed before data "
                "is retreived.");
        return (m_len == SQL_NULL_DATA) ? NULL : GetDataForBind();
    }

    void BindColumn(StatementClass *stmt) {
        RETCODE err = ESAPI_BindCol(stmt, m_ordinal, GetType(),
                                    GetDataForBind(), GetSize(), &m_len);
        if (!SQL_SUCCEEDED(err)) {
            std::string error_msg =
                "Failed to bind column with ordinal "
                + std::to_string(m_ordinal)
                + ". SQL Error code: " + std::to_string(err);
            throw std::runtime_error(error_msg.c_str());
        }
    }
    void AssignData(TupleField *tuple) {
        SQLPOINTER data = GetData();
        if ((data == NULL) || (m_len == SQL_NULL_DATA)) {
            set_tuplefield_null(tuple);
            return;
        }
        switch (GetType()) {
            case SQL_C_LONG:
                set_tuplefield_int4(tuple, *static_cast< Int4 * >(data));
                break;
            case SQL_C_SHORT:
                set_tuplefield_int2(tuple, *static_cast< Int2 * >(data));
                break;
            case SQL_C_CHAR:
                set_tuplefield_string(tuple, static_cast< const char * >(data));
                break;
            default:
                throw std::runtime_error(
                    std::string(
                        "Cannot convert unknown data type to tuplefield: "
                        + std::to_string(GetType()))
                        .c_str());
        }
    }
    BindTemplate(const BindTemplate &) = default;
    BindTemplate &operator=(const BindTemplate &) = default;
    virtual std::string AsString() = 0;
    virtual void UpdateData(SQLPOINTER new_data, size_t size) = 0;
    // virtual void UpdateDataNull() = 0;

   private:
    SQLLEN m_len;
    SQLUSMALLINT m_ordinal;

   protected:
    virtual SQLSMALLINT GetType() = 0;
    virtual SQLLEN GetSize() = 0;
    virtual SQLPOINTER GetDataForBind() = 0;
};

// 4 byte integer column
class BindTemplateInt4 : public BindTemplate {
   public:
    BindTemplateInt4(const bool nullable, const SQLUSMALLINT ordinal)
        : BindTemplate(nullable, ordinal), m_data(0) {
    }
    BindTemplateInt4(const bool nullable, const SQLUSMALLINT ordinal,
                     const Int4 data)
        : BindTemplate(nullable, ordinal, data), m_data(data) {
    }
    ~BindTemplateInt4() {
    }
    std::string AsString() {
        return std::to_string(*static_cast< Int4 * >(GetData()));
    }
    void UpdateData(SQLPOINTER new_data, size_t size) {
        (void)size;
        m_data = *(Int4 *)new_data;
    }
    // void UpdateDataNull() {
    //     m_data = NULL;
    // }

   private:
    Int4 m_data;

   protected:
    SQLPOINTER GetDataForBind() {
        return &m_data;
    }
    SQLSMALLINT GetType() {
        return SQL_C_LONG;
    }
    SQLLEN GetSize() {
        return static_cast< SQLLEN >(sizeof(Int4));
    }
};

// 2 byte integer column
class BindTemplateInt2 : public BindTemplate {
   public:
    BindTemplateInt2(const bool nullable, const SQLUSMALLINT ordinal)
        : BindTemplate(nullable, ordinal), m_data(0) {
    }
    BindTemplateInt2(const bool nullable, const SQLUSMALLINT ordinal,
                     const Int2 data)
        : BindTemplate(nullable, ordinal, data), m_data(data) {
    }
    ~BindTemplateInt2() {
    }
    std::string AsString() {
        return std::to_string(*static_cast< Int2 * >(GetData()));
    }
    void UpdateData(SQLPOINTER new_data, size_t size) {
        (void)size;
        m_data = *(Int2 *)new_data;
    }
    // void UpdateDataNull() {
    //     m_data = NULL;
    // }

   private:
    Int2 m_data;

   protected:
    SQLPOINTER GetDataForBind() {
        return &m_data;
    }
    SQLSMALLINT GetType() {
        return SQL_C_SHORT;
    }
    SQLLEN GetSize() {
        return static_cast< SQLLEN >(sizeof(Int2));
    }
};

// Varchar data
class BindTemplateSQLCHAR : public BindTemplate {
   public:
    BindTemplateSQLCHAR(const bool nullable, const SQLUSMALLINT ordinal)
        : BindTemplate(nullable, ordinal), m_data(MAX_INFO_STRING, '\0') {
    }
    BindTemplateSQLCHAR(const bool nullable, const SQLUSMALLINT ordinal,
                        const std::vector< SQLCHAR > &data)
        : BindTemplate(nullable, ordinal, data), m_data(MAX_INFO_STRING, '\0') {
        if (data.size() >= m_data.size()) {
            throw std::runtime_error(
                "Default data size exceeds max info string size.");
        } else {
            m_data.insert(m_data.begin(), data.begin(), data.end());
        }
    }
    ~BindTemplateSQLCHAR() {
    }
    std::string AsString() {
        char *bind_tbl_data_char = static_cast< char * >(GetData());
        return (bind_tbl_data_char == NULL) ? "" : bind_tbl_data_char;
    }
    void UpdateData(SQLPOINTER new_data, size_t size) {
        m_data.clear();
        SQLCHAR *data = (SQLCHAR *)new_data;
        for (size_t i = 0; i < size; i++) {
            m_data.push_back(*data++);
        }
        m_data.push_back(0);
    }
    // void UpdateDataNull() {
    //     m_data.clear();
    // }

   private:
    std::vector< SQLCHAR > m_data;

   protected:
    SQLPOINTER GetDataForBind() {
        // return m_data.size() == 0 ? NULL : m_data.data();
        return m_data.data();
    }
    SQLSMALLINT GetType() {
        return SQL_C_CHAR;
    }
    SQLLEN GetSize() {
        return static_cast< SQLLEN >(m_data.size());
    }
};

// Typedefs and macros to ease creation of BindTemplates
typedef std::unique_ptr< BindTemplate > bind_ptr;
typedef std::vector< bind_ptr > bind_vector;
#define _SQLCHAR_(...) \
    (std::make_unique< BindTemplateSQLCHAR >(BindTemplateSQLCHAR(__VA_ARGS__)))
#define _SQLINT2_(...) \
    (std::make_unique< BindTemplateInt2 >(BindTemplateInt2(__VA_ARGS__)))
#define _SQLINT4_(...) \
    (std::make_unique< BindTemplateInt4 >(BindTemplateInt4(__VA_ARGS__)))

// Common function definitions
enum class TableResultSet { Catalog, Schema, TableTypes, TableLookUp, All };
void ConvertToString(std::string &out, bool &valid, const SQLCHAR *sql_char,
                     const SQLSMALLINT sz);
QResultClass *SetupQResult(const bind_vector &cols, StatementClass *stmt,
                           StatementClass *col_stmt, const int col_cnt);
void CleanUp(StatementClass *stmt, StatementClass *sub_stmt, const RETCODE ret);
void ExecuteQuery(ConnectionClass *conn, HSTMT *stmt, const std::string &query);
void GetCatalogData(const std::string &query, StatementClass *stmt,
                    StatementClass *sub_stmt, const TableResultSet res_type,
                    std::string &table_type,
                    void (*populate_binds)(bind_vector &),
                    void (*setup_qres_info)(QResultClass *,
                                            EnvironmentClass *));

// Common function declarations
void ConvertToString(std::string &out, bool &valid, const SQLCHAR *sql_char,
                     const SQLSMALLINT sz) {
    valid = (sql_char != NULL);
    if (!valid) {
        out = "%";
    } else if (sz == SQL_NTS) {
        out.assign(reinterpret_cast< const char * >(sql_char));
    } else if (sz <= 0) {
        out = "";
    } else {
        out.assign(reinterpret_cast< const char * >(sql_char),
                   static_cast< size_t >(sz));
    }
}

QResultClass *SetupQResult(const bind_vector &cols, StatementClass *stmt,
                           StatementClass *col_stmt, const int col_cnt) {
    (void)(cols);
    (void)(col_stmt);

    // Initialize memory for data retreival
    QResultClass *res = NULL;
    if ((res = QR_Constructor()) == NULL) {
        SC_set_error(stmt, STMT_NO_MEMORY_ERROR,
                     "Couldn't allocate memory for Tables or Columns result.",
                     "FetchResults");
        throw std::runtime_error(
            "Couldn't allocate memory for Tables or Columns result.");
    }
    SC_set_Result(stmt, res);

    // The binding structure for a statement is not set up until a statement is
    // actually executed, so we'll have to do this ourselves
    extend_column_bindings(SC_get_ARDF(stmt),
                           static_cast< SQLSMALLINT >(col_cnt));
    QR_set_num_fields(res, col_cnt);

    return res;
}

void CleanUp(StatementClass *stmt, StatementClass *sub_stmt,
             const RETCODE ret = SQL_ERROR) {
    stmt->status = STMT_FINISHED;
    stmt->catalog_result = TRUE;

    if (!SQL_SUCCEEDED(ret) && 0 >= SC_get_errornumber(stmt))
        SC_error_copy(stmt, sub_stmt, TRUE);

    // set up the current tuple pointer for
    stmt->currTuple = -1;
    SC_set_rowset_start(stmt, -1, FALSE);
    SC_set_current_col(stmt, -1);

    if (sub_stmt)
        ESAPI_FreeStmt(sub_stmt, SQL_DROP);
}

void ExecuteQuery(ConnectionClass *conn, HSTMT *stmt,
                  const std::string &query) {
    // Prepare statement
    if (!SQL_SUCCEEDED(ESAPI_AllocStmt(conn, stmt, 0))) {
        throw std::runtime_error("Failed to allocate memory for statement.");
    }

    // Execute query
    if (!SQL_SUCCEEDED(ESAPI_ExecDirect(
            *stmt, reinterpret_cast< const SQLCHAR * >(query.c_str()), SQL_NTS,
            1))) {
        std::string error_msg = "Failed to execute query '" + query + "'.";
        throw std::runtime_error(error_msg.c_str());
    }
}

// Table specific function definitions
void split(const std::string &input, const std::string &delim,
           std::vector< std::string > &output);
void GenerateTableQuery(std::string &tables_query, const UWORD flag,
                        const std::string &table_name_value,
                        const TableResultSet result_type,
                        const bool table_valid);
void AssignTableBindTemplates(bind_vector &tabs);
void SetupTableQResInfo(QResultClass *res, EnvironmentClass *env);
void SetTableTuples(QResultClass *res, const TableResultSet res_type,
                    const bind_vector &bind_tbl, std::string &table_type,
                    StatementClass *stmt, StatementClass *tbl_stmt);

// Table specific function declarations
void split(const std::string &input, const std::string &delim,
           std::vector< std::string > &output) {
    size_t start = 0;
    size_t end = input.find(delim);
    while (end != std::string::npos) {
        output.push_back(input.substr(start, end - start));
        start = end + delim.length();
        end = input.find(delim, start);
    }
    output.push_back(input.substr(start, end));
}

// TODO #324 (SQL Plugin)- Fix patterns and escape characters for this
void GenerateTableQuery(std::string &tables_query, const UWORD flag,
                        const std::string &table_name_value,
                        const TableResultSet result_type,
                        const bool table_valid) {
    bool search_pattern = (~flag & PODBC_NOT_SEARCH_PATTERN);
    tables_query = "SHOW TABLES LIKE ";
    if (table_valid && (table_name_value != "")
        && (result_type == TableResultSet::All))
        tables_query +=
            search_pattern ? table_name_value : "^" + table_name_value + "$";
    else
        tables_query += "%";
}

// In case of unique_ptr's, using push_back (over emplace_back) is preferred in
// C++14 and higher
void AssignTableBindTemplates(bind_vector &tabs) {
    tabs.reserve(TABLE_TEMPLATE_COUNT);
    tabs.push_back(_SQLCHAR_(false, 1, EMPTY_VARCHAR));  // TABLE_CAT		1
    tabs.push_back(_SQLCHAR_(false, 2, EMPTY_VARCHAR));  // TABLE_SCHEM		2
    tabs.push_back(_SQLCHAR_(false, 3, EMPTY_VARCHAR));  // TABLE_NAME		3
    tabs.push_back(_SQLCHAR_(false, 4, EMPTY_VARCHAR));  // TABLE_TYPE		4
    tabs.push_back(_SQLCHAR_(true, 5));                  // REMARKS			5
}

void SetupTableQResInfo(QResultClass *res, EnvironmentClass *env) {
    if (EN_is_odbc3(env)) {
        QR_set_field_info_v(res, TABLES_CATALOG_NAME, TABLE_CAT,
                            ES_TYPE_VARCHAR, MAX_INFO_STRING);
        QR_set_field_info_v(res, TABLES_SCHEMA_NAME, TABLE_SCHEM,
                            ES_TYPE_VARCHAR, MAX_INFO_STRING);
    } else {
        QR_set_field_info_v(res, TABLES_CATALOG_NAME, TABLE_QUALIFIER,
                            ES_TYPE_VARCHAR, MAX_INFO_STRING);
        QR_set_field_info_v(res, TABLES_SCHEMA_NAME, TABLE_OWNER,
                            ES_TYPE_VARCHAR, MAX_INFO_STRING);
    }
    QR_set_field_info_v(res, TABLES_TABLE_NAME, TABLE_NAME, ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, TABLES_TABLE_TYPE, TABLE_TYPE, ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, TABLES_REMARKS, REMARKS, ES_TYPE_VARCHAR,
                        INFO_VARCHAR_SIZE);
}

void SetTableTuples(QResultClass *res, const TableResultSet res_type,
                    const bind_vector &bind_tbl, std::string &table_type,
                    StatementClass *stmt, StatementClass *tbl_stmt) {
    auto CheckResult = [&](const auto &res) {
        if (res != SQL_NO_DATA_FOUND) {
            SC_full_error_copy(stmt, tbl_stmt, FALSE);
            throw std::runtime_error(
                std::string("Failed to fetch data after query. Error code :"
                            + std::to_string(res))
                    .c_str());
        }
    };
    auto AssignData = [&](auto *res, const auto &binds) {
        TupleField *tuple = QR_AddNew(res);
        for (size_t i = 0; i < binds.size(); i++)
            binds[i]->AssignData(&tuple[i]);
    };

    // General case
    if (res_type == TableResultSet::All) {
        RETCODE result = SQL_NO_DATA_FOUND;
        while (SQL_SUCCEEDED(result = ESAPI_Fetch(tbl_stmt))) {
            if (bind_tbl[TABLES_TABLE_TYPE]->AsString() == "BASE TABLE") {
                std::string table("TABLE");
                bind_tbl[TABLES_TABLE_TYPE]->UpdateData(&table, table.size());
            }
            // if (bind_tbl[TABLES_CATALOG_NAME]->AsString() != "") {
            //     bind_tbl[TABLES_CATALOG_NAME]->UpdateDataNull();
            // }
            AssignData(res, bind_tbl);
        }
        CheckResult(result);
    } else if (res_type == TableResultSet::TableLookUp) {
        // Get accepted table types
        std::vector< std::string > table_types;
        table_type.erase(
            std::remove(table_type.begin(), table_type.end(), '\''),
            table_type.end());
        split(table_type, ",", table_types);

        // Loop through all data
        RETCODE result = SQL_NO_DATA_FOUND;
        while (SQL_SUCCEEDED(result = ESAPI_Fetch(tbl_stmt))) {
            // Replace BASE TABLE with TABLE for Excel & Power BI SQLTables call
            if (bind_tbl[TABLES_TABLE_TYPE]->AsString() == "BASE TABLE") {
                std::string table("TABLE");
                bind_tbl[TABLES_TABLE_TYPE]->UpdateData(&table, table.size());
            }
            if (bind_tbl[TABLES_CATALOG_NAME]->AsString() != "") {
                std::string table("");
                bind_tbl[TABLES_CATALOG_NAME]->UpdateData(&table, table.size());
            }
            if (std::find(table_types.begin(), table_types.end(),
                          bind_tbl[TABLES_TABLE_TYPE]->AsString())
                != table_types.end()) {
                AssignData(res, bind_tbl);
            }
        }

        CheckResult(result);

    }
    // Special cases - only need single grab for this one
    else {
        RETCODE result;
        if (!SQL_SUCCEEDED(result = ESAPI_Fetch(tbl_stmt))) {
            SC_full_error_copy(stmt, tbl_stmt, FALSE);
            throw std::runtime_error(
                std::string("Failed to fetch data after query. Error code :"
                            + std::to_string(result))
                    .c_str());
        }

        // Get index of result type of interest
        size_t idx = NUM_OF_TABLES_FIELDS;
        switch (res_type) {
            case TableResultSet::Catalog:
                MYLOG(ES_WARNING, "TRS::Catalog\n");
                return;
                idx = TABLES_CATALOG_NAME;
                break;
            case TableResultSet::Schema:
                MYLOG(ES_WARNING, "TRS::Schema\n");
                return;
                idx = TABLES_SCHEMA_NAME;
                break;
            case TableResultSet::TableTypes:
                MYLOG(ES_WARNING, "TRS::TableTypes\n");
                idx = TABLES_TABLE_TYPE;
                break;
            default:
                // This should not be possible, handle it anyway
                throw std::runtime_error(
                    "Result type is not an expected type.");
        }

        // if (bind_tbl[TABLES_CATALOG_NAME]->AsString() != "") {
        //     bind_tbl[TABLES_CATALOG_NAME]->UpdateDataNull();
        // }
        // Get new tuple and assign index of interest (NULL others)
        // TODO #324 (SQL Plugin)- Should these be unique?
        TupleField *tuple = QR_AddNew(res);
        for (size_t i = 0; i < bind_tbl.size(); i++) {
            if (i == idx)
                bind_tbl[i]->AssignData(&tuple[i]);
            else
                set_tuplefield_string(&tuple[i], NULL_STRING);
        }
    }
}

// Column specific function definitions
void SetupColumnQResInfo(QResultClass *res, EnvironmentClass *unused);
void GenerateColumnQuery(std::string &query, const std::string &table_name,
                         const std::string &column_name, const bool table_valid,
                         const bool column_valid, const UWORD flag);
void AssignColumnBindTemplates(bind_vector &cols);

// Column Specific function declarations
void SetupColumnQResInfo(QResultClass *res, EnvironmentClass *unused) {
    (void)(unused);

    QR_set_field_info_v(res, COLUMNS_CATALOG_NAME, TABLE_CAT, ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, COLUMNS_SCHEMA_NAME, TABLE_SCHEM, ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, COLUMNS_TABLE_NAME, TABLE_NAME, ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, COLUMNS_COLUMN_NAME, COLUMN_NAME, ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, COLUMNS_DATA_TYPE, DATA_TYPE, ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, COLUMNS_TYPE_NAME, TYPE_NAME, ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, COLUMNS_PRECISION, COLUMN_SIZE, ES_TYPE_INT4, 4);
    QR_set_field_info_v(res, COLUMNS_LENGTH, BUFFER_LENGTH, ES_TYPE_INT4, 4);
    QR_set_field_info_v(res, COLUMNS_SCALE, DECIMAL_DIGITS, ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, COLUMNS_RADIX, NUM_PREC_RADIX, ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, COLUMNS_NULLABLE, NULLABLE, ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, COLUMNS_REMARKS, REMARKS, ES_TYPE_VARCHAR,
                        INFO_VARCHAR_SIZE);
    QR_set_field_info_v(res, COLUMNS_COLUMN_DEF, COLUMN_DEF, ES_TYPE_VARCHAR,
                        INFO_VARCHAR_SIZE);
    QR_set_field_info_v(res, COLUMNS_SQL_DATA_TYPE, SQL_DATA_TYPE, ES_TYPE_INT2,
                        2);
    QR_set_field_info_v(res, COLUMNS_SQL_DATETIME_SUB, SQL_DATETIME_SUB,
                        ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, COLUMNS_CHAR_OCTET_LENGTH, CHAR_OCTET_LENGTH,
                        ES_TYPE_INT4, 4);
    QR_set_field_info_v(res, COLUMNS_ORDINAL_POSITION, ORDINAL_POSITION,
                        ES_TYPE_INT4, 4);
    QR_set_field_info_v(res, COLUMNS_IS_NULLABLE, IS_NULLABLE, ES_TYPE_VARCHAR,
                        INFO_VARCHAR_SIZE);
}

// TODO #325 (SQL Plugin)- Fix patterns and escape characters for this
void GenerateColumnQuery(std::string &query, const std::string &table_name,
                         const std::string &column_name, const bool table_valid,
                         const bool column_valid, const UWORD flag) {
    bool search_pattern = (~flag & PODBC_NOT_SEARCH_PATTERN);
    query = "DESCRIBE TABLES LIKE ";
    query += table_valid
                 ? (search_pattern ? table_name : "^" + table_name + "$")
                 : "%";
    if (column_valid)
        query += " COLUMNS LIKE " + column_name;
}

// In case of unique_ptr's, using push_back (over emplace_back) is preferred in
// C++14 and higher
void AssignColumnBindTemplates(bind_vector &cols) {
    cols.reserve(COLUMN_TEMPLATE_COUNT);
    cols.push_back(_SQLCHAR_(true, 1));                  // TABLE_CAT			1
    cols.push_back(_SQLCHAR_(true, 2));                  // TABLE_SCHEM			2
    cols.push_back(_SQLCHAR_(false, 3, EMPTY_VARCHAR));  // TABLE_NAME 3
    cols.push_back(_SQLCHAR_(false, 4, EMPTY_VARCHAR));  // COLUMN_NAME 4
    cols.push_back(
        _SQLINT2_(false, 5, DEFAULT_TYPE_INT));  // DATA_TYPE			5
    cols.push_back(
        _SQLCHAR_(false, 6, DEFAULT_TYPE_STR));  // TYPE_NAME			6
    cols.push_back(_SQLINT4_(true, 7));          // COLUMN_SIZE			7
    cols.push_back(_SQLINT4_(true, 8));          // BUFFER_LENGTH		8
    cols.push_back(_SQLINT2_(true, 9));          // DECIMAL_DIGITS		9
    cols.push_back(_SQLINT2_(true, 10));         // NUM_PREC_RADIX		10
    cols.push_back(
        _SQLINT2_(false, 11, SQL_NULLABLE_UNKNOWN));  // NULLABLE			11
    cols.push_back(_SQLCHAR_(true, 12));              // REMARKS				12
    cols.push_back(_SQLCHAR_(true, 13));              // COLUMN_DEF			13
    cols.push_back(
        _SQLINT2_(false, 14, DEFAULT_TYPE_INT));  // SQL_DATA_TYPE		14
    cols.push_back(_SQLINT2_(true, 15));          // SQL_DATETIME_SUB	15
    cols.push_back(_SQLINT4_(true, 16));          // CHAR_OCTET_LENGTH	16
    cols.push_back(_SQLINT4_(false, 17, -1));     // ORDINAL_POSITION	17
    cols.push_back(_SQLCHAR_(true, 18));          // IS_NULLABLE			18
}

void GetCatalogData(const std::string &query, StatementClass *stmt,
                    StatementClass *sub_stmt, const TableResultSet res_type,
                    std::string &table_type,
                    void (*populate_binds)(bind_vector &),
                    void (*setup_qres_info)(QResultClass *,
                                            EnvironmentClass *)) {
    // Execute query
    ExecuteQuery(SC_get_conn(stmt), reinterpret_cast< HSTMT * >(&sub_stmt),
                 query);

    // Bind Columns
    bind_vector binds;
    (*populate_binds)(binds);
    std::for_each(binds.begin(), binds.end(),
                  [&](const auto &b) { b->BindColumn(sub_stmt); });
    QResultClass *res =
        SetupQResult(binds, stmt, sub_stmt, static_cast< int >(binds.size()));

    // Setup QResultClass
    (*setup_qres_info)(
        res, static_cast< EnvironmentClass * >(CC_get_env(SC_get_conn(stmt))));
    SetTableTuples(res, res_type, binds, table_type, stmt, sub_stmt);

    CleanUp(stmt, sub_stmt, SQL_SUCCESS);
}

RETCODE SQL_API
ESAPI_Tables(HSTMT hstmt, const SQLCHAR *catalog_name_sql,
             const SQLSMALLINT catalog_name_sz, const SQLCHAR *schema_name_sql,
             const SQLSMALLINT schema_name_sz, const SQLCHAR *table_name_sql,
             const SQLSMALLINT table_name_sz, const SQLCHAR *table_type_sql,
             const SQLSMALLINT table_type_sz, const UWORD flag) {
    CSTR func = "ESAPI_Tables";
    StatementClass *stmt = (StatementClass *)hstmt;
    StatementClass *tbl_stmt = NULL;
    RETCODE result = SQL_ERROR;
    if ((result = SC_initialize_and_recycle(stmt)) != SQL_SUCCESS)
        return result;

    try {
        // Convert const SQLCHAR*'s to c++ strings
        std::string catalog_name, schema_name, table_name, table_type;
        bool catalog_valid, schema_valid, table_valid, table_type_valid;
        ConvertToString(catalog_name, catalog_valid, catalog_name_sql,
                        catalog_name_sz);
        ConvertToString(schema_name, schema_valid, schema_name_sql,
                        schema_name_sz);
        ConvertToString(table_name, table_valid, table_name_sql, table_name_sz);
        ConvertToString(table_type, table_type_valid, table_type_sql,
                        table_type_sz);

        //  Special semantics for the CatalogName, SchemaName, and TableType
        //  arguments
        TableResultSet result_type = TableResultSet::All;

        if (catalog_name == SQL_ALL_CATALOGS) {
            if (schema_valid && table_valid && (table_name == "")
                && (schema_name == "")) {
                std::string error_msg("Catalogs not supported.");
                SC_set_error(stmt, STMT_NOT_IMPLEMENTED_ERROR,
                             error_msg.c_str(), func);
                CleanUp(stmt, tbl_stmt);
                return SQL_ERROR;
            }
            // result_type = TableResultSet::Catalog;
        }
        if (schema_name == SQL_ALL_SCHEMAS) {
            if (catalog_valid && table_valid && (table_name == "")
                && (catalog_name == "")) {
                std::string error_msg("Schemas not supported.");
                SC_set_error(stmt, STMT_NOT_IMPLEMENTED_ERROR,
                             error_msg.c_str(), func);
                CleanUp(stmt, tbl_stmt);
                return SQL_ERROR;
            }
            // result_type = TableResultSet::Schema;
        }
        if (table_type_valid && (table_type == SQL_ALL_TABLE_TYPES)) {
            if (catalog_valid && table_valid && schema_valid
                && (table_name == "") && (catalog_name == "")
                && (schema_name == ""))
                result_type = TableResultSet::TableTypes;
        }
        if (table_type_valid && (table_type != SQL_ALL_TABLE_TYPES)) {
            result_type = TableResultSet::TableLookUp;
        }

        // Create query to find out list
        std::string query;
        GenerateTableQuery(query, flag, table_name, result_type, table_valid);

        // TODO #324 (SQL Plugin)- evaluate catalog & schema support
        GetCatalogData(query, stmt, tbl_stmt, result_type, table_type,
                       AssignTableBindTemplates, SetupTableQResInfo);
        return SQL_SUCCESS;
    } catch (std::bad_alloc &e) {
        std::string error_msg = std::string("Bad allocation exception: '")
                                + e.what() + std::string("'.");
        SC_set_error(stmt, STMT_NO_MEMORY_ERROR, error_msg.c_str(), func);
    } catch (std::exception &e) {
        std::string error_msg =
            std::string("Generic exception: '") + e.what() + std::string("'.");
        SC_set_error(stmt, STMT_INTERNAL_ERROR, error_msg.c_str(), func);
    } catch (...) {
        std::string error_msg = std::string("Unknown exception raised.");
        SC_set_error(stmt, STMT_INTERNAL_ERROR, error_msg.c_str(), func);
    }
    CleanUp(stmt, tbl_stmt);
    return SQL_ERROR;
}

RETCODE SQL_API
ESAPI_Columns(HSTMT hstmt, const SQLCHAR *catalog_name_sql,
              const SQLSMALLINT catalog_name_sz, const SQLCHAR *schema_name_sql,
              const SQLSMALLINT schema_name_sz, const SQLCHAR *table_name_sql,
              const SQLSMALLINT table_name_sz, const SQLCHAR *column_name_sql,
              const SQLSMALLINT column_name_sz, const UWORD flag,
              const OID reloid, const Int2 attnum) {
    (void)(reloid);
    (void)(attnum);

    CSTR func = "ESAPI_Columns";

    // Declare outside of try so we can clean them up properly if an exception
    // occurs
    StatementClass *stmt = (StatementClass *)hstmt;
    StatementClass *col_stmt = NULL;
    RETCODE result = SQL_ERROR;
    if ((result = SC_initialize_and_recycle(stmt)) != SQL_SUCCESS)
        return result;

    try {
        // Convert const SQLCHAR *'s to strings
        std::string catalog_name, schema_name, table_name, column_name;
        bool catalog_valid, schema_valid, table_valid, column_valid;
        ConvertToString(catalog_name, catalog_valid, catalog_name_sql,
                        catalog_name_sz);
        ConvertToString(schema_name, schema_valid, schema_name_sql,
                        schema_name_sz);
        ConvertToString(table_name, table_valid, table_name_sql, table_name_sz);
        ConvertToString(column_name, column_valid, column_name_sql,
                        column_name_sz);

        // Generate query
        std::string query;
        GenerateColumnQuery(query, table_name, column_name, table_valid,
                            column_valid, flag);

        // TODO #324 (SQL Plugin)- evaluate catalog & schema support

        // Execute query
        std::string table_type = "";
        GetCatalogData(query, stmt, col_stmt, TableResultSet::All, table_type,
                       AssignColumnBindTemplates, SetupColumnQResInfo);
        return SQL_SUCCESS;
    } catch (std::bad_alloc &e) {
        std::string error_msg = std::string("Bad allocation exception: '")
                                + e.what() + std::string("'.");
        SC_set_error(stmt, STMT_NO_MEMORY_ERROR, error_msg.c_str(), func);
    } catch (std::exception &e) {
        std::string error_msg =
            std::string("Generic exception: '") + e.what() + std::string("'.");
        SC_set_error(stmt, STMT_INTERNAL_ERROR, error_msg.c_str(), func);
    } catch (...) {
        std::string error_msg("Unknown exception raised.");
        SC_set_error(stmt, STMT_INTERNAL_ERROR, error_msg.c_str(), func);
    }
    CleanUp(stmt, col_stmt);
    return SQL_ERROR;
}
void CleanUp_GetTypeInfo(StatementClass *stmt, const RETCODE ret = SQL_ERROR) {
    stmt->status = STMT_FINISHED;
    stmt->currTuple = -1;
    if (SQL_SUCCEEDED(ret))
        SC_set_rowset_start(stmt, -1, FALSE);
    else
        SC_set_Result(stmt, NULL);
    SC_set_current_col(stmt, -1);
}

void SetupTypeQResInfo(QResultClass *res) {
    QR_set_field_info_v(res, GETTYPE_TYPE_NAME, TYPE_NAME, ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, GETTYPE_DATA_TYPE, DATA_TYPE, ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, GETTYPE_COLUMN_SIZE, PRECISION, ES_TYPE_INT4, 4);
    QR_set_field_info_v(res, GETTYPE_LITERAL_PREFIX, LITERAL_PREFIX,
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, GETTYPE_LITERAL_SUFFIX, LITERAL_SUFFIX,
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, GETTYPE_CREATE_PARAMS, CREATE_PARAMS,
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, GETTYPE_NULLABLE, NULLABLE, ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, GETTYPE_CASE_SENSITIVE, CASE_SENSITIVE,
                        ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, GETTYPE_SEARCHABLE, SEARCHABLE, ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, GETTYPE_UNSIGNED_ATTRIBUTE, UNSIGNED_ATTRIBUTE,
                        ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, GETTYPE_FIXED_PREC_SCALE, FIXED_PREC_SCALE,
                        ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, GETTYPE_AUTO_UNIQUE_VALUE, AUTO_INCREMENT,
                        ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, GETTYPE_LOCAL_TYPE_NAME, LOCAL_TYPE_NAME,
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, GETTYPE_MINIMUM_SCALE, MINIMUM_SCALE, ES_TYPE_INT2,
                        2);
    QR_set_field_info_v(res, GETTYPE_MAXIMUM_SCALE, MAXIMUM_SCALE, ES_TYPE_INT2,
                        2);
    QR_set_field_info_v(res, GETTYPE_SQL_DATA_TYPE, SQL_DATA_TYPE, ES_TYPE_INT2,
                        2);
    QR_set_field_info_v(res, GETTYPE_SQL_DATETIME_SUB, SQL_DATETIME_SUB,
                        ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, GETTYPE_NUM_PREC_RADIX, NUM_PREC_RADIX,
                        ES_TYPE_INT4, 4);
    QR_set_field_info_v(res, GETTYPE_INTERVAL_PRECISION, INTERVAL_PRECISION,
                        ES_TYPE_INT2, 2);
}

RETCODE SetTypeResult(ConnectionClass *conn, StatementClass *stmt,
                      QResultClass *res, int esType, int sqlType) {
    TupleField *tuple;

    if (tuple = QR_AddNew(res), NULL == tuple) {
        SC_set_error(stmt, STMT_NO_MEMORY_ERROR, "Couldn't QR_AddNew.",
                     "SetTypeResult");
        CleanUp_GetTypeInfo(stmt, SQL_ERROR);
        return SQL_ERROR;
    }

    set_tuplefield_string(&tuple[GETTYPE_TYPE_NAME],
                          estype_attr_to_name(conn, esType, -1, FALSE));
    set_tuplefield_int2(&tuple[GETTYPE_NULLABLE],
                        estype_nullable(conn, esType));

    set_tuplefield_int2(&tuple[GETTYPE_DATA_TYPE],
                        static_cast< short >(sqlType));
    set_tuplefield_int2(&tuple[GETTYPE_CASE_SENSITIVE],
                        estype_case_sensitive(conn, esType));
    set_tuplefield_int2(&tuple[GETTYPE_SEARCHABLE],
                        estype_searchable(conn, esType));
    set_tuplefield_int2(&tuple[GETTYPE_FIXED_PREC_SCALE],
                        estype_money(conn, esType));

    //  Localized data-source dependent data type name (always NULL)
    set_tuplefield_null(&tuple[GETTYPE_LOCAL_TYPE_NAME]);

    // These values can be NULL
    set_nullfield_int4(
        &tuple[GETTYPE_COLUMN_SIZE],
        estype_attr_column_size(conn, esType, ES_ATP_UNSET, ES_ADT_UNSET,
                                ES_UNKNOWNS_UNSET));
    set_nullfield_string(&tuple[GETTYPE_LITERAL_PREFIX],
                         estype_literal_prefix(conn, esType));
    set_nullfield_string(&tuple[GETTYPE_LITERAL_SUFFIX],
                         estype_literal_suffix(conn, esType));
    set_nullfield_string(&tuple[GETTYPE_CREATE_PARAMS],
                         estype_create_params(conn, esType));
    set_nullfield_int2(&tuple[GETTYPE_UNSIGNED_ATTRIBUTE],
                       estype_unsigned(conn, esType));
    set_nullfield_int2(&tuple[GETTYPE_AUTO_UNIQUE_VALUE],
                       estype_auto_increment(conn, esType));
    set_nullfield_int2(&tuple[GETTYPE_MINIMUM_SCALE],
                       estype_min_decimal_digits(conn, esType));
    set_nullfield_int2(&tuple[GETTYPE_MAXIMUM_SCALE],
                       estype_max_decimal_digits(conn, esType));
    set_tuplefield_int2(&tuple[GETTYPE_SQL_DATA_TYPE],
                        static_cast< short >(sqlType));
    set_nullfield_int2(&tuple[GETTYPE_SQL_DATETIME_SUB],
                       estype_attr_to_datetime_sub(conn, esType, ES_ATP_UNSET));
    set_nullfield_int4(&tuple[GETTYPE_NUM_PREC_RADIX],
                       estype_radix(conn, esType));
    set_nullfield_int4(&tuple[GETTYPE_INTERVAL_PRECISION], 0);

    return SQL_SUCCESS;
}

RETCODE SQL_API ESAPI_GetTypeInfo(HSTMT hstmt, SQLSMALLINT fSqlType) {
    CSTR func = "ESAPI_GetTypeInfo";
    StatementClass *stmt = (StatementClass *)hstmt;
    ConnectionClass *conn;
    conn = SC_get_conn(stmt);
    QResultClass *res = NULL;

    int result_cols;
    RETCODE result = SQL_ERROR;

    if (result = SC_initialize_and_recycle(stmt), SQL_SUCCESS != result)
        return result;

    try {
        if (res = QR_Constructor(), !res) {
            SC_set_error(stmt, STMT_INTERNAL_ERROR, "Error creating result.",
                         func);
            return SQL_ERROR;
        }
        SC_set_Result(stmt, res);

        result_cols = NUM_OF_GETTYPE_FIELDS;
        extend_column_bindings(SC_get_ARDF(stmt),
                               static_cast< SQLSMALLINT >(result_cols));

        stmt->catalog_result = TRUE;
        QR_set_num_fields(res, result_cols);
        SetupTypeQResInfo(res);

        if (fSqlType == SQL_ALL_TYPES) {
            for (std::pair< int, std::vector< int > > sqlType :
                 sql_es_type_map) {
                for (auto const &esType : sqlType.second) {
                    result =
                        SetTypeResult(conn, stmt, res, esType, sqlType.first);
                }
            }
        } else {
            if (sql_es_type_map.count(fSqlType) > 0) {
                for (auto esType : sql_es_type_map.at(fSqlType)) {
                    result = SetTypeResult(conn, stmt, res, esType, fSqlType);
                }
            }
        }
        result = SQL_SUCCESS;

    } catch (std::bad_alloc &e) {
        std::string error_msg = std::string("Bad allocation exception: '")
                                + e.what() + std::string("'.");
        SC_set_error(stmt, STMT_NO_MEMORY_ERROR, error_msg.c_str(), func);
    } catch (std::exception &e) {
        std::string error_msg =
            std::string("Generic exception: '") + e.what() + std::string("'.");
        SC_set_error(stmt, STMT_INTERNAL_ERROR, error_msg.c_str(), func);
    } catch (...) {
        std::string error_msg("Unknown exception raised.");
        SC_set_error(stmt, STMT_INTERNAL_ERROR, error_msg.c_str(), func);
    }

    CleanUp_GetTypeInfo(stmt, result);
    return result;
}