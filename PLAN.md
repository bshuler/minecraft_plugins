# PLAN.md — HelloWorld modernization

## Goal

Get this plugin building and running on the latest Paper API, then walk
backward through older Minecraft versions as far as practical, and give an
honest answer on cross-platform (mod-loader) support. See `CLAUDE.md` for
architecture. This is a genuinely trivial plugin (one class, no
dependencies, no listeners/commands) — scope and effort are matched to that:
no dependency removal table, no hook exclusions, no provenance
investigation needed (original snapshot, confirmed by a single "initial
commit" in `git log`).

## Milestones

### 1. Docs + branch hygiene

- [x] `CLAUDE.md` written.
- [x] `PLAN.md` written (this file).
- [x] Renamed local branch `master` → `main`, pushed. GitHub's branch-rename
      API 422'd ("New branch already exists" — `main` was already pushed),
      so the default branch was set directly via `gh repo edit
      --default-branch main` instead. `master` left intact (not deleted).

### 2. Modernize (flatten + Gradle + plugin.yml)

- [x] Flattened the single plugin from `helloworld/` to the repo root as one
      Gradle project (see `CLAUDE.md` "Layout" for why — no second plugin
      ever justified the subdirectory, and it matches every sibling repo).
- [x] Moved source to the conventional `src/main/java/helloworld/
      helloworld.java` / `src/main/resources/plugin.yml` layout. Package/
      class name `helloworld.helloworld` kept as-is (documented judgment
      call in `CLAUDE.md` — renaming would be gratuitous).
- [x] Removed Eclipse artifacts (`.classpath`, `.project`, `.settings/`)
      from version control (`.gitignore`d in case they regenerate locally).
- [x] Added Gradle 9.x + wrapper, `settings.gradle.kts`
      (`foojay-resolver-convention`), `gradle.properties`
      (`pluginVersion`, `paperApiVersion`).
- [x] `build.gradle.kts`: Java 25 toolchain, `compileOnly` on `paper-api` at
      the `paperApiVersion` property, plain `jar` task (no shadow plugin —
      no dependencies exist to shade; see `CLAUDE.md`).
- [x] `plugin.yml`: added `api-version: "26.2"`, templated `version:
      ${version}` expanded by `processResources` from `pluginVersion`.
- [x] Confirmed `26.2.build.111-stable` is still the latest resolvable
      `paper-api` coordinate at build time (checked live
      `repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/
      maven-metadata.xml` — `lastUpdated` timestamp 2026-08-07, matching the
      `EpicFurnaces`/`Spigot-InvUnload` siblings' recorded value; not
      hardcoded from memory).

### 3. Cross-platform assessment

**Conclusion: Fabric/NeoForge/Forge are not applicable to this plugin** —
same reasoning as every other plugin in this program (see `CLAUDE.md`
"Platforms"), restated briefly rather than at length since a one-class
hello-world plugin has no novel wrinkle to add to it. Bukkit plugins run
against a stable, server-implementation-maintained API; mod loaders compile
directly against Minecraft's own classes and rebuild per version — porting
this onto a mod loader would be a from-scratch rewrite, not a port, and is
out of scope.

| Server implementation | Status | Notes |
|---|---|---|
| Paper | Supported (primary target) | Built and verified against this |
| Purpur | Expected-compatible | Superset-API Paper fork; nothing here uses anything Purpur would break |
| Folia | Expected-compatible | No scheduler/global-state usage at all (just two log lines in `onEnable`/`onDisable`) — the usual Folia region-threading concerns don't apply to this plugin |
| Spigot | Expected-compatible | Only stable Bukkit/Spigot API (`Bukkit.getLogger()`, `ChatColor`) is used |

### 4. Build + backward version walk

Paper publishes `paper-api` for older MC versions using the classic
`X.Y.Z-R0.1-SNAPSHOT` Maven coordinate, resolvable from
`repo.papermc.io/repository/maven-public/`. No per-version code branching
exists (one class, no version-sensitive API calls), so each target is built
by overriding the `paperApiVersion` Gradle property on the command line.

| Target | api-version needed | Status |
|---|---|---|
| 26.2 (latest, `26.2.build.111-stable`) | `26.2` | **Built.** Default target (no property override). Jar: 5 files, verified non-empty. |
| 1.21.11 (1.21.x) | `1.21` | **Built.** `-PpaperApiVersion=1.21.11-R0.1-SNAPSHOT`. Jar: 5 files, verified non-empty. |
| 1.20.1 | `1.20` | **Built.** `-PpaperApiVersion=1.20.1-R0.1-SNAPSHOT`. Jar: 5 files, verified non-empty. |
| 1.19.4 | `1.19` | **Built.** `-PpaperApiVersion=1.19.4-R0.1-SNAPSHOT`. Jar: 5 files, verified non-empty. |
| 1.18.2 | `1.18` | **Built.** `-PpaperApiVersion=1.18.2-R0.1-SNAPSHOT`. Jar: 5 files, verified non-empty. |

All five jars are byte-identical in structure: `META-INF/MANIFEST.MF`,
`helloworld/helloworld.class`, `plugin.yml` (5 entries counting the two
directory entries). `plugin.yml` inside the jar confirmed to have
`version: 0.0.1` (expanded from `${version}`) and `api-version: "26.2"` in
every build — the `api-version` in `plugin.yml` is a fixed compatibility
gate for the shipped target, not swapped per override build, matching how
the `EpicFurnaces`/`Spigot-InvUnload` siblings treat milestone-4 builds.

The latest (26.2) and 1.21.11/1.20.1/1.19.4 builds each print a
`compileJava` deprecation note (`ChatColor` is soft-deprecated on modern
Paper in favor of Adventure components); the 1.18.2 build does not, since
`ChatColor` wasn't yet deprecated against that older API surface. Left
as-is — `ChatColor` still compiles and works correctly on every target;
migrating to Adventure components is a style choice, not a functional fix,
and would be a gratuitous change to a one-class hello-world plugin.

### 5. Verification

- [x] `./gradlew build` (and `clean build -PpaperApiVersion=...`) for each
      target above — all five green.
- [x] `unzip -l build/libs/*.jar` for each — confirmed the compiled
      `helloworld/helloworld.class`, `plugin.yml` (with expanded version and
      `api-version`), and `META-INF/MANIFEST.MF` are actually present (not
      just BUILD SUCCESSFUL), same file count and sizes across all five
      targets.
- [x] Working tree left on a final `clean build` (default/26.2 target) so
      `build/libs/HelloWorld-0.0.1.jar` reflects the shipped target,
      `build/` untracked either way.

### 6. Phase 2: Test coverage — DONE

- [x] JUnit 5 (junit-bom 6.1.3) + JaCoCo 0.8.15 + MockBukkit
      (`org.mockbukkit.mockbukkit:mockbukkit-v26.1.2:4.115.0`), the same
      known-good versions as every finished sibling in this program.
      MockBukkit is pinned to exactly ONE fixed Paper build on the **test**
      classpath, `io.papermc.paper:paper-api:26.1.2.build.74-stable` —
      deliberately NOT inherited from the main source set's
      `-PpaperApiVersion` (which defaults to `26.2.build.111-stable`), for
      the same `InternalDataLoadException`-at-runtime reason documented in
      the sibling repos' `build.gradle.kts`. No Mockito was added: the
      single class has no fields to mock/spy and no branches that need
      stubbing to reach.
- [x] `jacocoTestCoverageVerification` enforces LINE minimum `1.00` (and a
      BRANCH minimum `1.00` for parity with the sibling bar, though it
      passes trivially — see next bullet); `check` depends on it.
- [x] **Result: 100% line coverage, zero exclusions.** 1 class analyzed
      (`helloworld.helloworld`, the whole plugin) — confirmed via the JaCoCo
      XML report (`build/reports/jacoco/test/jacocoTestReport.xml`):
      `CLASS missed="0" covered="1"`, `LINE missed="0" covered="5"`,
      `INSTRUCTION missed="0" covered="21"`, `METHOD missed="0" covered="3"`
      (constructor + `onEnable` + `onDisable`) — nonzero and matching the
      expected 1-class scaffold (gotcha check: not a
      zero-classes-silently-passing false green). **No `<counter
      type="BRANCH">` element appears anywhere in the report at all** —
      `onEnable`/`onDisable` are entirely straight-line log calls with no
      `if`/`switch`/ternary/`try`-`catch`, so JaCoCo has zero branches to
      measure for this class (confirmed by grepping the XML directly, not
      assumed). No production code needed to be excluded from the coverage
      bar — the plugin's entire testable surface is one trivial class with
      no client rendering, no mixins, no static Bukkit singletons MockBukkit
      can't reach, no JDBC.
- [x] Both tests assert real behavior (captured log message content and
      level via a `java.util.logging.Handler` attached to
      `ServerMock.getLogger()`), not "constructs without throwing"
      placeholders. See `helloworldTest.java`.
- [x] **No bugs found** while writing this suite — the plugin's only
      behavior (two log lines on enable/disable) was already correct.
- [x] Coverage command: `./gradlew clean test jacocoTestReport
      jacocoTestCoverageVerification check`. See `CLAUDE.md` "Tests"
      section.

## Open problems / honest blockers

None. This plugin has no dependencies, no NMS access, and no deprecated-API
calls beyond soft-deprecated `ChatColor` (still present and functional on
every target built above) — it compiled and packaged unchanged against all
five Paper API targets, confirming the prediction made before the build
pass ran. Phase 2 testing found no bugs and needed no coverage exclusions.

## Repository / git notes

- Default branch to become `main` (renamed from `master`). `master` left in
  place, not deleted.
- Do not commit anything under `.github/workflows/` — the active `gh` token
  for the `bshuler` account lacks the `workflow` scope.
- Commits authored as `Bert Shuler <BertShuler@proton.me>`, signed via the
  1Password SSH agent. If signing fails with no human at the keyboard, the
  prepared commit message goes to the session scratchpad instead of being
  force-committed unsigned.
