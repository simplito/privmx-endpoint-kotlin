// swift-tools-version: 6.0
import PackageDescription
import Foundation
let package = Package(
    name: "PrivMXEndpoint",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        .library(
            name: "WebRTC",
            targets: ["WebRTC"]
        ),
        .library(
            name: "PrivMXEndpoint",
            targets: ["PrivMXEndpointObjC"]
        ),
        .library(
            name: "PrivMXEndpointExtra",
            targets: ["PrivMXEndpointExtraObjC"]
        ),
        .library(
            name: "PrivMXEndpointStreams",
            targets: ["PrivMXEndpointStreamsObjC", "WebRTC"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "PrivMXEndpointObjC",
            url: "https://github.com/simplito/privmx-endpoint-kotlin/releases/download/2.8.0-rc1/PrivMXEndpointObjC.xcframework.zip",
            checksum: "ea7f8734803735212f60f22464c45dc03c9f05cec763792942f1737e735f2d05"
        ),

        .binaryTarget(
            name: "PrivMXEndpointExtraObjC",
            url: "https://github.com/simplito/privmx-endpoint-kotlin/releases/download/2.8.0-rc1/PrivMXEndpointExtraObjC.xcframework.zip",
            checksum: "02364b22c42f433dd66050839e65ea35aa5c600cf34c8bf11ce4515e191c2186"
        ),

        .binaryTarget(
            name: "PrivMXEndpointStreamsObjC",
            url: "https://github.com/simplito/privmx-endpoint-kotlin/releases/download/2.8.0-rc1/PrivMXEndpointStreamsObjC.xcframework.zip",
            checksum: "fe2bd2cf701b74a9d360ac8f83d6b40a49e1e022bacc445fa2de97881e784532"
        ),

        .binaryTarget(
            name: "WebRTC",
            url: "https://github.com/simplito/privmx-endpoint-xcframeworks/releases/download/2.8.0/WebRTC-m125.1.0.xcframework.zip",
            checksum: "c231ea0b9b1c8857b947fce3dad1d0242b196c8ef9a8aee6727b08d3fec5421b"
        ),
    ]
)
