plugins {
    kotlin("jvm") version "2.1.20"
    application
}

group = "nick.mirosh"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.telegram:telegrambots:6.0.1")
    implementation(project.dependencies.platform("io.insert-koin:koin-bom:4.1.0"))
    implementation("io.insert-koin:koin-core")
    implementation("io.ktor:ktor-client-core:3.3.0")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation(platform("org.mongodb:mongodb-driver-bom:5.5.1"))
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine")
    implementation("org.mongodb:bson-kotlinx")
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