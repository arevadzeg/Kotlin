plugins {
    kotlin("jvm") version "2.0.20"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.3.4")
    implementation("io.ktor:ktor-server-netty:2.3.4")
    implementation("io.ktor:ktor-server-host-common:2.3.4")
    implementation("io.ktor:ktor-server-request-validation:2.3.4")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.4")
    implementation("io.ktor:ktor-serialization-jackson:2.3.4")
    implementation("ch.qos.logback:logback-classic:1.4.7") // Optional: Logging
    implementation("io.ktor:ktor-server-core:2.0.0") // Adjust the version as necessary
    implementation("io.ktor:ktor-server-netty:2.0.0") // Adjust the version as necessary
    implementation("io.ktor:ktor-serialization:2.0.0") // Ktor serialization support
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // Add this line
    implementation("org.xerial:sqlite-jdbc:3.36.0.3") // SQLite driver
    implementation("com.zaxxer:HikariCP:5.0.1") // Connection pooling library (optional, but good for managing connections)
    implementation("io.ktor:ktor-server-cors-jvm:2.3.4") // Ensure you're using the correct version
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("MainKt")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "MainKt"  // Use "MainKt" if no package is defined
        )
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // Set to Java 21
    }
}

tasks.test {
    useJUnitPlatform()
}
