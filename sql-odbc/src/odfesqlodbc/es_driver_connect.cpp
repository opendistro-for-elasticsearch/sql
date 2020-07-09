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

#include "es_driver_connect.h"

#include <stdio.h>
#include <stdlib.h>

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
#include <memory>
#include <string>

#include "dlg_specific.h"
#include "drvconn.h"
#include "es_apifunc.h"

static RETCODE CheckDriverComplete(const SQLUSMALLINT driver_completion,
                                   const HWND hwnd, ConnInfo *ci,
                                   const int reqs) {
    (void)(ci);
    (void)(reqs);
    if (hwnd == NULL)
        return SQL_SUCCESS;
    switch (driver_completion) {
#ifdef WIN32
        case SQL_DRIVER_COMPLETE_REQUIRED:
        case SQL_DRIVER_COMPLETE:
            if (!paramRequired(ci, reqs))
                break;
        case SQL_DRIVER_PROMPT: {
            const RETCODE dialog_result = dconn_DoDialog(hwnd, ci);
            if (dialog_result != SQL_SUCCESS)
                return dialog_result;
            break;
        }
#endif  // WIN32
        default:
            break;
    }
    return SQL_SUCCESS;
}

static RETCODE GetRequirementsAndConnect(const SQLUSMALLINT driver_completion,
                                         const HWND hwnd, ConnInfo *ci,
                                         int &reqs, ConnectionClass *conn,
                                         int &ret_val) {
    const RETCODE res = CheckDriverComplete(driver_completion, hwnd, ci, reqs);
    if (res != SQL_SUCCESS)
        return res;

    // Password is not a required parameter unless authentication asks for it.
    // Let the application ask over and over until a password is entered (the
    // user can always hit Cancel to get out)
    if (paramRequired(ci, reqs)) {
        CC_set_error(conn, CONN_OPENDB_ERROR, "Please supply password",
                     "ESAPI_DriverConnect->GetRequirements");
        return SQL_ERROR;
    }
    ret_val = CC_connect(conn);
    return SQL_SUCCESS;
}

static RETCODE CreateOutputConnectionString(ssize_t &len, ConnectionClass *conn,
                                            const ConnInfo *ci,
                                            const SQLSMALLINT conn_str_out_len,
                                            SQLCHAR *conn_str_out,
                                            const int retval) {
    // Create the output connection string
    SQLSMALLINT len_str_out = conn_str_out_len;
    if (conn->ms_jet && len_str_out > 255)
        len_str_out = 255;
    char conn_str[MAX_CONNECT_STRING];
    makeConnectString(conn_str, ci, len_str_out);

    // Set result and check connection string
    RETCODE result = ((retval == 1) ? SQL_SUCCESS : SQL_SUCCESS_WITH_INFO);
    len = strlen(conn_str);
    if (conn_str_out) {
        //  Return the completed string to the caller. The correct method is to
        //  only construct the connect string if a dialog was put up, otherwise,
        //  it should just copy the connection input string to the output.
        //  However, it seems ok to just always construct an output string.
        //  There are possible bad side effects on working applications (Access)
        //  by implementing the correct behavior
        strncpy((char *)conn_str_out, conn_str, conn_str_out_len);
        if (len >= conn_str_out_len) {
            for (int clen = conn_str_out_len - 1;
                 clen >= 0 && conn_str_out[clen] != ';'; clen--)
                conn_str_out[clen] = '\0';
            result = SQL_SUCCESS_WITH_INFO;
            CC_set_error(conn, CONN_TRUNCATED,
                         "Buffer is too small for output conn str.",
                         "CreateOutputConnectionString");
        }
    }
    return result;
}

static std::string CheckRetVal(const int retval, const HWND hwnd,
                               const SQLUSMALLINT driver_completion,
                               const int reqs, const ConnInfo *ci) {
    (void)(ci);
    (void)(reqs);
    (void)(hwnd);
    if (retval > 0)
        return "";
    // Error attempting to connect
    else if (retval == 0)
        return "Error from CC_Connect";
    // More info is required
    else if (retval < 0) {
        // Not allowed to prompt, but PW is required - Error
        if (driver_completion == SQL_DRIVER_NOPROMPT) {
            return "Need password but Driver_NoPrompt is set";
        } else {
#ifdef WIN32
            if (!(hwnd && paramRequired(ci, reqs)))
                return "Unable to prompt for required parameter";
#else
            return "Driver prompt only supported on Windows";
#endif
        }
    }
    return "";
}

static SQLRETURN SetupConnString(const SQLCHAR *conn_str_in,
                                 const SQLSMALLINT conn_str_in_len,
                                 ConnInfo *ci, ConnectionClass *conn) {
    CSTR func = "SetupConnString";

    // make_string uses malloc, need to overwrite delete operator to use free
    // for unique_ptr
    struct free_delete {
        void operator()(void *x) {
            if (x != NULL) {
                free(x);
                x = NULL;
            }
        }
    };

    // Make connection string and get DSN
    std::unique_ptr< char, free_delete > conn_str(
        make_string(conn_str_in, conn_str_in_len, NULL, 0));
    
    if (!dconn_get_DSN_or_Driver(conn_str.get(), ci)) {
        CC_set_error(conn, CONN_OPENDB_ERROR, "Connection string parse error",
                     func);
        return SQL_ERROR;
    }

    //This will be used to restore the log output dir fetched from connection string
    //Since getDSNinfo overrides all available connection attributes
    std::string conn_string_log_dir(ci->drivers.output_dir);

    // If the ConnInfo in the hdbc is missing anything, this function will fill
    // them in from the registry (assuming of course there is a DSN given -- if
    // not, it does nothing!)
    getDSNinfo(ci, NULL);

    // Parse the connect string and fill in conninfo
    if (!dconn_get_connect_attributes(conn_str.get(), ci)) {
        CC_set_error(conn, CONN_OPENDB_ERROR, "Connection string parse error",
                     func);
        return SQL_ERROR;
    }
    logs_on_off(1, ci->drivers.loglevel, ci->drivers.loglevel);

    //Sets log output dir to path retrived from connection string
    //If connection string doesn't have log path then takes value from DSN
    //If connection string & DSN both doesn't include log path then takes default value
    if (!conn_string_log_dir.empty()) {
        setLogDir(conn_string_log_dir.c_str());
        conn_string_log_dir.clear();
    } else {
        setLogDir(ci->drivers.output_dir);
    }
    InitializeLogging();
    return SQL_SUCCESS;
}

RETCODE ESAPI_DriverConnect(HDBC hdbc, HWND hwnd, SQLCHAR *conn_str_in,
                            SQLSMALLINT conn_str_in_len, SQLCHAR *conn_str_out,
                            SQLSMALLINT conn_str_out_len,
                            SQLSMALLINT *pcb_conn_str_out,
                            SQLUSMALLINT driver_completion) {
    CSTR func = "ESAPI_DriverConnect";
    ConnectionClass *conn = (ConnectionClass *)hdbc;

    if (!conn) {
        CC_log_error(func, "ConnectionClass handle is NULL", NULL);
        return SQL_INVALID_HANDLE;
    }
    ConnInfo *ci = &(conn->connInfo);

    // Setup connection string
    {
        const SQLRETURN return_code =
            SetupConnString(conn_str_in, conn_str_in_len, ci, conn);
        if (return_code != SQL_SUCCESS)
            return return_code;
    }

    // Initialize es_version
    CC_initialize_es_version(conn);

    int reqs = 0;
    int retval = 0;
    do {
        const SQLRETURN return_code = GetRequirementsAndConnect(
            driver_completion, hwnd, ci, reqs, conn, retval);
        if (return_code != SQL_SUCCESS)
            return return_code;

        // Check for errors
        const std::string error_msg =
            CheckRetVal(retval, hwnd, driver_completion, reqs, ci);

        // If we have an error, log it and exit
        if (error_msg != "") {
            CC_log_error(func, error_msg.c_str(), conn);
            return SQL_ERROR;
        }
    } while (retval <= 0);

    ssize_t len = 0;
    const RETCODE result = CreateOutputConnectionString(
        len, conn, ci, conn_str_out_len, conn_str_out, retval);
    if (pcb_conn_str_out)
        *pcb_conn_str_out = static_cast< SQLSMALLINT >(len);
    return result;
}
