# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("privmx-endpoint-java")

set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -s")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -s")

if( NOT DEFINED CMAKE_DESTINATION_OS )
    set(CMAKE_DESTINATION_OS ${CMAKE_SYSTEM_NAME})
endif ()

if( NOT DEFINED CMAKE_DESTINATION_ARCHITECTURE)
    if(CMAKE_DESTINATION_OS STREQUAL "Android" AND DEFINED CMAKE_ANDROID_ARCH_ABI)
        set(CMAKE_DESTINATION_ARCHITECTURE ${CMAKE_ANDROID_ARCH_ABI})
    else()
        set(CMAKE_DESTINATION_ARCHITECTURE ${CMAKE_SYSTEM_PROCESSOR})
    endif ()
endif()

if("${CMAKE_DESTINATION_OS}" STREQUAL "Darwin")
    set(file_extension "dylib")
    set(CMAKE_MACOSX_RPATH 1)
    set(CMAKE_INSTALL_RPATH "@loader_path")
elseif ("${CMAKE_DESTINATION_OS}" STREQUAL "Windows")
    set(file_extension "dll")
else ()
    set(file_extension "so")
endif ()

message(DEBUG "CMAKE DEST OS ${CMAKE_DESTINATION_OS}")
message(DEBUG "CMAKE DEST PROCESSOR ${CMAKE_DESTINATION_ARCHITECTURE}")

add_library(ssl SHARED IMPORTED GLOBAL)
set_target_properties(ssl PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_DESTINATION_OS}/${CMAKE_DESTINATION_ARCHITECTURE}/lib/libssl.${file_extension})

add_library(crypto SHARED IMPORTED GLOBAL)
set_target_properties(crypto PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_DESTINATION_OS}/${CMAKE_DESTINATION_ARCHITECTURE}/lib/libcrypto.${file_extension})

add_library(gmp SHARED IMPORTED GLOBAL)
set_target_properties(gmp PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_DESTINATION_OS}/${CMAKE_DESTINATION_ARCHITECTURE}/lib/libgmp.${file_extension})

add_library(PocoFoundation SHARED IMPORTED GLOBAL)
set_target_properties(PocoFoundation PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_DESTINATION_OS}/${CMAKE_DESTINATION_ARCHITECTURE}/lib/libPocoFoundation.${file_extension})

add_library(PocoJSON SHARED IMPORTED GLOBAL)
set_target_properties(PocoJSON PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_DESTINATION_OS}/${CMAKE_DESTINATION_ARCHITECTURE}/lib/libPocoJSON.${file_extension})

add_library(PocoNet SHARED IMPORTED GLOBAL)
set_target_properties(PocoNet PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_DESTINATION_OS}/${CMAKE_DESTINATION_ARCHITECTURE}/lib/libPocoNet.${file_extension})

add_library(PocoCrypto SHARED IMPORTED GLOBAL)
set_target_properties(PocoCrypto PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_DESTINATION_OS}/${CMAKE_DESTINATION_ARCHITECTURE}/lib/libPocoCrypto.${file_extension})

add_library(PocoXML SHARED IMPORTED GLOBAL)
set_target_properties(PocoXML PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_DESTINATION_OS}/${CMAKE_DESTINATION_ARCHITECTURE}/lib/libPocoXML.${file_extension})

add_library(PocoNetSSL SHARED IMPORTED GLOBAL)
set_target_properties(PocoNetSSL PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_DESTINATION_OS}/${CMAKE_DESTINATION_ARCHITECTURE}/lib/libPocoNetSSL.${file_extension})

add_library(PocoUtil SHARED IMPORTED GLOBAL)
set_target_properties(PocoUtil PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_DESTINATION_OS}/${CMAKE_DESTINATION_ARCHITECTURE}/lib/libPocoUtil.${file_extension})

add_library(Pson SHARED IMPORTED GLOBAL)
set_target_properties(Pson PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_DESTINATION_OS}/${CMAKE_DESTINATION_ARCHITECTURE}/lib/libPson.${file_extension})

add_library(libprivmx SHARED IMPORTED GLOBAL)
set_target_properties(libprivmx PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_DESTINATION_OS}/${CMAKE_DESTINATION_ARCHITECTURE}/lib/libprivmx.${file_extension})

add_library(privmxendpointcore SHARED IMPORTED GLOBAL)
set_target_properties(privmxendpointcore PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_DESTINATION_OS}/${CMAKE_DESTINATION_ARCHITECTURE}/lib/libprivmxendpointcore.${file_extension})

add_library(privmxendpointcrypto SHARED IMPORTED GLOBAL)
set_target_properties(privmxendpointcrypto PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_DESTINATION_OS}/${CMAKE_DESTINATION_ARCHITECTURE}/lib/libprivmxendpointcrypto.${file_extension})

add_library(privmxendpointstore SHARED IMPORTED GLOBAL)
set_target_properties(privmxendpointstore PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_DESTINATION_OS}/${CMAKE_DESTINATION_ARCHITECTURE}/lib/libprivmxendpointstore.${file_extension})

add_library(privmxendpointthread SHARED IMPORTED GLOBAL)
set_target_properties(privmxendpointthread PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_DESTINATION_OS}/${CMAKE_DESTINATION_ARCHITECTURE}/lib/libprivmxendpointthread.${file_extension})

add_library(privmxendpointinbox SHARED IMPORTED GLOBAL)
set_target_properties(privmxendpointinbox PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_DESTINATION_OS}/${CMAKE_DESTINATION_ARCHITECTURE}/lib/libprivmxendpointinbox.${file_extension})

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/libs/${CMAKE_DESTINATION_OS}/${CMAKE_DESTINATION_ARCHITECTURE}/include ${JAVA_HOME}/include ${JAVA_HOME}/include/darwin)

add_library(${CMAKE_PROJECT_NAME} SHARED
        ${CMAKE_CURRENT_SOURCE_DIR}/parser.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/utils.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/model_native_initializers.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/modules/Connection.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/modules/CryptoApi.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/modules/ThreadApi.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/modules/StoreApi.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/modules/InboxApi.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/modules/EventQueue.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/exceptions.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/modules/BackendRequester.cpp
)

# Android Debugging
#find_library( log-lib log )
#target_link_libraries(${CMAKE_PROJECT_NAME} ${log-lib} ${log})
target_link_libraries(${CMAKE_PROJECT_NAME} ssl crypto)
target_link_libraries(${CMAKE_PROJECT_NAME} gmp)
target_link_libraries(${CMAKE_PROJECT_NAME} PocoFoundation PocoXML PocoJSON PocoNet PocoNetSSL PocoUtil PocoCrypto)
target_link_libraries(${CMAKE_PROJECT_NAME} Pson libprivmx privmxendpointcore privmxendpointcrypto privmxendpointstore privmxendpointthread privmxendpointinbox)
message(DEBUG "Install to ${CMAKE_INSTALL_PREFIX}")
install(TARGETS ${CMAKE_PROJECT_NAME} DESTINATION ${CMAKE_INSTALL_PREFIX})
install(IMPORTED_RUNTIME_ARTIFACTS crypto ssl gmp PocoFoundation PocoXML PocoJSON PocoNet PocoNetSSL PocoUtil PocoCrypto Pson libprivmx privmxendpointcore privmxendpointcrypto privmxendpointstore privmxendpointthread privmxendpointinbox DESTINATION ${CMAKE_INSTALL_PREFIX})