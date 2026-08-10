import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    java
    jacoco
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

    // Test infrastructure. JUnit (Jupiter) for everything; MockBukkit for
    // simulating a Bukkit/Paper server so code touching org.bukkit.* can be
    // exercised without a real server. Tests deliberately pin
    // `paper-api:26.1.2.build.74-stable` here rather than inheriting
    // whichever `-PpaperApiVersion` the main source set is compiled against
    // (default 26.2.build.111-stable): MockBukkit ships its own bundled
    // registry-data snapshot captured from that exact Paper build, and
    // letting a newer paper-api leak onto the test classpath produces a
    // real InternalDataLoadException at test RUNTIME (compiles fine) - the
    // same gotcha every sibling plugin in this program hit independently
    // (domains.critical.groups, domains.critical.regions,
    // domains.critical.command.example). So: tests run against exactly ONE
    // fixed target, 26.1.2, regardless of what the packaged jar targets.
    // Deliberately NOT extending testImplementation from compileOnly, for
    // the same reason.
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.papermc.paper:paper-api:26.1.2.build.74-stable")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.1.2:4.115.0")
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

// JaCoCo 0.8.15 is the newest published version (matches every sibling in
// this program) - the default Gradle-bundled version is older and does not
// fully understand Java 25 class files.
jacoco {
    toolVersion = "0.8.15"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// GOTCHA: helloworld.onEnable()/onDisable() are entirely branch-free (two
// straight-line log calls each, no if/switch/ternary/try-catch) - JaCoCo
// therefore emits NO BRANCH counter at all for this class (0 branches to
// measure, not 0-of-something-missed). A BRANCH minimum rule is included
// anyway for parity with the sibling bar (domains.critical.groups/
// domains.critical.command.example both enforce it); it passes trivially
// since there is nothing to violate, not because branch coverage was
// achieved on nonexistent branches. See PLAN.md.
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "1.00".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                minimum = "1.00".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
