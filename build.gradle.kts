plugins {
    java
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

group = "dev.hyspawn"

// Release builds pass -PpluginVersion=<version> (CI derives it from the git tag).
val explicitVersion = findProperty("pluginVersion") as String?
version = explicitVersion ?: "1.1.0"

// --- Auto-incrementing build number (local dev builds only) ---
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

// Include the project license and bundled dependency notices in both JARs.
tasks.withType<Jar>().configureEach {
    from(listOf("LICENSE", "NOTICE")) {
        into("META-INF")
    }
    from("licenses") {
        into("META-INF/licenses")
    }
}

// Keep the un-shaded jar out of the way so it can never be mistaken for the
// real artifact — only the shadow jar bundles the relocated dependencies.
tasks.jar {
    archiveClassifier.set("plain")
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set(
        if (explicitVersion != null) "HySpawn-${project.version}.jar" else "HySpawn-b${nextBuild}.jar"
    )
    relocate("com.github.Anon8281.universalScheduler", "dev.hyspawn.universalScheduler")
    relocate("org.bstats", "dev.hyspawn.bstats")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
