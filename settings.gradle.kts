import java.util.Properties

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    val localRepoPath = Properties()
        .apply {
            file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
        }.getProperty("repositoryURL")

    repositories {
        google()
        mavenCentral()

        if (!localRepoPath.isNullOrEmpty()) {
            maven {
                name = "LocalMaven"
                url = uri(localRepoPath)
            }
        }
    }
}

rootProject.name = "privmx-endpoint-kotlin"
include(":privmx-endpoint-extra")
include(":privmx-endpoint-streams")
include(":privmx-endpoint")
include(":jni-wrapper")
include(":examples:snippets")
