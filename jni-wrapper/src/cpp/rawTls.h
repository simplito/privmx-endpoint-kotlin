#ifndef PRIVMXENDPOINT_RAW_TLS_H
#define PRIVMXENDPOINT_RAW_TLS_H

namespace privmx {
    namespace wrapper {
        namespace jni {

            /**
             * Thin wrapper over thread-local storage with a per-thread exit
             * destructor (pthread_key_create).
             *
             * Kept deliberately free of <pthread.h> so that callers - and anything
             * that includes them - never drag that header in. The platform layer
             * lives in rawTls.cpp and nowhere else, which is also where Windows
             * support (FlsAlloc) would be added.
             *
             * Raw, process-lifetime state on purpose: no C++ objects with destructors,
             * so the key stays usable even if a thread exits after static destructors
             * already ran.
             */

            /**
             * Creates the TLS key. The destructor is invoked with the value stored for
             * a thread, right before that thread exits. Call once (it is not idempotent
             * across successful calls). Returns false if the key could not be created.
             */
            bool rawTlsCreate(void (*destructor)(void *value));

            /** True once rawTlsCreate() has succeeded. */
            bool rawTlsExists();

            /** Value stored for the current thread, or nullptr (also when no key exists). */
            void *rawTlsGet();

            /** Stores value for the current thread. No-op when no key exists. */
            void rawTlsSet(void *value);

        } // jni
    } // wrapper
} // privmx

#endif //PRIVMXENDPOINT_RAW_TLS_H