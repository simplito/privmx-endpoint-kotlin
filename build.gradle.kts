import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClients
import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.AbstractDokkaTask
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.dokka.versioning.VersioningConfiguration
import org.jetbrains.dokka.versioning.VersioningPlugin
import java.net.URLEncoder
import java.util.Properties
import kotlin.apply
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    id("org.jetbrains.dokka") version "2.0.0"
}
buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:2.0.0")
        classpath("org.jetbrains.dokka:versioning-plugin:2.0.0")
        classpath("org.apache.httpcomponents:httpclient:4.5.14")
        classpath("org.apache.httpcomponents:httpmime:4.5.14")
        classpath(libs.kotlinx.serialization.json)
    }
}

val localProperties = Properties().apply {
    load(file(rootDir.absolutePath + "/local.properties").inputStream())
}

version = libs.versions.publishPrivmxEndpoint.get()

listOf(
    project(":privmx-endpoint"),
    project(":privmx-endpoint-extra"),
).forEach { currentProject: Project ->
    currentProject.apply(plugin = "org.jetbrains.dokka")
    currentProject.tasks.register<DokkaTaskPartial>("privmxEndpointHtmlPartial") {
        (currentProject.dokkaTaskConfiguration)()
    }
    currentProject.tasks.withType<DokkaTask>().configureEach {
        dokkaSourceSets {
            configureEach {
                documentedVisibilities.set(
                    setOf(
                        Visibility.PUBLIC,
                        Visibility.PROTECTED
                    )
                )
            }
        }
    }

    val emptyJavadocJar = currentProject.tasks.register<Jar>("emptyJavadocJar") {
        archiveClassifier = "javadoc"
    }

    currentProject.afterEvaluate {
        val publishingExtension = currentProject.extensions.getByType<PublishingExtension>()
        publishingExtension
            .publications
            .withType<MavenPublication>()
            .configureEach {
                if (name == "jvm") {
                    artifact(emptyJavadocJar)
                }
                configurePom()
                currentProject.createZipPublicationTask(publishingExtension, this)
                currentProject.createUploadPublicationTask(publishingExtension, this)
            }

        currentProject.tasks.register("deployToMavenCentral") {
            group = "PrivMX Deployment"
            publishingExtension.publications.withType<MavenPublication>().onEach { publication ->
                dependsOn(tasks.named("upload${publication.name}Publication"))
            }
        }
    }
}

@Suppress("DEPRECATION")
tasks.register<DokkaMultiModuleTask>("privmxEndpointHtmlMultiModule") {
    addSubprojectChildTasks("privmxEndpointHtmlPartial")
    dokkaTaskConfiguration()
    // This can be any persistent folder where
    // you store documentation by version
    val docVersionsDir = outputDirectory.asFile.get().resolve("version")

    // The version for which you are currently generating docs
    val currentVersion = project.docsVersion

    // Set the output to a folder with all other versions
    // as you'll need the current version for future builds
    val currentDocsDir = docVersionsDir.resolve(currentVersion)

    val latestDocsDir = docVersionsDir.resolve("latest")

    outputDirectory.set(currentDocsDir)

    pluginConfiguration<VersioningPlugin, VersioningConfiguration> {
        olderVersionsDir = docVersionsDir
        version = currentVersion
    }
    val latestVersion: String? = latestDocsDir.resolve("version.json").let {
        if (!it.exists()) {
            return@let null
        }
        return@let it.reader().use { reader ->
            Json.parseToJsonElement(reader.readText()).jsonObject["version"]?.jsonPrimitive?.content
        }
    }
    outputs.upToDateWhen { latestDocsDir.exists() && latestVersion == currentVersion }
    doLast {
        // Copy custom fonts
        copy {
            from(rootProject.layout.projectDirectory.file("docs/fonts"))
            into(outputDirectory.file("styles/fonts"))
        }


        // This folder contains the latest documentation with all
        // previous versions included, so it's ready to be published.
        // Make sure it's copied and not moved - you'll still need this
        // version for future builds
        println(currentDocsDir.copyRecursively(latestDocsDir, overwrite = true))


        // Only once current documentation has been safely moved,
        // remove previous versions bundled in it. They will not
        // be needed in future builds, it's just overhead.
        currentDocsDir.resolve("older").deleteRecursively()
    }
    plugins.dependencies.addAll(
        listOf(
            dependencies.create("org.jetbrains.dokka:all-modules-page-plugin:2.0.0"),
            dependencies.create("org.jetbrains.dokka:versioning-plugin:2.0.0"),
        )
    )
}

fun MavenPublication.configurePom() {
    pom {
        url = "https://privmx.dev"
        licenses {
            license {
                name = "MIT License"
                url = "https://github.com/simplito/privmx-endpoint-kotlin/blob/main/LICENSE"
            }
        }
        scm {
            url = "https://github.com/simplito/privmx-endpoint-kotlin"
        }
        developers {
            developer {
                id = "Simplito"
                name = "Simplito Team"
                organization = "Simplito sp. z o.o."
                organizationUrl = "https://www.simplito.com"
            }
        }
    }
}

fun Project.createZipPublicationTask(
    publishing: PublishingExtension,
    publication: MavenPublication
): Zip {
    val repoFile =
        file((publishing.repositories.named("localRepo").get() as MavenArtifactRepository).url)
    val packageDirName =
        "${publication.groupId.replace(".", "/")}/${publication.artifactId}/${publication.version}"
    return tasks.create<Zip>("zip${publication.name}Publication") {
        group = "PrivMX Deployment"
        val zipDirectoryFile =
            file(repoFile.absolutePath + "/zipped/${publication.artifactId}/${publication.version}")
        archiveFileName = "${publication.artifactId}-${publication.version}.zip"
        destinationDirectory = zipDirectoryFile
        from(repoFile)
        include("$packageDirName/*")
        doFirst {
            if (!zipDirectoryFile.exists()) {
                zipDirectoryFile.mkdirs()
            }
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun Project.createUploadPublicationTask(
    publishing: PublishingExtension,
    publication: MavenPublication
): DefaultTask {
    val username = localProperties.getProperty("mavenUsername")
    val password = localProperties.getProperty("mavenPassword")
    val repoFile =
        file((publishing.repositories.named("localRepo").get() as MavenArtifactRepository).url)
    val zipFile =
        file(repoFile.absolutePath + "/zipped/${publication.artifactId}/${publication.version}/${publication.artifactId}-${publication.version}.zip")
    return tasks.create<DefaultTask>("upload${publication.name}Publication") {
        group = "PrivMX Deployment"
        dependsOn("zip${publication.name}Publication")
        doFirst {
            val client = HttpClients.createDefault()
            val httpEntity = MultipartEntityBuilder.create()
                .addBinaryBody("bundle", zipFile)
                .build()
            val deploymentName = when (publication.name) {
                "kotlinMultiplatform" -> URLEncoder.encode(
                    "${publication.pom.name.get()} ${publication.version}",
                    "UTF-8"
                )

                else -> URLEncoder.encode(
                    "${publication.pom.name.get()}-${publication.name} ${publication.version}",
                    "UTF-8"
                )
            }
            val post =
                HttpPost("https://central.sonatype.com/api/v1/publisher/upload?name=$deploymentName")
            post.entity = httpEntity

            val token = Base64.Default.encode("$username:$password".encodeToByteArray())
            post.addHeader("Authorization", "Bearer $token")

            println(
                "deploymentID: ${
                    client.execute(post).entity.content.readAllBytes().decodeToString()
                }"
            )
            client.close()
        }
    }
}

val Project.dokkaTaskConfiguration: AbstractDokkaTask.() -> Unit
    get() = {
        moduleName = project.name
        moduleVersion = docsVersion
        val taskName = when (this) {
            is DokkaTaskPartial -> "privmxEndpointHtmlPartial"
            is DokkaMultiModuleTask -> "privmxEndpointHtmlMultiModule"
            else -> "privmxEndpointHtml"
        }
        outputDirectory.set(file(layout.buildDirectory.file("dokka/$taskName")))
        pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
            templatesDir = file(rootProject.layout.projectDirectory.file("docs/templates"))
            customStyleSheets = listOf(
                rootProject.layout.projectDirectory.file("docs/styles/style.css").asFile,
                rootProject.layout.projectDirectory.file("docs/styles/main.css").asFile,
                rootProject.layout.projectDirectory.file("docs/styles/font-manrope-auto.css").asFile
            )
        }
    }

val Project.docsVersion: String
    get() = Regex("[0-9]+.[0-9]+").find(version.toString())?.value ?: "unspecified"