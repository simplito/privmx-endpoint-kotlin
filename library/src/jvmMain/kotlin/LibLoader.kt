package com.simplito.kotlin.privmx_endpoint

object LibLoader {
    fun loadLibrary(){
        System.loadLibrary("privmx-endpoint-java")
    }
}