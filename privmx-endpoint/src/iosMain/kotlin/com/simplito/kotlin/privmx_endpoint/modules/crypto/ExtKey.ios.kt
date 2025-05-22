package com.simplito.kotlin.privmx_endpoint.modules.crypto

import cnames.structs.pson_value
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.utils.PsonValue
import com.simplito.kotlin.privmx_endpoint.utils.asResponse
import com.simplito.kotlin.privmx_endpoint.utils.makeArgs
import com.simplito.kotlin.privmx_endpoint.utils.pson
import com.simplito.kotlin.privmx_endpoint.utils.toExtKey
import com.simplito.kotlin.privmx_endpoint.utils.typedValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import libprivmxendpoint.privmx_endpoint_execExtKey
import libprivmxendpoint.privmx_endpoint_freeExtKey
import libprivmxendpoint.privmx_endpoint_newExtKey
import libprivmxendpoint.pson_free_result
import libprivmxendpoint.pson_free_value

@OptIn(ExperimentalForeignApi::class)
actual class ExtKey() : AutoCloseable {
    private val _nativeExtKey = nativeHeap.allocPointerTo<cnames.structs.ExtKey>()
    private val nativeExtKey
        get() = _nativeExtKey.value?.let { _nativeExtKey }
            ?: throw IllegalStateException("ExtKey has been closed.")

    internal fun getExtKey() = nativeExtKey.value

    init {
        privmx_endpoint_newExtKey(_nativeExtKey.ptr)
    }

    @Throws(exceptionClasses = [IllegalStateException::class, PrivmxException::class, NativeException::class])
    actual fun fromSeed(seed: ByteArray): ExtKey = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(seed.pson)
        try {
            privmx_endpoint_execExtKey(_nativeExtKey.value, 0, args, pson_result.ptr)
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toExtKey()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(exceptionClasses = [IllegalStateException::class, PrivmxException::class, NativeException::class])
    actual fun fromBase58(base58: String): ExtKey = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(base58.pson)
        try {
            privmx_endpoint_execExtKey(_nativeExtKey.value, 1, args, pson_result.ptr)
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toExtKey()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(exceptionClasses = [IllegalStateException::class, PrivmxException::class, NativeException::class])
    actual fun generateRandom(): ExtKey = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execExtKey(_nativeExtKey.value, 2, args, pson_result.ptr)
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toExtKey()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(exceptionClasses = [IllegalStateException::class, PrivmxException::class, NativeException::class])
    actual fun derive(index: Int): ExtKey = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(index.pson)
        try {
            privmx_endpoint_execExtKey(_nativeExtKey.value, 3, args, pson_result.ptr)
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toExtKey()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(exceptionClasses = [IllegalStateException::class, PrivmxException::class, NativeException::class])
    actual fun deriveHardened(index: Int): ExtKey = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs(index.pson)
        try {
            privmx_endpoint_execExtKey(_nativeExtKey.value, 4, args, pson_result.ptr)
            val result = pson_result.value!!.asResponse?.getResultOrThrow() as PsonValue.PsonObject
            result.toExtKey()
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(exceptionClasses = [IllegalStateException::class, PrivmxException::class, NativeException::class])
    actual fun getPrivatePartAsBase58(): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execExtKey(_nativeExtKey.value, 5, args, pson_result.ptr)
            pson_result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(exceptionClasses = [IllegalStateException::class, PrivmxException::class, NativeException::class])
    actual fun getPublicPartAsBase58(): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execExtKey(_nativeExtKey.value, 6, args, pson_result.ptr)
            pson_result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(exceptionClasses = [IllegalStateException::class, PrivmxException::class, NativeException::class])
    actual fun getPrivateKey(): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execExtKey(_nativeExtKey.value, 7, args, pson_result.ptr)
            pson_result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(exceptionClasses = [IllegalStateException::class, PrivmxException::class, NativeException::class])
    actual fun getPublicKey(): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execExtKey(_nativeExtKey.value, 8, args, pson_result.ptr)
            pson_result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(exceptionClasses = [IllegalStateException::class, PrivmxException::class, NativeException::class])
    actual fun getPrivateEncKey(): ByteArray = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execExtKey(_nativeExtKey.value, 9, args, pson_result.ptr)
            pson_result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(exceptionClasses = [IllegalStateException::class, PrivmxException::class, NativeException::class])
    actual fun getPublicKeyAsBase58Address(): String = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execExtKey(_nativeExtKey.value, 10, args, pson_result.ptr)
            pson_result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(exceptionClasses = [IllegalStateException::class, PrivmxException::class, NativeException::class])
    actual fun getChainCode(): ByteArray = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execExtKey(_nativeExtKey.value, 11, args, pson_result.ptr)
            pson_result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue()!!
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(exceptionClasses = [IllegalStateException::class, PrivmxException::class, NativeException::class])
    actual fun verifyCompactSignatureWithHash(
        message: ByteArray,
        signature: ByteArray
    ): Boolean = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execExtKey(_nativeExtKey.value, 12, args, pson_result.ptr)
            pson_result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue<Boolean>() == true
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    @Throws(exceptionClasses = [IllegalStateException::class, PrivmxException::class, NativeException::class])
    actual fun isPrivate(): Boolean = memScoped {
        val pson_result = allocPointerTo<pson_value>()
        val args = makeArgs()
        try {
            privmx_endpoint_execExtKey(_nativeExtKey.value, 13, args, pson_result.ptr)
            pson_result.value!!.asResponse
                ?.getResultOrThrow()
                ?.typedValue<Boolean>() == true
        } finally {
            pson_free_value(args)
            pson_free_result(pson_result.value)
        }
    }

    actual override fun close() {
        if (_nativeExtKey.value == null) return
        privmx_endpoint_freeExtKey(_nativeExtKey.value)
        _nativeExtKey.value = null
    }
}