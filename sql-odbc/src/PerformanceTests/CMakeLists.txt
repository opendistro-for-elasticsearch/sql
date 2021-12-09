project(performance_tests)

set(RESULTS_PTESTS "${CMAKE_CURRENT_SOURCE_DIR}/PTODBCResults")
set(INFO_PTESTS "${CMAKE_CURRENT_SOURCE_DIR}/PTODBCInfo")
set(EXECUTION_PTESTS "${CMAKE_CURRENT_SOURCE_DIR}/PTODBCExecution")

# Projects to build
add_subdirectory(${RESULTS_PTESTS})
add_subdirectory(${INFO_PTESTS})
add_subdirectory(${EXECUTION_PTESTS})
