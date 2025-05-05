# PrivMX Endpoint Kotlin

This repository provides Kotlin wrappers for the native C++ library used by PrivMX to handle
end-to-end (e2e) encryption.

PrivMX is a privacy-focused platform designed to offer secure collaboration solutions by integrating
robust encryption across various data types and communication methods. This project enables seamless
integration of PrivMX’s encryption functionalities in Java/Kotlin applications, preserving the
security and performance of the original C++ library while making its capabilities accessible in the
Kotlin multiplatform projects.

## About PrivMX

[PrivMX](https://privmx.dev) allows developers to build end-to-end encrypted apps used for
communication. The Platform works according to privacy-by-design mindset, so all of our solutions
are based on Zero-Knowledge architecture. This project extends PrivMX’s commitment to security by
making its encryption features accessible to developers using Java/Kotlin.

### Key Features

- **End-to-End Encryption:** Ensures that data is encrypted at the source and can only be decrypted
  by the intended recipient.
- **Native C++ Library Integration:** Leverages the performance and security of C++ while making it
  accessible in Java/Kotlin applications.
- **Cross-Platform Compatibility:** Designed to support PrivMX on multiple operating systems and
  environments.
- **Simple API:** Easy-to-use interface for Java/Kotlin developers without compromising security.

## Modules

### 1. PrivMX Endpoint Kotlin Extra

PrivMX Endpoint Kotlin Extra is the fundamental **recommended library** for utilizing the platform
in the majority of cases. It encompasses all the essential logic that simplifies and secures the
usage of our libraries.

#### This library implements:

- Enums and static fields to minimize errors while invoking the methods.
- `PrivMXEndpoint` for managing the connection and registering callbacks for any events.
- `PrivMXEndpointContainer` for managing the global session with an implemented event loop.
- Classes to simplify reading and writing to files using byte arrays, InputStream/OutputStream or
  Source/Sink (kotlinx.io), depending on the platform and use case.

### 2. PrivMX Endpoint Kotlin

PrivMX Endpoint Kotlin is the fundamental wrapper library, essential for the Platform’s operational
functionality. It utilizes JNI to declare native functions in Kotlin. As the most minimalist library
available, it provides the highest degree of flexibility in customizing the Platform to meet your
specific requirements.

This library implements models, exception catching, and the following modules:

- `CryptoApi` - Cryptographic methods used to encrypt/decrypt and sign your data or generate keys to
  work with PrivMX Bridge.
- `Connection` - Methods for managing connection with PrivMX Bridge.
- `ThreadApi` - Methods for managing Threads and sending/reading messages.
- `StoreApi` - Methods for managing Stores and sending/reading files.
- `InboxApi` - Methods for managing Inboxes and entries.

## Supported platforms

* JVM (minimum JDK 8)
  * Android
    * arm64-v8a
    * armeabi-v7a
    * x86
    * x86_64
  * Darwin
    * arm64
  * Linux
    * x86_64 (comming soon)
  * Windows
    * x86_64 (comming soon)
* iOS
  * arm64
  * arm64Simulator

## Usage

### Add Dependencies

1. Add `mavenCentral()` repository to your `settings.gradle.kts`:

```groovy
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

2. Add dependency to `build.gradle.kts`:

```groovy
kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation("com.simplito.kotlin:privmx-endpoint:$privmxLibVersion")
        // optionally you can add privmx-endpoint-extra dependency
//        implementation("com.simplito.kotlin:privmx-endpoint-extra:$privmxLibVersion")
      }
    }
  }
}
```

### JVM
You have to pass path to PrivMX Endpoint native libraries directory by configuring 
`-Djava.library.path=<path-to-your-libraries-dir>` system property during run application.

**You can download pre-compiled zipped native binaries for each supported JVM platform from [GitHub Releases](https://github.com/simplito/privmx-endpoint-kotlin/releases).**

### Android
#### Native libraries
Before build your project you have to attach PrivMX Endpoint native libraries to Android build process by adding them 
to jniLibs sourceSet directory (`src/main/jniLibs` by default) for each architecture.

**You can download pre-compiled zipped native binaries for each supported Android architecture from [GitHub Releases](https://github.com/simplito/privmx-endpoint-kotlin/releases).**

#### Required permissions
PrivMX Endpoint requires to add the following permissions to your AndroidManifest.xml:
* `<uses-permission android:name="android.permission.INTERNET" />`  

## License information

**PrivMX Endpoint Kotlin**\
Copyright © 2025 Simplito sp. z o.o.

This project is part of the PrivMX Platform (https://privmx.dev). \
This project is Licensed under the MIT License.

PrivMX Endpoint and PrivMX Bridge are licensed under the PrivMX Free License.\
See the License for the specific language governing permissions and limitations under the License.