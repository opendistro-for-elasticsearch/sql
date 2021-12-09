#ifndef __LOADLIB_H__
#define __LOADLIB_H__

#include "es_odbc.h"
#ifdef HAVE_LIBLTDL
#include <ltdl.h>
#else
#ifdef HAVE_DLFCN_H
#include <dlfcn.h>
#endif /* HAVE_DLFCN_H */
#endif /* HAVE_LIBLTDL */

#include <stdlib.h>
#ifdef __cplusplus
extern "C" {
#endif

#ifdef _HANDLE_ENLIST_IN_DTC_
RETCODE CALL_EnlistInDtc(ConnectionClass *conn, void *pTra, int method);
RETCODE CALL_DtcOnDisconnect(ConnectionClass *);
RETCODE CALL_IsolateDtcConn(ConnectionClass *, BOOL);
void *CALL_GetTransactionObject(HRESULT *);
void CALL_ReleaseTransactionObject(void *);
#endif /* _HANDLE_ENLIST_IN_DTC_ */
/* void	UnloadDelayLoadedDLLs(BOOL); */
void CleanupDelayLoadedDLLs(void);
#ifdef WIN32
HMODULE MODULE_load_from_elasticodbc_path(const char *module_name);
void AlreadyLoadedElasticsearchodbc(void);
#endif /* WIN32 */

#ifdef __cplusplus
}
#endif
#endif /* __LOADLIB_H__ */
