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