# Changelog

All notable changes to HySpawn are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-08-30

This release is about one thing: **players actually respawning where you set spawn.**
On Folia the global spawn was being ignored entirely on death, and everyone without a bed
was dumped at the world spawnpoint — usually a spawn-camping target. That is fixed, and
beds now behave the way players expect.

### Fixed

- **Respawn ignored the global spawn on Folia.** Folia's region-threaded respawn logic does
  not reliably fire `PlayerRespawnEvent` and mishandles `setRespawnLocation`
  ([Folia #229](https://github.com/PaperMC/Folia/issues/229),
  [#337](https://github.com/PaperMC/Folia/issues/337)), so the location HySpawn set was
  silently discarded and players respawned at their world's own spawnpoint — for example at
  the overworld's 0,0 rather than in your spawn world. HySpawn now detects Folia at startup
  and, instead of relying on those events, watches the dead player and moves them to the
  global spawn the moment they respawn. Paper and Spigot keep the original event-based path,
  which stays seamless with no teleport.
- **Bed and respawn anchor spawns were overridden.** A player who had slept in a bed or
  charged a respawn anchor was still sent to the global spawn on death. Beds and anchors now
  take priority, and only players without one are sent to the global spawn.
- **Global spawn stopped working when its world loaded late.** The spawn world was resolved
  once at startup, so if the world was created or loaded after HySpawn enabled — common with
  world-management plugins — the global spawn silently became unavailable and no redirect
  happened at all. Spawn locations now resolve their world on each use, and the console
  warns once per world if a configured spawn world is genuinely missing.
- **Returning players could still land at the world spawnpoint.** If a player's saved logout
  position became invalid (renamed or deleted world, corrupted position), the server placed
  them at the world spawnpoint on join. They are now redirected to the global spawn.

### Added

- **`spawn.respect-bed-spawn` config option** (default `true`). Leave it on for the vanilla
  expectation — beds and anchors win, everyone else goes to spawn. Set it to `false` to force
  every respawn to the global spawn regardless of beds.
- **bStats metrics** ([plugin 30946](https://bstats.org/plugin/bukkit/HySpawn/30946)).
  Anonymous server and player counts only, nothing identifying. Opt out for all plugins with
  `enabled: false` in `plugins/bStats/config.yml`.

### Changed

- Release jars are now named by version (`HySpawn-1.1.0.jar`) instead of an incrementing
  local build number, and the version reported by the plugin is derived from the release tag,
  so it can no longer drift out of sync.
- The un-shaded jar is published with a `plain` classifier so it can never be mistaken for the
  real, dependency-bundling jar.
- Releases are now built, published to GitHub, and uploaded to Modrinth automatically by CI,
  and every push and pull request is build-verified.

### Upgrading

Drop in the new jar and restart. **No configuration changes are required** — the new
`respect-bed-spawn` option defaults to `true` when it is absent, so existing `config.yml`
files keep working untouched. Add it to your config only if you want to turn it off.

Note the behaviour change: players with a bed or charged respawn anchor now respawn there
instead of at the global spawn. If you preferred the old always-global behaviour, set
`spawn.respect-bed-spawn: false`.

## [1.0.1] - 2026-04-25

### Added

- Legacy color code support (`&` codes) alongside MiniMessage formatting.

### Fixed

- CI build failures caused by a missing Gradle wrapper and a machine-local
  `gradle.properties` being committed.

## [1.0.0] - 2026-04-21

Initial release.

### Added

- Global spawn with `/setspawn`, `/spawn`, and `/unsetspawn`.
- Per-world void fall rescue with `/setvoidspawn` and `/unsetvoidspawn`.
- Configurable teleport countdown, actionbar display, and cancel-on-move.
- Configurable `/spawn` cooldown with bypass permissions.
- Configurable sounds for teleport, countdown ticks, and cancellation.
- Full MiniMessage formatting for every message.
- Folia and Canvas support via UniversalScheduler.

[1.1.0]: https://github.com/Szabolcs05/HySpawn/releases/tag/v1.1.0
[1.0.1]: https://github.com/Szabolcs05/HySpawn/releases/tag/v1.0.1
[1.0.0]: https://github.com/Szabolcs05/HySpawn/releases/tag/v1.0.0
