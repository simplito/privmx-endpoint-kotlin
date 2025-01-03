package io.github.kotlin.fibonacci

expect class Connection {
    fun connect(host: String, solutionId: String, userPrivKey: String)
}