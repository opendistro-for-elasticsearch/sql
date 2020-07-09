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

#ifndef __DESCRIPTOR_H__
#define __DESCRIPTOR_H__

#include "es_odbc.h"

#ifdef WIN32
#pragma warning(push)
#pragma warning(disable : 4201)  // nonstandard extension used: nameless
                                 // struct/union warning
#endif                           // WIN32

typedef struct InheritanceClass {
    UInt4 allocated;
    UInt4 count;
    OID cur_tableoid;
    esNAME cur_fullTable;
    struct {
        OID tableoid;
        esNAME fullTable;
    } inf[1];
} InheritanceClass;

enum {
    TI_UPDATABLE = 1L,
    TI_HASOIDS_CHECKED = (1L << 1),
    TI_HASOIDS = (1L << 2),
    TI_COLATTRIBUTE = (1L << 3),
    TI_HASSUBCLASS = (1L << 4)
};
typedef struct {
    OID table_oid;
    COL_INFO *col_info; /* cached SQLColumns info for this table */
    esNAME schema_name;
    esNAME table_name;
    esNAME table_alias;
    esNAME bestitem;
    esNAME bestqual;
    UInt4 flags;
    InheritanceClass *ih;
} TABLE_INFO;
#define TI_set_updatable(ti) (ti->flags |= TI_UPDATABLE)
#define TI_is_updatable(ti) (0 != (ti->flags & TI_UPDATABLE))
#define TI_no_updatable(ti) (ti->flags &= (~TI_UPDATABLE))
#define TI_set_hasoids_checked(ti) (ti->flags |= TI_HASOIDS_CHECKED)
#define TI_checked_hasoids(ti) (0 != (ti->flags & TI_HASOIDS))
#define TI_set_hasoids(ti) (ti->flags |= TI_HASOIDS)
#define TI_has_oids(ti) (0 != (ti->flags & TI_HASOIDS))
#define TI_set_has_no_oids(ti) (ti->flags &= (~TI_HASOIDS))
#define TI_set_hassubclass(ti) (ti->flags |= TI_HASSUBCLASS)
#define TI_has_subclass(ti) (0 != (ti->flags & TI_HASSUBCLASS))
#define TI_set_has_no_subclass(ti) (ti->flags &= (~TI_HASSUBCLASS))
void TI_Destructor(TABLE_INFO **, int);
void TI_Destroy_IH(TABLE_INFO *ti);

enum {
    FIELD_INITIALIZED = 0,
    FIELD_PARSING = 1L,
    FIELD_TEMP_SET = (1L << 1),
    FIELD_COL_ATTRIBUTE = (1L << 2),
    FIELD_PARSED_OK = (1L << 3),
    FIELD_PARSED_INCOMPLETE = (1L << 4)
};
typedef struct {
    char flag;
    char updatable;
    Int2 attnum;
    esNAME schema_name;
    TABLE_INFO *ti; /* to resolve explicit table names */
    esNAME column_name;
    esNAME column_alias;
    char nullable;
    char auto_increment;
    char func;
    char columnkey;
    int column_size;    /* precision in 2.x */
    int decimal_digits; /* scale in 2.x */
    int display_size;
    SQLLEN length;
    OID columntype;
    OID basetype; /* may be the basetype when the column type is a domain */
    int typmod;
    char expr;
    char quote;
    char dquote;
    char numeric;
    esNAME before_dot;
} FIELD_INFO;
Int4 FI_precision(const FIELD_INFO *);
void FI_Destructor(FIELD_INFO **, int, BOOL freeFI);
#define FI_is_applicable(fi) \
    (NULL != fi && (fi->flag & (FIELD_PARSED_OK | FIELD_COL_ATTRIBUTE)) != 0)
#define FI_type(fi) (0 == (fi)->basetype ? (fi)->columntype : (fi)->basetype)

typedef struct DescriptorHeader_ {
    ConnectionClass *conn_conn;
    char embedded;
    char type_defined;
    UInt4 desc_type;
    UInt4 error_row;   /* 1-based row */
    UInt4 error_index; /* 1-based index */
    Int4 __error_number;
    char *__error_message;
    ES_ErrorInfo *eserror;
} DescriptorHeader;

/*
 *	ARD and APD are(must be) of the same format
 */
struct ARDFields_ {
    SQLLEN size_of_rowset; /* for ODBC3 fetch operation */
    SQLUINTEGER bind_size; /* size of each structure if using
                            * Row-wise Binding */
    SQLUSMALLINT *row_operation_ptr;
    SQLULEN *row_offset_ptr;
    BindInfoClass *bookmark;
    BindInfoClass *bindings;
    SQLSMALLINT allocated;
    SQLLEN size_of_rowset_odbc2; /* for SQLExtendedFetch */
};

/*
 *	APD must be of the same format as ARD
 */
struct APDFields_ {
    SQLLEN paramset_size;        /* really an SQLINTEGER type */
    SQLUINTEGER param_bind_type; /* size of each structure if using
                                  * Row-wise Parameter Binding */
    SQLUSMALLINT *param_operation_ptr;
    SQLULEN *param_offset_ptr;
    ParameterInfoClass *bookmark; /* dummy item to fit APD to ARD */
    ParameterInfoClass *parameters;
    SQLSMALLINT allocated;
    SQLLEN paramset_size_dummy; /* dummy item to fit APD to ARD */
};

struct IRDFields_ {
    StatementClass *stmt;
    SQLULEN *rowsFetched;
    SQLUSMALLINT *rowStatusArray;
    UInt4 nfields;
    SQLSMALLINT allocated;
    FIELD_INFO **fi;
};

struct IPDFields_ {
    SQLULEN *param_processed_ptr;
    SQLUSMALLINT *param_status_ptr;
    SQLSMALLINT allocated;
    ParameterImplClass *parameters;
};

/***
typedef struct
{
    DescriptorHeader	deschd;
    ARDFields		ardopts;
}	ARDClass;
typedef struct
{
    DescriptorHeader	deschd;
    APDFields		apdopts;
}	APDClass;
typedef struct
{
    DescriptorHeader	deschd;
    IRDFields		irdopts;
}	IRDClass;
typedef struct
{
    DescriptorHeader	deschd;
    IPDFields		ipdopts;
}	IPDClass;
***/
typedef struct {
    DescriptorHeader deschd;
    union {
        ARDFields ardf;
        APDFields apdf;
        IRDFields irdf;
        IPDFields ipdf;
    };
} DescriptorClass;

#define DC_get_conn(a) ((a)->deschd.conn_conn)
#define DC_get_desc_type(a) ((a)->deschd.desc_type)
#define DC_get_embedded(a) ((a)->deschd.embedded)

void InitializeEmbeddedDescriptor(DescriptorClass *, StatementClass *stmt,
                                  UInt4 desc_type);
void DC_Destructor(DescriptorClass *desc);
void InitializeARDFields(ARDFields *self);
void InitializeAPDFields(APDFields *self);
/* void	InitializeIRDFields(IRDFields *self);
void	InitializeIPDFiedls(IPDFields *self); */
BindInfoClass *ARD_AllocBookmark(ARDFields *self);
void ARD_unbind_cols(ARDFields *self, BOOL freeall);
void APD_free_params(APDFields *self, char option);
void IPD_free_params(IPDFields *self, char option);
RETCODE DC_set_stmt(DescriptorClass *desc, StatementClass *stmt);
void DC_set_error(DescriptorClass *desc, int errornumber, const char *errormsg);
void DC_set_errormsg(DescriptorClass *desc, const char *errormsg);
ES_ErrorInfo *DC_get_error(DescriptorClass *self);
int DC_get_errornumber(const DescriptorClass *self);
const char *DC_get_errormsg(const DescriptorClass *self);
void DC_log_error(const char *func, const char *desc,
                  const DescriptorClass *self);

/*	Error numbers about descriptor handle */
enum {
    LOWEST_DESC_ERROR = -2
    /* minus means warning/notice message */
    ,
    DESC_ERROR_IN_ROW = -2,
    DESC_OPTION_VALUE_CHANGED = -1,
    DESC_OK = 0,
    DESC_EXEC_ERROR,
    DESC_STATUS_ERROR,
    DESC_SEQUENCE_ERROR,
    DESC_NO_MEMORY_ERROR,
    DESC_COLNUM_ERROR,
    DESC_NO_STMTSTRING,
    DESC_ERROR_TAKEN_FROM_BACKEND,
    DESC_INTERNAL_ERROR,
    DESC_STILL_EXECUTING,
    DESC_NOT_IMPLEMENTED_ERROR,
    DESC_BAD_PARAMETER_NUMBER_ERROR,
    DESC_OPTION_OUT_OF_RANGE_ERROR,
    DESC_INVALID_COLUMN_NUMBER_ERROR,
    DESC_RESTRICTED_DATA_TYPE_ERROR,
    DESC_INVALID_CURSOR_STATE_ERROR,
    DESC_CREATE_TABLE_ERROR,
    DESC_NO_CURSOR_NAME,
    DESC_INVALID_CURSOR_NAME,
    DESC_INVALID_ARGUMENT_NO,
    DESC_ROW_OUT_OF_RANGE,
    DESC_OPERATION_CANCELLED,
    DESC_INVALID_CURSOR_POSITION,
    DESC_VALUE_OUT_OF_RANGE,
    DESC_OPERATION_INVALID,
    DESC_PROGRAM_TYPE_OUT_OF_RANGE,
    DESC_BAD_ERROR,
    DESC_INVALID_OPTION_IDENTIFIER,
    DESC_RETURN_NULL_WITHOUT_INDICATOR,
    DESC_INVALID_DESCRIPTOR_IDENTIFIER,
    DESC_OPTION_NOT_FOR_THE_DRIVER,
    DESC_FETCH_OUT_OF_RANGE,
    DESC_COUNT_FIELD_INCORRECT
};

#ifdef WIN32
#pragma warning(pop)
#endif  // WIN32

#endif /* __DESCRIPTOR_H__ */
