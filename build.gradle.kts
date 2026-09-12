plugins {
    kotlin("jvm") version "2.1.0"
    // Shadow removed — manual fatJar (shadow 8.1.1 can't read Java 21 bytecode, ASM 65).
}

group = "gay.nyaa"
version = "0.1.0-alpha"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly(fileTree("../PurrCore/build/libs") { include("*.jar") })
    compileOnly(fileTree("../PurrItems/build/libs") { include("*.jar") })

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    testImplementation(fileTree("../PurrCore/build/libs") { include("*.jar") })
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
    jvmToolchain(21)
}

// Manual fatJar — bundles runtimeClasspath (kotlin-stdlib + gson) without shadow ASM.
val shadowJar by tasks.registering(Jar::class) {
    archiveBaseName.set("PurrHolograms")
    archiveClassifier.set("")
    archiveVersion.set("0.1.0-alpha")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
    }
}
