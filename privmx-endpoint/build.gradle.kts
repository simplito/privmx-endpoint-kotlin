@file:OptIn(ExperimentalEncodingApi::class)

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.bson.Document
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream

import java.util.Properties
import java.util.UUID
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.time.Duration.Companion.seconds

buildscript {
    dependencies {
        classpath("org.mongodb:mongodb-driver-kotlin-coroutine:5.3.0")
        classpath("org.jetbrains.kotlin:kotlin-reflect")
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("maven-publish")
    id("signing")
}

group = "com.simplito.kotlin"
version = libs.versions.publishPrivmxEndpoint.get()

kotlin {

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    listOf(
        iosSimulatorArm64(),
        iosArm64(),
    ).forEach {
        it.compilations.getByName("main") {
            cinterops {
                val libprivmxendpoint by creating {
                    this.extraOpts = listOf(
                        "-libraryPath",
                        "src/nativeInterop/cinterop/privmx-endpoint/${it.name}/lib",
                        "-compilerOpts",
                        "-Isrc/nativeInterop/cinterop/privmx-endpoint/${it.name}/include"
                    )
                    val headerFiles =
                        fileTree("src/nativeInterop/cinterop/privmx-endpoint/${it.name}/include").matching {
                            include("privmx/endpoint/**/cinterface/*.h")
                            include("Pson/pson.h")
                        }.files
                    headers(headerFiles)
                }
            }
        }
    }

    sourceSets {
        listOf(
            iosSimulatorArm64Main.get(),
            iosArm64Main.get(),
        ).forEach {
            it.dependsOn(iosMain.get())
        }

        listOf(
            iosSimulatorArm64Test.get(),
            iosArm64Test.get()
        ).forEach {
            it.dependsOn(iosTest.get())
        }

        val iosMain by getting {
            dependsOn(commonMain.get())
        }

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines)
        }

        commonTest {
            dependencies {
                implementation(libs.kotlinx.io.core)
                implementation(libs.kotlin.test)
            }
        }

        iosTest {
            dependsOn(commonTest.get())
        }
        jvmTest {}
    }
}

tasks.register<Jar>("desktopJar") {
    archiveClassifier = "desktop"
    val binariesDir = project(":jni-wrapper").layout.buildDirectory.dir("native/install/Darwin/$version").get()
    dependsOn(project(":jni-wrapper").tasks.named("compileDarwin"))
    from(binariesDir)
    include("**/**")
    into("lib/Darwin")
    destinationDirectory = layout.buildDirectory.dir("nativeJars/desktop")

    doFirst {
        binariesDir.asFile.listFiles()?.filter {
            it.isDirectory && !it.isHidden
        }?.forEach { archDir ->
            println(archDir.path)
            File("${archDir.path}/fileNames.txt").run {
                createNewFile()
                outputStream().use {
                    writeText(archDir.listFiles()?.joinToString(";") { it.name } ?: "")
                }
            }
        }
    }
}

tasks.register<Jar>("androidJar"){
    archiveClassifier="android"
    val binariesDir = project(":jni-wrapper").layout.buildDirectory.dir("native/install/Android/$version").get()
    dependsOn(project(":jni-wrapper").tasks.named("compileAndroid"))
    from(binariesDir)
    include("**/**")
    into("lib")
    destinationDirectory = layout.buildDirectory.dir("nativeJars/android")
}

publishing {
    repositories {
        val localProperties = Properties().apply {
            load(file(rootDir.absolutePath + "/local.properties").inputStream())
        }
        val repositoryURL: String = localProperties.getProperty("repositoryURL") ?: rootProject.layout.buildDirectory.get().dir("publications").asFile.absolutePath
        maven {
            name = "localRepo"
            url = uri(repositoryURL)
        }
    }

    publications {
        withType<MavenPublication>().configureEach {
            groupId = "com.simplito.kotlin"
            version = project.version as String
            if (this.name == "jvm") {
                artifact(tasks["desktopJar"])
                artifact(tasks["androidJar"])
            }
            afterEvaluate {
                pom {
                    name = "PrivMX Endpoint Kotlin"
                    description =
                        "PrivMX Endpoint Kotlin is a minimal wrapper library declaring native functions in Kotlin using JNI."
                    licenses {
                        license {
                            name = "Apache-2.0"
                            url =
                                "https://openssl-library.org/source/license/apache-license-2.0.txt"
                            comments = "OpenSSL native libraries license"
                        }

                        license {
                            name = "BSL-1.0"
                            url = "https://www.boost.org/LICENSE_1_0.txt"
                            comments = "POCO native libraries license"
                        }

                        license {
                            name = "LGPL-3.0-only"
                            url = "https://www.gnu.org/licenses/lgpl-3.0.txt"
                            comments = "GMP native libraries license"
                        }

                        license {
                            name = "PrivMX Free License ver. 1.0"
                            url = "https://github.com/simplito/privmx-endpoint/blob/aea8de762b3fe4e1054fb185a8ec2ce40c6f9ddf/LICENSE.md"
                            comments = "PrivMX Endpoint native libraries license"
                        }

                        license {
                            name = "PrivMX Free License ver. 1.0"
                            url = "https://github.com/simplito/pson-cpp/blob/46451d80eb8abc5897a644ff437916a48d185419/LICENSE.md"
                            comments = "pson-cpp native libraries license"
                        }
                    }
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

tasks.register("testsPreConfig") {
    doFirst {
        val COMPOSE_NETWORK = "endpoint_e2e_testing_network";
        val COMPOSE_PROJECT = "tests";
        val DOCKER_IMAGE = "simplito/privmx-bridge:latest"
        val id = UUID.randomUUID().toString();
        val hostPort = 3001;
        val containerName = "privmx_e2e_tests_$id"
        val dbName = "privmx_e2e_db_$id"
        val dataSet = "defaultDataset"

        val internalMongoUrl = "mongodb://privmx_test_mongo:27017/$dbName"
        val localMongoUrl = "mongodb://localhost:27017/$dbName?directConnection=true";

        val envVars = listOf(
            "PRIVMX_PORT=3000",
            "PRIVMX_MONGO_URL=$internalMongoUrl",
            "PRIVMX_WORKERS=1",
            "PMX_MIGRATION=Migration067AddNotificationCollection",
            "PMX_STREAM_ENABLED=true",
            "PRIVMX_HOSTNAME=0.0.0.0",
            // Internal Domains (must match service names in docker-compose)
            "PMX_MEDIA_SERVER_ALLOW_SELF_SIGNED_CERTS=true",
            "PMX_STREAMS_MEDIA_SERVER=janus",
            "PMX_STREAMS_TURN_SERVER=turn:127.0.0.1:3478",
            "PMX_STREAMS_TURN_SERVER_SECRET=my-secret-key",
        ).flatMap { it -> listOf("-e", it) };

        val client = MongoClient.create(localMongoUrl)
        val db = client.getDatabase(dbName)

        ProcessBuilder("docker", "rm", "-f", containerName).apply {
            redirectError()
            redirectInput()
            redirectOutput()
        }.start().apply {
            waitFor(90, TimeUnit.SECONDS)
            if (this.exitValue() != 0) {
                println("docker rm failed")
            }
        }
        val datasetDir = rootProject.layout.projectDirectory.dir("tests/$dataSet")
        runBlocking {
            project.loadDataSet(db, datasetDir)
        }

        ProcessBuilder(
            "docker",
            "run",
            "-d",
            "--name",
            containerName,
            "-p",
            "$hostPort:3000",
            "--network",
            COMPOSE_NETWORK,
            "--label",
            "com.docker.compose.project=${COMPOSE_PROJECT}",
            "--label",
            "com.docker.compose.service=e2e_worker",
            "--label",
            "com.docker.compose.oneoff=False",
            *envVars.toTypedArray(),
            "--add-host",
            "host.docker.internal:host-gateway",
            DOCKER_IMAGE,
        ).apply {
            redirectError()
            redirectInput()
            redirectOutput()
        }.start().apply {
            inputStream.use {
                println(it.readAllBytes().decodeToString())
            }
            errorStream.use {
                println(it.readAllBytes().decodeToString())
            }
        }
        runBlocking {
            waitForServerReady(hostPort, containerName)
        }
        val cliContext = CliContext.fromDataSet(datasetDir,containerName)
        val solutionId = cliContext.callCli("solution/createSolution","{\"name\": \"tests_solution\"}").jsonObject["solutionId"]!!.jsonPrimitive.content
        val contextId = cliContext.callCli("context/createContext","""{"solution": "$solutionId", "name": "tests_context", "description": "base context for tests", "scope": "private"}""").jsonObject["contextId"]!!.jsonPrimitive.content
        val context2Id = cliContext.callCli("context/createContext","""{"solution": "$solutionId", "name": "tests_context_2", "description": "second context for tests", "scope": "private"}""").jsonObject["contextId"]!!.jsonPrimitive.content
        val (privateKey, publicKey) = cliContext.callGenKeyPair()
        val userId = UUID.randomUUID().toString()
        val (privateKey2, publicKey2) = cliContext.callGenKeyPair()
        val user2Id = UUID.randomUUID().toString()

        cliContext.callCli("context/addUserToContext","""{"contextId": "$contextId", "userId": "$userId", "userPubKey": "$publicKey"}""")
        cliContext.callCli("context/addUserToContext","""{"contextId": "$contextId", "userId": "$user2Id", "userPubKey": "$publicKey2"}""")
        cliContext.callCli("context/addUserToContext","""{"contextId": "$context2Id", "userId": "$userId", "userPubKey": "$publicKey"}""")
        cliContext.callCli("context/addUserToContext","""{"contextId": "$context2Id", "userId": "$user2Id", "userPubKey": "$publicKey2"}""")
        IniData(privateKey,publicKey,userId,privateKey2,publicKey2,user2Id,solutionId,contextId,context2Id,"http://localhost:3001").apply {
            val file = project.layout.projectDirectory.dir("src/commonTest/resources").file("TestData.ini").asFile
            saveAsIniFile(file)
        }
    }
}

fun waitForServerReady(port: Int, containerName: String) = runBlocking {
    val url = "http://localhost:${port}/privmx-configuration.json"
    val client = HttpClients.createDefault()
    withTimeout(30.seconds) {
        while (isActive) {
            val isRunning = ProcessBuilder(
                "docker", "inspect", "-f", "{{.State.Running}}", containerName
            )
                .start()
                .inputStream
                .readAllBytes()
                .decodeToString()
                .trim()
            println(isRunning)
            if (isRunning != "true") {
//                    printContainerLogs(containerName);
            }
            runCatching {
                val request = HttpGet(url)
                client.execute(request).use {
                    if (it.statusLine.statusCode == 200) return@withTimeout
                }
            }
            delay(200)
        }
    }
    client.close()
    //printContainerLogs(containerName);
}

fun Project.loadDataSet(db: MongoDatabase, datasetDir: Directory) = runBlocking {
    if (!datasetDir.asFile.exists() || !datasetDir.asFile.isDirectory) return@runBlocking

    val collectionFiles = datasetDir.asFile.listFiles()
    for (file in collectionFiles) {
        println("db file ext: ${file.extension}")
        if (file.extension != "json") continue;
        val collectionName = file.nameWithoutExtension
        try {
            val fileAsJsonElement = Json.decodeFromString<JsonElement>(file.readText())
            runCatching {
                val collectionAsJson = fileAsJsonElement.jsonArray
                println("collectionAsJson")
                if (collectionAsJson.isNotEmpty()) {
                    println("collection is not empty")
                    try {
                        val documents = collectionAsJson.map { Document.parse(Json.encodeToString(it)) }
                        db.getCollection<Document>(collectionName).insertMany(documents)
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                    println("insertCollection")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to load collection $collectionName: ${e.message}")
        }
    }
}

//TODO: Add better error logs
data class CliContext(
    val apiKeyId: String,
    val apiKeySecret: String,
    val containerName: String
) {
    companion object {
        fun fromDataSet(dataSetDir: Directory, containerName: String): CliContext {
            if (!dataSetDir.asFile.exists()) throw IOException("Dataset directory doesn't exist.")

            val apiKeysCollectionFile = dataSetDir.asFile.listFiles().find { println(it.name);it.name == "api_key.json" }
                ?: throw IOException("This dataset not contain api_key.json file.")
            val apiKeysJsonContent = Json.decodeFromString<JsonElement>(apiKeysCollectionFile.readText())
            val apiKeysCollectionJson = try {
                apiKeysJsonContent.jsonArray
            } catch (_: Exception) {
                throw IOException("api_key.json file is not correct json array")
            }
            val apiKeyJson = try {
                apiKeysCollectionJson[0].jsonObject
            } catch (_: Exception) {
                throw IOException("api_key.json contains incorrect first api_key")
            }
            val apiKeyId = apiKeyJson["_id"]?.jsonPrimitive?.content ?: ""
            val apiKeySecret = apiKeyJson["secret"]?.jsonPrimitive?.content ?: ""
            return CliContext(apiKeyId, apiKeySecret, containerName)
        }
    }

    fun callCli(method: String, params: String): JsonElement {
        val cmd = listOf(
            "docker", "exec",
            "-e", "API_KEY_ID=$apiKeyId",
            "-e", "API_KEY_SECRET=$apiKeySecret",
            containerName, "pmxbridge_cli",
            method, params, "--json=."
        )

        try {
            return ProcessBuilder(cmd).start().run {
                println("executes cmd: ${cmd.joinToString(" ") { it }}")
                waitFor(30, TimeUnit.SECONDS)
                val result = Json.decodeFromString<JsonElement>(inputStream.readAllBytes().decodeToString())
                println("result : ${result}")
                result.jsonObject["result"] ?: throw RuntimeException("Unsupported result format")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        throw RuntimeException("Cannot execute cli method: $method.")
    }

    fun callGenKeyPair(): Pair<String,String> {
        val cmd = listOf(
            "docker", "exec",
            "-e", "API_KEY_ID=$apiKeyId",
            "-e", "API_KEY_SECRET=$apiKeySecret",
            containerName, "pmxbridge_genkeypair"
        )

        try {
            return ProcessBuilder(cmd).start().run {
                println("executes cmd: ${cmd.joinToString(" ") { it }}")
                waitFor(30, TimeUnit.SECONDS)
                val resultLines = inputStream.reader().readLines().map { it.split("=")[1] }
                println(resultLines.joinToString("\n"))
                resultLines[0] to resultLines[1]
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        throw RuntimeException("Cannot execute genKeyPair.")
    }
}


data class IniData(
    val userPrivKey: String,
    val userPubKey: String,
    val userId: String,
    val user2PrivKey: String,
    val user2PubKey: String,
    val user2Id: String,
    val solutionId: String,
    val contextId: String,
    val context2Id: String,
    val instanceUrl: String
) {
    fun saveAsIniFile(file: File) {
        val content = "[Login]\n" + (this::class as KClass<IniData>).declaredMemberProperties.joinToString("\n") {
            "${it.name} = ${it.get(this@IniData)}"
        }
        println("content:\n$content")
        file.parentFile.mkdirs()
        file.writeText(content)
    }
}

tasks.withType<KotlinJvmTest> {
    // Run each test class in seperated process with limit to 3 processes at one time
    //TODO: check runner platform and set correct path for it
    val jniPath = layout.projectDirectory.file("jniTestLibs/Linux/x86_64/").asFile.absolutePath
    systemProperty("java.library.path", jniPath)
    this.forkEvery = 1
    this.maxParallelForks = 3
    // configure junitPlatform
    useJUnitPlatform { }
    setIncludes(
        listOf(
            "*",
            "**/**",
        )
    )
}

tasks.withType<KotlinNativeSimulatorTest> {
    workingDir =
        layout.buildDirectory.dir("bin/$targetName/debugTest/resources").get().asFile.absolutePath
    // iosTest/kotlin-native configuration (at start base configuration is good enough)
    // run all test methods sequentially, so event should not fail tests
    val taskName = "copyCommonTestResourcesTo$targetName"
    this.dependsOn(taskName)
    tasks.register<Copy>(taskName) {
        from("src/commonTest/resources")
        into(layout.buildDirectory.dir("bin/$targetName/debugTest/resources"))
    }

}

tasks.register<Exec>("GeneratePemKey") {
    val path = "src/commonTest/resources/private-key.pem"
    commandLine(
        "openssl",
        "ecparam",
        "-name",
        "prime256v1",
        "-genkey",
        "-noout",
        "-out",
        path
    )
}

val pgp_key_uid = "user_id"
tasks.register("GeneratePGPKey") {
    val path = "privmx-endpoint/src/commonTest/resources/pgp-public-key.asc"
    var fingerprint = ""
    doFirst {
        val inPip = PipedInputStream()
        val outPip = PipedOutputStream(inPip)
        exec {
            commandLine(
                "/bin/bash",
                "-c",
                "gpg --no-tty --batch --yes --passphrase '' --quick-generate-key --with-fingerprint \"$pgp_key_uid\" secp256k1 sign never"
            )
            standardOutput = outPip;
            errorOutput = outPip;
        }

        val res = inPip.readAllBytes().decodeToString()
        Regex("(?:pub *secp256k1.*\\n *(.*))|(?:.*/(.*).rev'$)").find(res)?.let {
            fingerprint = it.groupValues[1].run { if(isNullOrBlank()) it.groupValues[2] else "" }
        }
        exec {
            commandLine(
                "/bin/bash",
                "-c",
                "gpg --armor --export $pgp_key_uid"
            )
            standardOutput = File(path).outputStream()
        }

        exec {
            isIgnoreExitValue = true
            commandLine(
                "/bin/bash",
                "-c",
                "gpg --no-tty --batch --yes --delete-secret-and-public-key \"$fingerprint\""
            )
        }
    }
}
