plugins {
    java
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

group = "dev.hyspawn"
version = "1.0.1"

// --- Auto-incrementing build number ---
val buildNumberFile = file("build-number.txt")
val buildNumber: Int = if (buildNumberFile.exists()) {
    buildNumberFile.readText().trim().toIntOrNull() ?: 0
} else {
    0
}
val nextBuild = buildNumber + 1
buildNumberFile.writeText(nextBuild.toString())

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:2.12.1")
    implementation("com.github.Anon8281:UniversalScheduler:0.1.7")
    implementation("org.bstats:bstats-bukkit:3.2.1")
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("HySpawn-b${nextBuild}.jar")
    relocate("com.github.Anon8281.universalScheduler", "dev.hyspawn.universalScheduler")
    relocate("org.bstats", "dev.hyspawn.bstats")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
