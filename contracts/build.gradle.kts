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
    implementation("com.google.protobuf:protobuf-java:$protobufVersion")
    implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")
    implementation("com.google.protobuf:protobuf-java-util:$protobufVersion")
}
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
}