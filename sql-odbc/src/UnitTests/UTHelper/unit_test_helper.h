#ifndef UNIT_TEST_HELPER
#define UNIT_TEST_HELPER

#if defined(WIN32) || defined (WIN64)
#ifdef _DEBUG
#define VLD_FORCE_ENABLE 1
#include <vld.h>
#endif
#endif

#include <string>
#ifdef USE_SSL
const bool use_ssl = true;
#else
const bool use_ssl = false;
#endif

void WriteFileIfSpecified(char** begin, char** end, const std::string& option,
                          std::string& output);

#endif
