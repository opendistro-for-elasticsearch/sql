:: --force: force checks all define combinations (default max is 12)
:: --suppress=objectIndex: seemingly false-positive (TODO: investigate this further) 
:: -iaws-sdk-cpp: avoid checking AWS C++ SDK source files in our repo
cppcheck.exe --force --suppress=objectIndex -iaws-sdk-cpp .\src\ 2> cppcheck-results.log
