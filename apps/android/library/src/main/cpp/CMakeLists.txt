cmake_minimum_required(VERSION 3.22.1)

project(ionclaw_jni LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# the prebuilt native runtime packaged with this module, one binary per abi
add_library(ionclaw SHARED IMPORTED)
set_target_properties(ionclaw PROPERTIES
    IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libionclaw.so)

# single source of truth for the public C ABI shipped with the native library
set(IONCLAW_INCLUDE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../main/lib/include)

add_library(ionclaw_jni SHARED ionclaw_jni.cpp)

target_include_directories(ionclaw_jni PRIVATE ${IONCLAW_INCLUDE_DIR})

target_link_libraries(ionclaw_jni ionclaw log)
