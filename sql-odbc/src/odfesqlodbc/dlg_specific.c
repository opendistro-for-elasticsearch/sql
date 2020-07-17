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

#include "dlg_specific.h"

#include <ctype.h>

#include "es_apifunc.h"
#include "misc.h"

#define NULL_IF_NULL(a) ((a) ? ((const char *)(a)) : "(null)")

static void encode(const esNAME, char *out, int outlen);
static esNAME decode(const char *in);
static esNAME decode_or_remove_braces(const char *in);

#define OVR_EXTRA_BITS                                                      \
    (BIT_FORCEABBREVCONNSTR | BIT_FAKE_MSS | BIT_BDE_ENVIRONMENT            \
     | BIT_CVT_NULL_DATE | BIT_ACCESSIBLE_ONLY | BIT_IGNORE_ROUND_TRIP_TIME \
     | BIT_DISABLE_KEEPALIVE)

#define OPENING_BRACKET '{'
#define CLOSING_BRACKET '}'

#ifdef __APPLE__
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wembedded-directive"
#endif  // __APPLE__
void makeConnectString(char *connect_string, const ConnInfo *ci, UWORD len) {
    UNUSED(len);
    char got_dsn = (ci->dsn[0] != '\0');
    char encoded_item[LARGE_REGISTRY_LEN];
    char *connsetStr = NULL;
    char *esoptStr = NULL;
#ifdef _HANDLE_ENLIST_IN_DTC_
    char xaOptStr[16];
#endif
    ssize_t hlen, nlen, olen;

    encode(ci->password, encoded_item, sizeof(encoded_item));
    /* fundamental info */
    nlen = MAX_CONNECT_STRING;
    olen = snprintf(
        connect_string, nlen,
        "%s=%s;" INI_SERVER
        "=%s;"
        "database=elasticsearch;" INI_PORT "=%s;" INI_USERNAME_ABBR
        "=%s;" INI_PASSWORD_ABBR "=%s;" INI_AUTH_MODE "=%s;" INI_REGION
        "=%s;" INI_SSL_USE "=%d;" INI_SSL_HOST_VERIFY "=%d;" INI_LOG_LEVEL
        "=%d;" INI_LOG_OUTPUT "=%s;" INI_TIMEOUT "=%s;" INI_FETCH_SIZE "=%s;",
        got_dsn ? "DSN" : "DRIVER", got_dsn ? ci->dsn : ci->drivername,
        ci->server, ci->port, ci->username, encoded_item, ci->authtype,
        ci->region, (int)ci->use_ssl, (int)ci->verify_server,
        (int)ci->drivers.loglevel, ci->drivers.output_dir,
        ci->response_timeout, ci->fetch_size);
    if (olen < 0 || olen >= nlen) {
        connect_string[0] = '\0';
        return;
    }

    /* extra info */
    hlen = strlen(connect_string);
    nlen = MAX_CONNECT_STRING - hlen;
    if (olen < 0 || olen >= nlen) /* failed */
        connect_string[0] = '\0';

    if (NULL != connsetStr)
        free(connsetStr);
    if (NULL != esoptStr)
        free(esoptStr);
}
#ifdef __APPLE__
#pragma clang diagnostic pop
#endif  // __APPLE__

BOOL get_DSN_or_Driver(ConnInfo *ci, const char *attribute, const char *value) {
    BOOL found = TRUE;

    if (stricmp(attribute, "DSN") == 0)
        STRCPY_FIXED(ci->dsn, value);
    else if (stricmp(attribute, "driver") == 0)
        STRCPY_FIXED(ci->drivername, value);
    else
        found = FALSE;

    return found;
}

BOOL copyConnAttributes(ConnInfo *ci, const char *attribute,
                        const char *value) {
    BOOL found = TRUE, printed = FALSE;
    if (stricmp(attribute, "DSN") == 0)
        STRCPY_FIXED(ci->dsn, value);
    else if (stricmp(attribute, "driver") == 0)
        STRCPY_FIXED(ci->drivername, value);
    else if ((stricmp(attribute, INI_HOST) == 0)
             || (stricmp(attribute, INI_SERVER) == 0))
        STRCPY_FIXED(ci->server, value);
    else if (stricmp(attribute, INI_PORT) == 0)
        STRCPY_FIXED(ci->port, value);
    else if ((stricmp(attribute, INI_USERNAME) == 0)
             || (stricmp(attribute, INI_USERNAME_ABBR) == 0))
        STRCPY_FIXED(ci->username, value);
    else if ((stricmp(attribute, INI_PASSWORD) == 0)
             || (stricmp(attribute, INI_PASSWORD_ABBR) == 0)) {
        ci->password = decode_or_remove_braces(value);
#ifndef FORCE_PASSWORDE_DISPLAY
        MYLOG(ES_DEBUG, "key='%s' value='xxxxxxxx'\n", attribute);
        printed = TRUE;
#endif
    } else if (stricmp(attribute, INI_AUTH_MODE) == 0)
        STRCPY_FIXED(ci->authtype, value);
    else if (stricmp(attribute, INI_REGION) == 0)
        STRCPY_FIXED(ci->region, value);
    else if (stricmp(attribute, INI_SSL_USE) == 0)
        ci->use_ssl = (char)atoi(value);
    else if (stricmp(attribute, INI_SSL_HOST_VERIFY) == 0)
        ci->verify_server = (char)atoi(value);
    else if (stricmp(attribute, INI_LOG_LEVEL) == 0)
        ci->drivers.loglevel = (char)atoi(value);
    else if (stricmp(attribute, INI_LOG_OUTPUT) == 0)
        STRCPY_FIXED(ci->drivers.output_dir, value);
    else if (stricmp(attribute, INI_TIMEOUT) == 0)
        STRCPY_FIXED(ci->response_timeout, value);
    else if (stricmp(attribute, INI_FETCH_SIZE) == 0)
        STRCPY_FIXED(ci->fetch_size, value);
    else
        found = FALSE;

    if (!printed)
        MYLOG(ES_DEBUG, "key='%s' value='%s'%s\n", attribute, value,
              found ? NULL_STRING : " not found");

    return found;
}

static void getCiDefaults(ConnInfo *ci) {
    strncpy(ci->desc, DEFAULT_DESC, MEDIUM_REGISTRY_LEN);
    strncpy(ci->drivername, DEFAULT_DRIVERNAME, MEDIUM_REGISTRY_LEN);
    strncpy(ci->server, DEFAULT_HOST, MEDIUM_REGISTRY_LEN);
    strncpy(ci->port, DEFAULT_PORT, SMALL_REGISTRY_LEN);
    strncpy(ci->response_timeout, DEFAULT_RESPONSE_TIMEOUT_STR,
            SMALL_REGISTRY_LEN);
    strncpy(ci->fetch_size, DEFAULT_FETCH_SIZE_STR,
            SMALL_REGISTRY_LEN);
    strncpy(ci->authtype, DEFAULT_AUTHTYPE, MEDIUM_REGISTRY_LEN);
    if (ci->password.name != NULL)
        free(ci->password.name);
    ci->password.name = NULL;
    strncpy(ci->username, DEFAULT_USERNAME, MEDIUM_REGISTRY_LEN);
    strncpy(ci->region, DEFAULT_REGION, MEDIUM_REGISTRY_LEN);
    ci->use_ssl = DEFAULT_USE_SSL;
    ci->verify_server = DEFAULT_VERIFY_SERVER;
    strcpy(ci->drivers.output_dir, "C:\\");
}

#ifdef __APPLE__
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wembedded-directive"
#endif  // __APPLE__
int getDriverNameFromDSN(const char *dsn, char *driver_name, int namelen) {
#ifdef WIN32
    return SQLGetPrivateProfileString(ODBC_DATASOURCES, dsn, NULL_STRING,
                                      driver_name, namelen, ODBC_INI);
#else  /* WIN32 */
    int cnt;

    cnt = SQLGetPrivateProfileString(dsn, "Driver", NULL_STRING, driver_name,
                                     namelen, ODBC_INI);
    if (!driver_name[0])
        return cnt;
    if (strchr(driver_name, '/') || /* path to the driver */
        strchr(driver_name, '.')) {
        driver_name[0] = '\0';
        return 0;
    }
    return cnt;
#endif /* WIN32 */
#ifdef __APPLE__
#pragma clang diagnostic pop
#endif  // __APPLE__
}

void getDriversDefaults(const char *drivername, GLOBAL_VALUES *comval) {
    if (NULL != drivername)
        STR_TO_NAME(comval->drivername, drivername);
}

void getDSNinfo(ConnInfo *ci, const char *configDrvrname) {
    char *DSN = ci->dsn;
    char temp[LARGE_REGISTRY_LEN];
    const char *drivername;
    getCiDefaults(ci);
    drivername = ci->drivername;
    if (DSN[0] == '\0') {
        if (drivername[0] == '\0') /* adding new DSN via configDSN */
        {
            if (configDrvrname)
                drivername = configDrvrname;
            strncpy_null(DSN, INI_DSN, sizeof(ci->dsn));
        }
        /* else dns-less connections */
    }

    /* brute-force chop off trailing blanks... */
    while (*(DSN + strlen(DSN) - 1) == ' ')
        *(DSN + strlen(DSN) - 1) = '\0';

    if (!drivername[0] && DSN[0])
        getDriverNameFromDSN(DSN, (char *)drivername, sizeof(ci->drivername));
    MYLOG(ES_DEBUG, "drivername=%s\n", drivername);
    if (!drivername[0])
        drivername = INVALID_DRIVER;
    getDriversDefaults(drivername, &(ci->drivers));

    if (DSN[0] == '\0')
        return;

    /* Proceed with getting info for the given DSN. */
    if (SQLGetPrivateProfileString(DSN, INI_SERVER, NULL_STRING, temp,
                                   sizeof(temp), ODBC_INI)
        > 0)
        STRCPY_FIXED(ci->server, temp);
    if (SQLGetPrivateProfileString(DSN, INI_HOST, NULL_STRING, temp,
                                   sizeof(temp), ODBC_INI)
        > 0)
        STRCPY_FIXED(ci->server, temp);
    if (SQLGetPrivateProfileString(DSN, INI_PORT, NULL_STRING, temp,
                                   sizeof(temp), ODBC_INI)
        > 0)
        STRCPY_FIXED(ci->port, temp);
    if (SQLGetPrivateProfileString(DSN, INI_USERNAME, NULL_STRING, temp,
                                   sizeof(temp), ODBC_INI)
        > 0)
        STRCPY_FIXED(ci->username, temp);
    if (SQLGetPrivateProfileString(DSN, INI_USERNAME_ABBR, NULL_STRING, temp,
                                   sizeof(temp), ODBC_INI)
        > 0)
        STRCPY_FIXED(ci->username, temp);
    if (SQLGetPrivateProfileString(DSN, INI_PASSWORD, NULL_STRING, temp,
                                   sizeof(temp), ODBC_INI)
        > 0)
        ci->password = decode(temp);
    if (SQLGetPrivateProfileString(DSN, INI_PASSWORD_ABBR, NULL_STRING, temp,
                                   sizeof(temp), ODBC_INI)
        > 0)
        ci->password = decode(temp);
    if (SQLGetPrivateProfileString(DSN, INI_AUTH_MODE, NULL_STRING, temp,
                                   sizeof(temp), ODBC_INI)
        > 0)
        STRCPY_FIXED(ci->authtype, temp);
    if (SQLGetPrivateProfileString(DSN, INI_REGION, NULL_STRING, temp,
                                   sizeof(temp), ODBC_INI)
        > 0)
        STRCPY_FIXED(ci->region, temp);
    if (SQLGetPrivateProfileString(DSN, INI_SSL_USE, NULL_STRING, temp,
                                   sizeof(temp), ODBC_INI)
        > 0)
        ci->use_ssl = (char)atoi(temp);
    if (SQLGetPrivateProfileString(DSN, INI_SSL_HOST_VERIFY, NULL_STRING, temp,
                                   sizeof(temp), ODBC_INI)
        > 0)
        ci->verify_server = (char)atoi(temp);
    if (SQLGetPrivateProfileString(DSN, INI_LOG_LEVEL, NULL_STRING, temp,
                                   sizeof(temp), ODBC_INI)
        > 0)
        ci->drivers.loglevel = (char)atoi(temp);
    if (SQLGetPrivateProfileString(DSN, INI_LOG_OUTPUT, NULL_STRING, temp,
                                   sizeof(temp), ODBC_INI)
        > 0)
        STRCPY_FIXED(ci->drivers.output_dir, temp);
    if (SQLGetPrivateProfileString(DSN, INI_TIMEOUT, NULL_STRING, temp,
                                   sizeof(temp), ODBC_INI)
        > 0)
        STRCPY_FIXED(ci->response_timeout, temp);
    if (SQLGetPrivateProfileString(DSN, INI_FETCH_SIZE, NULL_STRING, temp,
                                   sizeof(temp), ODBC_INI)
        > 0)
        STRCPY_FIXED(ci->fetch_size, temp);
    STR_TO_NAME(ci->drivers.drivername, drivername);
}
/*
 *	This function writes any global parameters (that can be manipulated)
 *	to the ODBCINST.INI portion of the registry
 */
int write_Ci_Drivers(const char *fileName, const char *sectionName,
                     const GLOBAL_VALUES *comval) {
    UNUSED(comval, fileName, sectionName);

    // We don't need anything here
    return 0;
}

int writeDriversDefaults(const char *drivername, const GLOBAL_VALUES *comval) {
    return write_Ci_Drivers(ODBCINST_INI, drivername, comval);
}

/*	This is for datasource based options only */
void writeDSNinfo(const ConnInfo *ci) {
    const char *DSN = ci->dsn;
    char encoded_item[MEDIUM_REGISTRY_LEN], temp[SMALL_REGISTRY_LEN];

    SQLWritePrivateProfileString(DSN, INI_HOST, ci->server, ODBC_INI);
    SQLWritePrivateProfileString(DSN, INI_PORT, ci->port, ODBC_INI);
    SQLWritePrivateProfileString(DSN, INI_USERNAME, ci->username, ODBC_INI);
    encode(ci->password, encoded_item, sizeof(encoded_item));
    SQLWritePrivateProfileString(DSN, INI_PASSWORD, encoded_item, ODBC_INI);
    SQLWritePrivateProfileString(DSN, INI_AUTH_MODE, ci->authtype, ODBC_INI);
    SQLWritePrivateProfileString(DSN, INI_REGION, ci->region, ODBC_INI);
    ITOA_FIXED(temp, ci->use_ssl);
    SQLWritePrivateProfileString(DSN, INI_SSL_USE, temp, ODBC_INI);
    ITOA_FIXED(temp, ci->verify_server);
    SQLWritePrivateProfileString(DSN, INI_SSL_HOST_VERIFY, temp, ODBC_INI);
    ITOA_FIXED(temp, ci->drivers.loglevel);
    SQLWritePrivateProfileString(DSN, INI_LOG_LEVEL, temp, ODBC_INI);
    SQLWritePrivateProfileString(DSN, INI_LOG_OUTPUT, ci->drivers.output_dir,
                                 ODBC_INI);
    SQLWritePrivateProfileString(DSN, INI_TIMEOUT, ci->response_timeout,
                                 ODBC_INI);
    SQLWritePrivateProfileString(DSN, INI_FETCH_SIZE, ci->fetch_size,
                                 ODBC_INI);

}

static void encode(const esNAME in, char *out, int outlen) {
    size_t i, ilen = 0;
    int o = 0;
    char inc, *ins;

    if (NAME_IS_NULL(in)) {
        out[0] = '\0';
        return;
    }
    ins = GET_NAME(in);
    ilen = strlen(ins);
    for (i = 0; i < ilen && o < outlen - 1; i++) {
        inc = ins[i];
        if (inc == '+') {
            if (o + 2 >= outlen)
                break;
            snprintf(&out[o], outlen - o, "%%2B");
            o += 3;
        } else if (isspace((unsigned char)inc))
            out[o++] = '+';
        else if (!isalnum((unsigned char)inc)) {
            if (o + 2 >= outlen)
                break;
            snprintf(&out[o], outlen - o, "%%%02x", inc);
            o += 3;
        } else
            out[o++] = inc;
    }
    out[o++] = '\0';
}

static unsigned int conv_from_hex(const char *s) {
    int i, y = 0, val;

    for (i = 1; i <= 2; i++) {
        if (s[i] >= 'a' && s[i] <= 'f')
            val = s[i] - 'a' + 10;
        else if (s[i] >= 'A' && s[i] <= 'F')
            val = s[i] - 'A' + 10;
        else
            val = s[i] - '0';

        y += val << (4 * (2 - i));
    }

    return y;
}

static esNAME decode(const char *in) {
    size_t i, ilen = strlen(in), o = 0;
    char inc, *outs;
    esNAME out;

    INIT_NAME(out);
    if (0 == ilen) {
        return out;
    }
    outs = (char *)malloc(ilen + 1);
    if (!outs)
        return out;
    for (i = 0; i < ilen; i++) {
        inc = in[i];
        if (inc == '+')
            outs[o++] = ' ';
        else if (inc == '%') {
            snprintf(&outs[o], ilen + 1 - o, "%c", conv_from_hex(&in[i]));
            o++;
            i += 2;
        } else
            outs[o++] = inc;
    }
    outs[o++] = '\0';
    STR_TO_NAME(out, outs);
    free(outs);
    return out;
}

/*
 *	Remove braces if the input value is enclosed by braces({}).
 *	Othewise decode the input value.
 */
static esNAME decode_or_remove_braces(const char *in) {
    if (OPENING_BRACKET == in[0]) {
        size_t inlen = strlen(in);
        if (CLOSING_BRACKET == in[inlen - 1]) /* enclosed with braces */
        {
            int i;
            const char *istr, *eptr;
            char *ostr;
            esNAME out;

            INIT_NAME(out);
            if (NULL == (ostr = (char *)malloc(inlen)))
                return out;
            eptr = in + inlen - 1;
            for (istr = in + 1, i = 0; *istr && istr < eptr; i++) {
                if (CLOSING_BRACKET == istr[0] && CLOSING_BRACKET == istr[1])
                    istr++;
                ostr[i] = *(istr++);
            }
            ostr[i] = '\0';
            SET_NAME_DIRECTLY(out, ostr);
            return out;
        }
    }
    return decode(in);
}

void CC_conninfo_release(ConnInfo *conninfo) {
    NULL_THE_NAME(conninfo->password);
    finalize_globals(&conninfo->drivers);
}

void CC_conninfo_init(ConnInfo *conninfo, UInt4 option) {
    MYLOG(ES_TRACE, "entering opt=%d\n", option);

    if (0 != (CLEANUP_FOR_REUSE & option))
        CC_conninfo_release(conninfo);
    memset(conninfo, 0, sizeof(ConnInfo));

    strncpy(conninfo->dsn, DEFAULT_DSN, MEDIUM_REGISTRY_LEN);
    strncpy(conninfo->desc, DEFAULT_DESC, MEDIUM_REGISTRY_LEN);
    strncpy(conninfo->drivername, DEFAULT_DRIVERNAME, MEDIUM_REGISTRY_LEN);
    strncpy(conninfo->server, DEFAULT_HOST, MEDIUM_REGISTRY_LEN);
    strncpy(conninfo->port, DEFAULT_PORT, SMALL_REGISTRY_LEN);
    strncpy(conninfo->response_timeout, DEFAULT_RESPONSE_TIMEOUT_STR,
            SMALL_REGISTRY_LEN);
    strncpy(conninfo->fetch_size, DEFAULT_FETCH_SIZE_STR,
            SMALL_REGISTRY_LEN);
    strncpy(conninfo->authtype, DEFAULT_AUTHTYPE, MEDIUM_REGISTRY_LEN);
    if (conninfo->password.name != NULL)
        free(conninfo->password.name);
    conninfo->password.name = NULL;
    strncpy(conninfo->username, DEFAULT_USERNAME, MEDIUM_REGISTRY_LEN);
    strncpy(conninfo->region, DEFAULT_REGION, MEDIUM_REGISTRY_LEN);
    conninfo->use_ssl = DEFAULT_USE_SSL;
    conninfo->verify_server = DEFAULT_VERIFY_SERVER;

    if (0 != (INIT_GLOBALS & option))
        init_globals(&(conninfo->drivers));
}

void init_globals(GLOBAL_VALUES *glbv) {
    memset(glbv, 0, sizeof(*glbv));
    glbv->loglevel = DEFAULT_LOGLEVEL;
    glbv->output_dir[0] = '\0';
}

#define CORR_STRCPY(item) strncpy_null(to->item, from->item, sizeof(to->item))
#define CORR_VALCPY(item) (to->item = from->item)

void copy_globals(GLOBAL_VALUES *to, const GLOBAL_VALUES *from) {
    memset(to, 0, sizeof(*to));
    NAME_TO_NAME(to->drivername, from->drivername);
    CORR_VALCPY(loglevel);
}

void finalize_globals(GLOBAL_VALUES *glbv) {
    NULL_THE_NAME(glbv->drivername);
}

#undef CORR_STRCPY
#undef CORR_VALCPY
#define CORR_STRCPY(item) strncpy_null(ci->item, sci->item, sizeof(ci->item))
#define CORR_VALCPY(item) (ci->item = sci->item)

void CC_copy_conninfo(ConnInfo *ci, const ConnInfo *sci) {
    memset(ci, 0, sizeof(ConnInfo));
    CORR_STRCPY(dsn);
    CORR_STRCPY(desc);
    CORR_STRCPY(drivername);
    CORR_STRCPY(server);
    CORR_STRCPY(username);
    CORR_STRCPY(authtype);
    CORR_STRCPY(region);
    NAME_TO_NAME(ci->password, sci->password);
    CORR_VALCPY(use_ssl);
    CORR_VALCPY(verify_server);
    CORR_STRCPY(port);
    CORR_STRCPY(response_timeout);
    CORR_STRCPY(fetch_size);
    copy_globals(&(ci->drivers), &(sci->drivers));
}
#undef CORR_STRCPY
#undef CORR_VALCPY
