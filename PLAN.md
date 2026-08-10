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
- [ ] Renamed local branch `master` → `main`, pushed, set as GitHub default
      branch. `master` left intact (not deleted).

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
| 26.2 (latest, `26.2.build.111-stable`) | `26.2` | pending |
| 1.21.11 (1.21.x) | `1.21` | pending |
| 1.20.1 | `1.20` | pending |
| 1.19.4 | `1.19` | pending |
| 1.18.2 | `1.18` | pending |

(Table updated to Built/jar-verified status below once the build pass runs.)

### 5. Verification

- [ ] `./gradlew build` for each target above.
- [ ] `unzip -l build/libs/*.jar` for each — confirm the compiled
      `helloworld/helloworld.class`, `plugin.yml`, and `META-INF/MANIFEST.MF`
      are actually present (not just BUILD SUCCESSFUL).

## Open problems / honest blockers

None expected — this plugin has no dependencies, no NMS access, no
deprecated-API calls beyond soft-deprecated `ChatColor` (still present and
functional on current Paper), so it should compile and package unchanged
against every target above. Recorded as a prediction here; see the
Verification section above for the actual measured result.

## Repository / git notes

- Default branch to become `main` (renamed from `master`). `master` left in
  place, not deleted.
- Do not commit anything under `.github/workflows/` — the active `gh` token
  for the `bshuler` account lacks the `workflow` scope.
- Commits authored as `Bert Shuler <BertShuler@proton.me>`, signed via the
  1Password SSH agent. If signing fails with no human at the keyboard, the
  prepared commit message goes to the session scratchpad instead of being
  force-committed unsigned.
