package io.github.kotlin.fibonacci

actual class Connection {
    actual external fun connect(host: String, solutionId: String, userPrivKey: String)
}