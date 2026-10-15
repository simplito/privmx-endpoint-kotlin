val netDrvLibName = "privmxdrvnet"
val netDrvModuleName = "PrivmxNetDrv"

data class NetDrvApplePlatform(val dirName: String, val sdk: String, val target: String)

val netDrvApplePlatforms = mapOf(
    "IOS" to NetDrvApplePlatform("iOS", "iphoneos", "arm64-apple-ios13.0"),
    "IOSSimulator" to NetDrvApplePlatform("iOSSimulator", "iphonesimulator", "arm64-apple-ios14.0-simulator"),
)

netDrvApplePlatforms.forEach { (taskSuffix, platform) ->
    tasks.register("buildNetDrv$taskSuffix") {
        group = "privmx native"
        description = "Builds lib$netDrvLibName.a (Swift net driver) for ${platform.dirName} arm64."
        inputs.dir("Sources")
        inputs.dir("include")
        inputs.file("../include/privmx/drv/net.h")
        val outputLib = layout.buildDirectory.file("native/install/${platform.dirName}/arm64/lib$netDrvLibName.a")
        outputs.file(outputLib)
        doLast {
            val lib = outputLib.get().asFile.apply { parentFile.mkdirs() }
            val sources = fileTree("Sources") { include("*.swift") }.files.sortedBy { it.name }.map { it.absolutePath }
            exec {
                commandLine(
                    listOf(
                        "xcrun", "--sdk", platform.sdk, "swiftc",
                        "-target", platform.target,
                        "-O", "-whole-module-optimization",
                        "-parse-as-library", "-emit-library", "-static",
                        "-module-name", netDrvModuleName,
                        "-I", file("include").absolutePath,
                        "-o", lib.absolutePath,
                    ) + sources
                )
            }
        }
    }
}

tasks.register("buildNetDrvIOSAll") {
    group = "privmx native"
    description = "Builds the Swift net driver for iOS and the iOS simulator."
    dependsOn(netDrvApplePlatforms.keys.map { "buildNetDrv$it" })
}
