pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "privmx-endpoint-kotlin"
include(":privmx-endpoint-extra")
include(":privmx-endpoint")
include(":jni-wrapper")