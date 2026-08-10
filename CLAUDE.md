# CLAUDE.md — minecraft_plugins (HelloWorld)

## What this is

This repo holds exactly one plugin: **HelloWorld**, a trivial Bukkit/Spigot/
Paper **server plugin** (not a client mod, not a Fabric/Forge/NeoForge mod).
It logs one colored line to console on enable and one on disable — no
commands, no listeners, no config, no dependencies. It is an original
snapshot in this repo (not a GitHub fork of anything). See `PLAN.md` for the
modernization plan, milestone status, and the version support matrix.

## Layout

```
build.gradle.kts           # single Gradle project, no submodules
settings.gradle.kts        # rootProject.name = "helloworld"
gradle.properties          # pluginVersion, paperApiVersion (override point)
src/main/java/helloworld/helloworld.java   # the whole plugin
src/main/resources/plugin.yml              # name/main/api-version, ${version} templated
```

The original repo had the plugin nested one level down in `helloworld/` with
an Eclipse project (`.classpath`/`.project`/`.settings/`) and a hand-written
source tree (`helloworld/src/helloworld/helloworld.java`, no `src/main/java`
convention) and no build system at all. This modernization **flattens the
single plugin to the repo root** as one Gradle project — there was never a
second plugin to justify a subdirectory, and every sibling repo in
`~/code/minecraft-mods/` (`EpicFurnaces`, `Spigot-InvUnload`,
`domains.critical.regions`) uses this same repo-root-is-the-project layout.
The package/class name `helloworld.helloworld` (lowercase class name, matches
the file name) is kept as-is — renaming a one-class hello-world plugin's
class to `HelloWorld` would be a gratuitous rename with no functional
benefit; noted here rather than silently changed.

Eclipse artifacts (`.classpath`, `.project`, `.settings/`) are removed from
version control (still `.gitignore`d in case a contributor's local Eclipse
regenerates them).

## Build

```bash
./gradlew build                                              # default target: latest Paper API
./gradlew build -PpaperApiVersion=1.20.1-R0.1-SNAPSHOT        # override target version
```

- Gradle 9.x (wrapper committed), Java 25 toolchain, auto-provisioned via
  `org.gradle.toolchains.foojay-resolver-convention` — do not install a
  system JDK for this. The system JDK (Temurin 21) is untouched. Java 25 is
  required because the latest `paper-api` coordinate's Gradle module
  metadata declares it as a minimum (discovered by a failed build against a
  Java 21 toolchain, not assumed in advance) — same finding as every other
  plugin in this program.
- No shadow/shade plugin: this plugin has **zero runtime dependencies** to
  bundle (only `compileOnly` on `paper-api`), so there is nothing for a
  shade/relocate step to do. Plain `jar` produces `build/libs/HelloWorld-
  <version>.jar`. (Every sibling repo uses `com.gradleup.shadow` because they
  have at least one dependency to shade or hooks to isolate; this plugin
  doesn't, so the plugin is simply omitted rather than added for
  consistency's sake alone.)
- `plugin.yml`'s `version:` is templated as `${version}` and expanded by
  `processResources` from `gradle.properties`' `pluginVersion`.
- Depends on `io.papermc.paper:paper-api` at the version named by the
  `paperApiVersion` Gradle property (`gradle.properties`, overridable with
  `-PpaperApiVersion=...`). Do not trust a cached memory of "the latest MC
  version" — query `https://fill.papermc.io/v3/projects/paper` or
  `repo.papermc.io`'s `maven-metadata.xml` for `paper-api` at build time;
  Minecraft is calendar-versioned now (26.x).

## Platforms

This is Bukkit-API software. "Platform" here means Bukkit-API server
implementations, not mod loaders:

- **Paper** (primary target), and by API compatibility, **Purpur** and
  **Folia** — the plugin only touches `Bukkit.getLogger()` and `ChatColor`
  on `onEnable`/`onDisable`, nothing that any of these forks change.
- **Spigot** — only stable Bukkit/Spigot API is used, no Paper-only calls.
- **Fabric / NeoForge / Forge are not applicable.** Bukkit plugins run
  against a stable, server-implementation-maintained API; Fabric/Forge/
  NeoForge mods compile directly against Minecraft's own (Mojang-mapped or
  obfuscated) classes and are rebuilt per MC version, with no equivalent
  stable API layer. Porting this plugin onto a mod loader would mean
  rewriting it from scratch against a different programming model, which is
  a rewrite, not a port — same conclusion reached for every other plugin in
  this program, restated briefly here since a hello-world plugin has no
  novel wrinkle worth a longer writeup. See `PLAN.md`.

See `PLAN.md` for the full version matrix and per-version build status.
