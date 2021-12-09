#ifndef __ES_DRIVER_CONNECT_H__
#define __ES_DRIVER_CONNECT_H__
#include "es_connection.h"

// C Interface
#ifdef __cplusplus
extern "C" {
#endif
RETCODE ESAPI_DriverConnect(HDBC hdbc, HWND hwnd, SQLCHAR *conn_str_in,
                            SQLSMALLINT conn_str_in_len, SQLCHAR *conn_str_out,
                            SQLSMALLINT conn_str_out_len,
                            SQLSMALLINT *pcb_conn_str_out,
                            SQLUSMALLINT driver_completion);
#ifdef __cplusplus
}
#endif

#endif /* __ES_DRIVER_CONNECT_H__ */
