package io.github.kotlin.fibonacci

expect class Connection {
    expect fun connect(host: String, solutionId: String, userPrivKey: String)
}