# minecraft_plugins (HelloWorld)

This repo holds exactly one plugin: **HelloWorld**, a deliberately trivial
Bukkit/Spigot/Paper server plugin. It logs one colored line to console on
enable and one on disable — no commands, no listeners, no config, no
dependencies. It exists as the minimal working example of this program's
modern plugin build setup.

## Modernization work

The original repo was an Eclipse project with no build system at all
(hand-laid source tree, committed IDE metadata). Modernization gave it:

- A single-project Gradle build at the repo root (Gradle 9.x wrapper,
  Java 25 toolchain auto-provisioned via foojay — no system JDK installs).
- A templated `plugin.yml` (`version: ${version}` expanded at build time).
- No shadow/shade plugin on purpose: the plugin has zero runtime
  dependencies, so plain `jar` is the whole packaging story
  (`build/libs/HelloWorld-<version>.jar`, a 5-file jar).
- A unit test and the same headless Paper boot smoke test as every sibling
  plugin repo.

Details and per-milestone status: `PLAN.md` / `CLAUDE.md`.

## Supported Paper versions

One codebase, no version branches. Default build targets the newest stable
Paper API (currently **26.2**); the same source compiles cleanly against
older API lines:

```sh
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
./gradlew build                                             # 26.2 (default)
./gradlew build -PpaperApiVersion=1.21.11-R0.1-SNAPSHOT
./gradlew build -PpaperApiVersion=1.20.1-R0.1-SNAPSHOT
./gradlew build -PpaperApiVersion=1.19.4-R0.1-SNAPSHOT
./gradlew build -PpaperApiVersion=1.18.2-R0.1-SNAPSHOT
```

All five targets are verified builds.

## Testing

1. **Unit tests** — `./gradlew check`.
2. **Headless Paper boot smoke test** (opt-in — needs a real Paper server
   jar):

   ```sh
   ./gradlew paperBootTest -PpaperServerJar=/path/to/paper-26.2-111.jar
   ```

   Boots a real headless Paper server with the packaged jar in `plugins/`
   and asserts: the jar loads, `onEnable` doesn't throw, the plugin shows
   in `plugins`, and `onDisable` exits cleanly (this plugin registers no
   commands, so there is no command assertion). Without a server jar the
   task reports `SKIPPED (this is a skip, not a pass)`. Transcript:
   `build/paper-boot/paper-boot-test.log`.
