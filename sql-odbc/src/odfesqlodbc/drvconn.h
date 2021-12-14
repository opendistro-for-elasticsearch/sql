#ifndef _DRVCONN_H_
#define _DRVCONN_H_

#include <stdio.h>
#include <stdlib.h>

#include "es_connection.h"
#include "es_odbc.h"
#include "misc.h"

#ifndef WIN32
#include <sys/socket.h>
#include <sys/types.h>
#else
#include <winsock2.h>
#endif

#include <string.h>

#ifdef WIN32
#include <windowsx.h>

#include "resource.h"
#endif
#include "dlg_specific.h"
#include "es_apifunc.h"

#define PASSWORD_IS_REQUIRED 1

#ifdef __cplusplus
extern "C" {
#endif
char *hide_password(const char *str);
BOOL dconn_get_connect_attributes(const char *connect_string, ConnInfo *ci);
BOOL dconn_get_DSN_or_Driver(const char *connect_string, ConnInfo *ci);
int paramRequired(const ConnInfo *ci, int reqs);
#ifdef WIN32
RETCODE dconn_DoDialog(HWND hwnd, ConnInfo *ci);
#endif
#ifdef __cplusplus
}
#endif

#endif
