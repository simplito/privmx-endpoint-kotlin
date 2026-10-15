cmake_minimum_required(VERSION 3.18)
project(net_drv_cpp CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
if(NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE MinSizeRel)
endif()

add_library(net_drv_cpp SHARED src/net.cpp)

target_include_directories(net_drv_cpp PRIVATE
    ${CMAKE_CURRENT_BINARY_DIR}../include)

if(NOT ANDROID)
    if(DEFINED ENV{JAVA_HOME})
        set(JDK_INCLUDE $ENV{JAVA_HOME}/include)
        if(APPLE)
            set(JDK_MD_INCLUDE ${JDK_INCLUDE}/darwin)
        elseif(WIN32)
            set(JDK_MD_INCLUDE ${JDK_INCLUDE}/win32)
        else()
            set(JDK_MD_INCLUDE ${JDK_INCLUDE}/linux)
        endif()
        target_include_directories(net_drv_cpp PRIVATE ${JDK_INCLUDE} ${JDK_MD_INCLUDE})
    else()
        find_package(JNI REQUIRED)
        target_include_directories(net_drv_cpp PRIVATE ${JNI_INCLUDE_DIRS})
    endif()
endif()

target_compile_options(net_drv_cpp PRIVATE
    -fno-exceptions -fno-rtti -fvisibility=hidden -fvisibility-inlines-hidden
    -ffunction-sections -fdata-sections -Wall -Wextra)

if(APPLE)
    target_link_options(net_drv_cpp PRIVATE -nostdlib++ -Wl,-dead_strip)
else()
    target_link_options(net_drv_cpp PRIVATE -nostdlib++ -Wl,--gc-sections -Wl,--as-needed)
endif()
