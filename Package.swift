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
            checksum: "dafa97541862b68bc301a604d51197e3da552c82e11f9f4c1450168339241a7f"
        ),

        .binaryTarget(
            name: "WebRTC",
            url: "https://github.com/simplito/privmx-endpoint-xcframeworks/releases/download/2.8.0/WebRTC-m125.1.0.xcframework.zip",
            checksum: "3ff9b597233c00bae65ae7afb5bac3ef9ae31a348fd351411f98c12cacd37445"
        ),
    ]
)
