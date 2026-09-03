//
// PrivMX Endpoint Kotlin.
// Copyright © 2025 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//

#include "rawTls.h"
#include <pthread.h>

namespace privmx {
    namespace wrapper {
        namespace jni {
            namespace {
                pthread_key_t g_tlsKey;
                bool g_tlsKeyCreated = false;
            } // namespace

            bool rawTlsCreate(void (*destructor)(void *)) {
                // pthread destructor signature already matches void(*)(void*).
                g_tlsKeyCreated = pthread_key_create(&g_tlsKey, destructor) == 0;
                return g_tlsKeyCreated;
            }

            bool rawTlsExists() { return g_tlsKeyCreated; }

            void *rawTlsGet() { return rawTlsExists() ? pthread_getspecific(g_tlsKey) : nullptr; }

            void rawTlsSet(void *value) { if (rawTlsExists()) pthread_setspecific(g_tlsKey, value); }

        } // jni
    } // wrapper
} // privmx
