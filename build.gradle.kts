import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClients
import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.AbstractDokkaTask
import java.net.URLEncoder
import java.util.Properties
import kotlin.apply
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
    id("org.jetbrains.dokka") version "2.0.0"
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:2.0.0")
    }
}

val localProperties = Properties().apply {
    load(file(rootDir.absolutePath + "/local.properties").inputStream())
}


listOf(
    project(":privmx-endpoint"),
    project(":privmx-endpoint-extra"),
).forEach { currentProject ->

    currentProject.apply(plugin = "org.jetbrains.dokka")
    val dokkaTaskConfiguration: AbstractDokkaTask.() -> Unit = {
        outputDirectory.set(file(layout.buildDirectory.file("customHtml")))
//        val indexFile = outputDirectory.file("index.html").get().asFile
//        val svgFile = outputDirectory.file("ui-kit/assets/theme-toggle.svg").get().asFile
        pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
            templatesDir = file(rootProject.layout.projectDirectory.file("docs/templates"))
            customAssets = listOf(
                rootProject.layout.projectDirectory.file("docs/fonts/Manrope-VariableFont_wght.ttf").asFile
            )
            customStyleSheets = listOf(
                rootProject.layout.projectDirectory.file("docs/styles/style.css").asFile,
                rootProject.layout.projectDirectory.file("docs/styles/main.css").asFile,
                rootProject.layout.projectDirectory.file("docs/styles/font-manrope-auto.css").asFile
            )
        }
        doLast {
            copy {
                from(rootProject.layout.projectDirectory.file("docs/fonts"))
                into(outputDirectory.file("styles/fonts"))
            }
        }
//        doLast {
//            val content = svgFile.readText().replace(Regex("rgba\\([^\\)]*\\)"), "currentColor")
//            val buttonTag =
//                "<button class=\"navigation-controls--btn custom-header-icons\" id=\"theme-toggle-button\" type=\"button\">$content</button>"
//            indexFile.readText(Charsets.UTF_8).replace(
//                Regex(
//                    "<button class=\"navigation-controls--btn navigation-controls--btn_theme\"[^<]*</button>",
//                    RegexOption.MULTILINE
//                ), buttonTag
//            ).let {
//                indexFile.writeText(it)
//            }
//        }
    }
    currentProject.tasks.register<DokkaTask>("customHtml") {
        dokkaTaskConfiguration()
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

    val emptyJavadocJar = currentProject.tasks.register<Jar>("emptyJavadocJar"){
        archiveClassifier = "javadoc"
    }

    currentProject.afterEvaluate {
        val publishingExtension = currentProject.extensions.getByType<PublishingExtension>()
        publishingExtension
            .publications
            .withType<MavenPublication>()
            .configureEach {
                if(name == "jvm") {
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

fun MavenPublication.configurePom(){
    pom{
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

fun Project.createZipPublicationTask(publishing: PublishingExtension, publication: MavenPublication): Zip {
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
fun Project.createUploadPublicationTask( publishing: PublishingExtension, publication: MavenPublication): DefaultTask {
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
            val deploymentName = when(publication.name){
                "kotlinMultiplatform" -> URLEncoder.encode("${publication.pom.name.get()} ${publication.version}","UTF-8")
                else -> URLEncoder.encode("${publication.pom.name.get()}-${publication.name} ${publication.version}","UTF-8")
            }
            val post = HttpPost("https://central.sonatype.com/api/v1/publisher/upload?name=$deploymentName")
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