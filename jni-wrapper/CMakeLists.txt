# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("privmx-endpoint-kotlin")
message("module path ${CMAKE_MODULE_PATH}")
message("toolchain path ${CMAKE_TOOLCHAIN_FILE}")

if(APPLE)
    set(CMAKE_INSTALL_RPATH "@loader_path")
elseif(UNIX)
    set(CMAKE_INSTALL_RPATH "$ORIGIN")
endif()

find_package(privmxendpoint REQUIRED)
find_package(Poco REQUIRED)
find_package(privmxdrvnet REQUIRED)
add_library(${CMAKE_PROJECT_NAME} SHARED
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/parser.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/utils.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/jniUtils.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/model_native_initializers.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/modules/Connection.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/modules/CryptoApi.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/modules/ThreadApi.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/modules/StoreApi.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/modules/InboxApi.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/modules/EventQueue.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/exceptions.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/modules/BackendRequester.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/modules/EventApi.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/modules/UserVerifierInterfaceJNI.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/modules/ExtKey.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/modules/Utils.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/modules/KvdbApi.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/modules/StreamApiLow.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/cpp/modules/WebRTCInterfaceJNI.cpp
)

include_directories(${JAVA_HOME}/include)
if(CMAKE_HOST_APPLE)
    include_directories(${JAVA_HOME}/include/darwin)
elseif(CMAKE_HOST_WIN32)
    include_directories(${JAVA_HOME}/include/win32)
else()
    include_directories(${JAVA_HOME}/include/linux)
endif()
target_link_libraries(${CMAKE_PROJECT_NAME} PUBLIC
        privmxendpoint::privmxendpointcore
        privmxendpoint::privmxendpointcrypto
        privmxendpoint::privmxendpointthread
        privmxendpoint::privmxendpointstore
        privmxendpoint::privmxendpointinbox
        privmxendpoint::privmxendpointkvdb
        privmxendpoint::privmxendpointevent
        privmxendpoint::privmxendpointstream
        Poco::Foundation
        privmxdrvnet::privmxdrvnet
)
install(TARGETS ${CMAKE_PROJECT_NAME} DESTINATION ${CMAKE_INSTALL_PREFIX})