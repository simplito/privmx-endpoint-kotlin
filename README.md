# PrivMX Endpoint Kotlin

This repository provides Kotlin wrappers for the native C++ library used by PrivMX to handle
end-to-end (e2e) encryption.

PrivMX is a privacy-focused platform designed to offer secure collaboration solutions by integrating
robust encryption across various data types and communication methods. This project enables seamless
integration of PrivMX’s encryption functionalities in Java/Kotlin applications, preserving the
security and performance of the original C++ library while making its capabilities accessible in the
JVM ecosystem.

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
usage of our libraries. It can be utilized on Java Virtual Machines (JVM).

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
specific requirements. It is compatible with Java Virtual Machines (JVM).

This library implements models, exception catching, and the following modules:

- `CryptoApi` - Cryptographic methods used to encrypt/decrypt and sign your data or generate keys to
  work with PrivMX Bridge.
- `Connection` - Methods for managing connection with PrivMX Bridge.
- `ThreadApi` - Methods for managing Threads and sending/reading messages.
- `StoreApi` - Methods for managing Stores and sending/reading files.
- `InboxApi` - Methods for managing Inboxes and entries.

## Supported platforms

This Kotlin Multiplatform project is built with cross-platform compatibility and supports the
following platforms:

- **Android** - through the JVM target, making it easy to integrate with existing Android projects
  and fully compatible with the Android SDK.
- **iOS** - including both physical devices and simulators, allowing shared logic to run natively
  within iOS applications.

The use of Kotlin Multiplatform ensures that core functionalities are implemented once and reused
across supported platforms.

## Minimal Supported JDK Version

This project requires at least JDK 8 for compiling and running the Kotlin code. Make sure to use JDK
8 or a later version for full compatibility with the Android target and JVM support.

## Running on JVM

### Dependencies

In your project's `build.gradle.kts` add the necessary dependencies:

```
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.simplito.kotlin:privmx-endpoint-extra:$privmxLibVersion")

    // Use privmx-endpoint for the base Kotlin library:
    // implementation("com.simplito.kotlin:privmx-endpoint:$privmxLibVersion")
}
```

### Shared Libraries

For Kotlin projects, you need to install the shared native libraries in a specific path and pass
that path when running application:

`-Djava.library.path=<path_to_native_libraries>`

## Running on Android

### Permissions

Add the Internet permission (for network access) to your AndroidManifest.xml:

`<uses-permission android:name="android.permission.INTERNET" />`

### Dependencies

The Android setup uses the same Gradle dependencies as the JVM setup:

```
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.simplito.kotlin:privmx-endpoint-extra:$privmxLibVersion")

    // Use privmx-endpoint for the base Kotlin library:
    // implementation("com.simplito.kotlin:privmx-endpoint:$privmxLibVersion")
}
```

### Native Libraries

For Android, place the native libraries in the `src/main/jniLibs` directory.

## Running on Kotlin Multiplatform (KMP)

How to use PrivMX Endpoint Kotlin in your Kotlin Multiplatform (KMP) project:

### Add the dependencies

In your `build.gradle.kts` add the appropriate dependency in the commonMain or
platform-specific source set. For example:

```
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.simplito.kotlin:privmx-endpoint-extra:$privmxLibVersion")
            }
        }
    }
}
```

### Native libraries

Make sure native libraries are available depending on the platform.

#### JVM

You must provide the native files and run your app with:

`-Djava.library.path=<path_to_native_libraries>`

#### Android

You must provide the native files into a specific location in your project:

`src/main/jniLibs/<abi>/`

where `abi` can be one of the supported targets: armeabi-v7a, arm64-v8a, x86 or x86_64.

#### iOS

C interop is already preconfigured. The project assumes you have compiled native libraries under:

`src/nativeInterop/cinterop/privmx-endpoint/<platform>/lib`

and headers under:

`src/nativeInterop/cinterop/privmx-endpoint/<platform>/include`

### Platform-specific setup (optional)

If your `sourceSets` are split into platform-specific modules (e.g. iosMain, androidMain, jvmMain),
and you use different logic per platform, you can include the dependency in jvmMain, iosMain, or
keep it in commonMain if your code works across all targets.

Example with platform-specific dependency:

```
val jvmMain by getting {
    dependencies {
        implementation("com.simplito.kotlin:privmx-endpoint-extra:$privmxLibVersion")
    }
}
```
