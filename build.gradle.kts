plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "2.2.20"
    application
}

group = "nick.mirosh"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral() // Add this if it's missing
    maven("https://jogamp.org/deployment/maven")
    google()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.telegram:telegrambots:6.0.1")
    implementation(project.dependencies.platform("io.insert-koin:koin-bom:4.1.0"))
    implementation("io.insert-koin:koin-core")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation(platform("org.mongodb:mongodb-driver-bom:5.5.1"))
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.mongodb:bson-kotlinx")

    val ktorVersion = "3.3.0"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-java:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

}

application {
    mainClass.set("nick.mirosh.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(19)
}