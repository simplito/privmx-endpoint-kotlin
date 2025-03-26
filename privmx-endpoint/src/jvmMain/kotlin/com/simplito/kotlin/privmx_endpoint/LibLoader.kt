package com.simplito.kotlin.privmx_endpoint


internal object LibLoader {
    fun load() {
        System.loadLibrary("privmx-endpoint-kotlin")
    }
}