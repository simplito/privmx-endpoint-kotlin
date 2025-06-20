package com.simplito.kotlin.privmx_endpoint.modules.crypto

import cnames.structs.pson_value
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.utils.PsonValue
import com.simplito.kotlin.privmx_endpoint.utils.asResponse
import com.simplito.kotlin.privmx_endpoint.utils.makeArgs
import com.simplito.kotlin.privmx_endpoint.utils.pson
import com.simplito.kotlin.privmx_endpoint.utils.typedValue
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execExtKey
import libprivmxendpoint.privmx_endpoint_freeExtKey
import libprivmxendpoint.privmx_endpoint_newExtKey
import libprivmxendpoint.pson_free_result
import libprivmxendpoint.pson_free_value

@OptIn(ExperimentalForeignApi::class)
actual class ExtKey private constructor() : AutoCloseable {
    private val _nativeExtKey: CPointerVar<cnames.structs.ExtKey> = nativeHeap.allocPointerTo()
    private val nativeExtKey
        get() = _nativeExtKey.value?.let { _nativeExtKey }
            ?: throw IllegalStateException("ExtKey has been closed.")

    internal fun getExtKey() = nativeExtKey.value

    internal constructor(ptr: PsonValue.PsonLong) : this() {
        _nativeExtKey.value = ptr.getValue().toCPointer()
    }

    actual companion object {
        private val _companionNativeExtKey: CPointerVar<cnames.structs.ExtKey> by lazy {
            nativeHeap.allocPointerTo<cnames.structs.ExtKey>().apply {
                privmx_endpoint_newExtKey(ptr)
            }
        }

        @Throws(PrivmxException::class, NativeException::class)
        actual fun fromSeed(seed: ByteArray): ExtKey = memScoped {
            val pson_result = allocPointerTo<pson_value>()
            val args = makeArgs(seed.pson)
            try {
                privmx_endpoint_execExtKey(_companionNativeExtKey.value, 0, args, pson_result.ptr)
                val result =
                    pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonLong
                ExtKey(result)
            } finally {
                pson_free_value(args)
                pson_free_result(pson_result.value)
            }
        }

        @Throws(PrivmxException::class, NativeException::class)
        actual fun fromBase58(base58: String): ExtKey = memScoped {
            val pson_result = allocPointerTo<pson_value>()
            val args = makeArgs(base58.pson)
            try {
                privmx_endpoint_execExtKey(_companionNativeExtKey.value, 1, args, pson_result.ptr)
                val result =
                    pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonLong
                ExtKey(result)
            } finally {
                pson_free_value(args)
                pson_free_result(pson_result.value)
            }
        }

        @Throws(PrivmxException::class, NativeException::class)
        actual fun generateRandom(): ExtKey = memScoped {
            val pson_result = allocPointerTo<pson_value>()
            val args = makeArgs()
            try {
                privmx_endpoint_execExtKey(_companionNativeExtKey.value, 2, args, pson_result.ptr)
                val result =
                    pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonLong
                ExtKey(result)
            } finally {
                pson_free_value(args)
                pson_free_result(pson_result.value)
            }
        }
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun derive(index: Int): ExtKey = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(index.pson)
        try {
            privmx_endpoint_execExtKey(nativeExtKey.value, 3, args, pson_result.ptr)
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonLong
            ExtKey(result)
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun deriveHardened(index: Int): ExtKey = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(index.pson)
        try {
            privmx_endpoint_execExtKey(nativeExtKey.value, 4, args, pson_result.ptr)
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonLong
            ExtKey(result)
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getPrivatePartAsBase58(): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execExtKey(nativeExtKey.value, 5, args, pson_result.ptr)
            pson_result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getPublicPartAsBase58(): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execExtKey(nativeExtKey.value, 6, args, pson_result.ptr)
            pson_result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getPrivateKey(): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execExtKey(nativeExtKey.value, 7, args, pson_result.ptr)
            pson_result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getPublicKey(): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execExtKey(nativeExtKey.value, 8, args, pson_result.ptr)
            pson_result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getPrivateEncKey(): ByteArray = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execExtKey(nativeExtKey.value, 9, args, pson_result.ptr)
            pson_result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getPublicKeyAsBase58Address(): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execExtKey(nativeExtKey.value, 10, args, pson_result.ptr)
            pson_result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun getChainCode(): ByteArray = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execExtKey(nativeExtKey.value, 11, args, pson_result.ptr)
            pson_result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun verifyCompactSignatureWithHash(
        message: ByteArray,
        signature: ByteArray
    ): Boolean = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(
            message.pson,
            signature.pson
        )
        try {
            privmx_endpoint_execExtKey(nativeExtKey.value, 12, args, pson_result.ptr)
            pson_result.value?.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(PrivmxException::class, NativeException::class, IllegalStateException::class)
    actual fun isPrivate(): Boolean = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execExtKey(nativeExtKey.value, 13, args, pson_result.ptr)
            pson_result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    actual override fun close() {
        privmx_endpoint_freeExtKey(nativeExtKey.value)
        _nativeExtKey.value = null
    }
}