import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    java
}

group = "helloworld"
version = providers.gradleProperty("pluginVersion").get()

// Java 25: the latest resolvable paper-api (26.2.build.111-stable, matching
// Minecraft's current calendar-versioned release) publishes Gradle module
// metadata requiring JVM 25 - the same constraint hit by the EpicFurnaces/
// Spigot-InvUnload siblings. The foojay-resolver-convention plugin (declared
// in settings.gradle.kts) auto-provisions this JDK into Gradle's own
// toolchain cache; it does not touch the system/Homebrew JDK 21 install.
// See PLAN.md/CLAUDE.md.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${providers.gradleProperty("paperApiVersion").get()}")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25)
}

// No shadow plugin: this plugin has zero runtime dependencies to bundle, so
// there is nothing for a shade/relocate step to do. Plain `jar` is enough -
// see CLAUDE.md for this call.
tasks.jar {
    archiveBaseName.set("HelloWorld")
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    filesMatching("plugin.yml") {
        expand(props)
    }
}
