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

rootProject.name = "multiplatform-library-template"
include(":privmx-endpoint-extra")
include(":privmx-endpoint")
include(":jni-wrapper")
include(":library-test")