plugins {
    `java-library`
    id("com.google.protobuf")
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
repositories {
    mavenCentral()
}

val protobufVersion = "4.36.2"
dependencies {
    api("com.google.protobuf:protobuf-java:$protobufVersion")
}
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
}