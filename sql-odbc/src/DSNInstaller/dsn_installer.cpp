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

// clang-format off
#include <sql.h>
#include <sqlext.h>
#include <iodbcinst.h>
#include <codecvt>
#include <string>
#include <vector>
#include <iostream>
// clang-format on

// Necessary for installing driver, since the driver description needs to
// maintain null characters.
using namespace std::string_literals;

std::wstring driver_name = L"ODFE SQL ODBC Driver";
std::wstring driver_filename = L"libodfesqlodbc.dylib";
std::wstring dsn_name = L"ODFE SQL ODBC DSN";
std::wstring dsn_ini_filename = L"odfesqlodbc.ini";

std::wstring driver_name_placeholder = L"%DRIVER_NAME%";
std::wstring driver_path_placeholder = L"%DRIVER_PATH%";
std::wstring setup_path_placeholder = L"%SETUP_PATH%";

std::vector< std::pair< std::wstring, std::wstring > > dsn_options = {
    {L"Driver", driver_path_placeholder},
    {L"Host", L"localhost"},
    {L"Port", L"9200"},
    {L"User", L""},
    {L"Password", L""},
    {L"Auth", L"NONE"},
    {L"UseSSL", L"0"},
    {L"ResponseTimeout", L"10"}};

void print_error_message(DWORD error_code, wchar_t *error_message) {
    switch (error_code) {
        case ODBC_ERROR_GENERAL_ERR:
            printf("\t[GENERAL_ERR] %S\n", error_message);
            break;
        case ODBC_ERROR_INVALID_BUFF_LEN:
            printf("\t[INVALID_BUFF_LEN] %S\n", error_message);
            break;
        case ODBC_ERROR_INVALID_HWND:
            printf("\t[INVALID_HWND] %S\n", error_message);
            break;
        case ODBC_ERROR_INVALID_STR:
            printf("\t[INVALID_STR] %S\n", error_message);
            break;
        case ODBC_ERROR_INVALID_REQUEST_TYPE:
            printf("\t[INVALID_REQUEST_TYPE] %S\n", error_message);
            break;
        case ODBC_ERROR_COMPONENT_NOT_FOUND:
            printf("\t[COMPONENT_NOT_FOUND] %S\n", error_message);
            break;
        case ODBC_ERROR_INVALID_NAME:
            printf("\t[INVALID_NAME] %S\n", error_message);
            break;
        case ODBC_ERROR_INVALID_KEYWORD_VALUE:
            printf("\t[INVALID_KEYWORD_VALUE] %S\n", error_message);
            break;
        case ODBC_ERROR_INVALID_PATH:
            printf("\t[INVALID_PATH] %S\n", error_message);
            break;
        default:
            printf("\t%d\n", error_code);
    }
}

void print_installer_error() {
    int ret = 0;

    WORD error_message_max_length = SQL_MAX_MESSAGE_LENGTH;
    DWORD out_error_code;
    wchar_t error_message[SQL_MAX_MESSAGE_LENGTH];
    WORD error_message_num_bytes;

    WORD in_error_rec = 0;  // (1-8)
    do {
        printf("Error %d:\n", ++in_error_rec);
        ret = SQLInstallerErrorW(in_error_rec, &out_error_code, error_message,
                                 error_message_max_length,
                                 &error_message_num_bytes);
        print_error_message(out_error_code, error_message);
    } while (ret != SQL_NO_DATA);
}

void replace_placeholder(std::wstring &source, std::wstring placeholder,
                         std::wstring contents) {
    size_t index = source.find(placeholder);
    if (index != std::string::npos) {
        source.replace(index, placeholder.size(), contents);
    }
}

bool install_driver(std::wstring install_path) {
    std::wstring driver_install_str =
        L"%DRIVER_NAME%\0"
        L"Driver=%DRIVER_PATH%\0"
        L"Setup=%SETUP_PATH%\0\0"s;
    std::wstring driver_path = install_path + driver_filename;

    replace_placeholder(driver_install_str, driver_name_placeholder,
                        driver_name);
    replace_placeholder(driver_install_str, driver_path_placeholder,
                        driver_path);
    replace_placeholder(driver_install_str, setup_path_placeholder,
                        driver_path);

    SQLWCHAR out_path[512];
    WORD out_path_length = 512;
    WORD num_out_path_bytes;
    DWORD out_usage_count = 0;
    bool success =
        SQLInstallDriverExW(driver_install_str.c_str(), install_path.c_str(),
                            out_path, out_path_length, &num_out_path_bytes,
                            ODBC_INSTALL_COMPLETE, &out_usage_count);
    if (!success) {
        print_installer_error();
        return false;
    }

    return success;
}

bool install_dsn() {
    bool success = SQLWriteDSNToIniW(dsn_name.c_str(), driver_name.c_str());
    if (!success) {
        print_installer_error();
        return false;
    }
    return success;
}

bool add_properties_to_dsn(
    std::vector< std::pair< std::wstring, std::wstring > > options,
    std::wstring driver_path) {
    bool success = false;
    for (auto dsn_config_option : options) {
        std::wstring key = dsn_config_option.first;
        std::wstring value = dsn_config_option.second;

        if (value.find(driver_path_placeholder, 0) != std::string::npos) {
            replace_placeholder(value, driver_path_placeholder, driver_path);
        }

        success = SQLWritePrivateProfileStringW(dsn_name.c_str(), key.c_str(),
                                                value.c_str(),
                                                dsn_ini_filename.c_str());
        if (!success) {
            print_installer_error();
            return false;
        }
    }
    return success;
}

bool uninstall_driver() {
    bool remove_dsns = true;
    DWORD out_usage_count = 0;

    bool success =
        SQLRemoveDriverW(driver_name.c_str(), remove_dsns, &out_usage_count);
    if (!success) {
        print_installer_error();
        return false;
    }
    return success;
}

int main(int argc, char *argv[]) {
    // Get install path from args
    if (!argv || argc != 2) {
        printf("Error! Driver path not supplied\n");
        return 1;
    }
    std::wstring user_install_path =
        std::wstring_convert< std::codecvt_utf8_utf16< wchar_t >, wchar_t >{}
            .from_bytes(argv[1]);

    printf("User install path: %S\n", user_install_path.c_str());
    if (!user_install_path.compare(L"uninstall")) {
        bool uninstall_driver_success = uninstall_driver();
        return uninstall_driver_success;
    }

    // Install Driver entry
    printf("Installing Driver entry...\n");
    bool install_driver_success = install_driver(user_install_path);
    if (!install_driver_success) {
        return 1;
    }

    // Add DSN entry
    printf("Adding DSN entry...\n");
    bool install_dsn_success = install_dsn();
    if (!install_dsn_success) {
        return 1;
    }

    // Add DSN properties
    printf("Adding DSN properties...\n");
    bool add_properties_success =
        add_properties_to_dsn(dsn_options, user_install_path + driver_filename);
    if (!add_properties_success) {
        return 1;
    }

    printf("Finished adding DSN!\n");
    return 0;
}
