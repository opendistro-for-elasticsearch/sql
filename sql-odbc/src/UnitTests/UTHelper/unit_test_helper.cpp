#include "unit_test_helper.h"

#include <fstream>
#include <iostream>

void WriteFileIfSpecified(char** begin, char** end, const std::string& option,
                          std::string& output) {
    char** itr = std::find(begin, end, option);
    if (itr != end && ++itr != end) {
        std::ofstream out_file(*itr);
        if (out_file.good())
            out_file << output;
    }
    return;
}
