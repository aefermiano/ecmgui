cmake_minimum_required(VERSION 3.10)

project(ecmglue)
add_subdirectory(libecm)
set(libsrc com_afermiano_ecmgui_control_bridge_NativeBridge.h nativebridge.c)

option(JAVA_HOME "JAVA_HOME" "/usr/lib/jvm/java")

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY lib)
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY lib)
set(CMAKE_C_FLAGS "-O3")

include_directories(ecmglue ${JAVA_HOME}/include ${JAVA_HOME}/include/linux ../../libecm/include)

add_library(ecmglue SHARED ${libsrc})
add_dependencies(ecmglue ecm_static)
set_property(TARGET ecmglue PROPERTY POSITION_INDEPENDENT_CODE 1)
target_link_libraries(ecmglue ${CMAKE_SOURCE_DIR}/libecm/lib/libecm_static.a)
