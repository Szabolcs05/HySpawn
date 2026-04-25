# HySpawn

A lightweight, Folia-compatible spawn management plugin for Paper, Folia, and Canvas servers.

## Features

- **Global Spawn** — Set a default spawn for first-join players and death respawns
- **Void Spawn** — Per-world void fall rescue teleportation
- **Spawn Command** — `/spawn` with configurable countdown, actionbar display, and movement cancellation
- **Command Cooldown** — Configurable cooldown between `/spawn` uses
- **Sounds** — Configurable sounds for teleport, countdown ticks, and cancellation
- **MiniMessage** — Full MiniMessage formatting support for all messages
- **PacketEvents** — Uses PacketEvents for actionbar messages
- **Folia Support** — Fully compatible with Folia/Canvas via [UniversalScheduler](https://github.com/Anon8281/UniversalScheduler)

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/setspawn` | Set the global spawn at your location | `hyspawn.admin` |
| `/spawn` | Teleport to spawn | `hyspawn.spawn` |
| `/setvoidspawn` | Set void spawn for current world | `hyspawn.admin` |
| `/unsetspawn` | Remove the global spawn | `hyspawn.admin` |
| `/unsetvoidspawn [world]` | Remove void spawn for a world | `hyspawn.admin` |
| `/hyspawn reload` | Reload configuration | `hyspawn.admin` |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `hyspawn.admin` | Admin commands (set/unset/reload) | OP |
| `hyspawn.spawn` | Use `/spawn` | Everyone |
| `hyspawn.bypass.countdown` | Skip the teleport countdown | OP |
| `hyspawn.bypass.cooldown` | Skip the command cooldown | OP |

## Dependencies

- [Paper](https://papermc.io/) 1.21.11+ (or Folia/Canvas)
- [PacketEvents](https://github.com/retrooper/packetevents) 2.12.1+

## Building

```bash
./gradlew shadowJar
```

Output jar will be in `build/libs/` with auto-incrementing build numbers (e.g. `HySpawn-b1.jar`).

## Author

- **Szabolcs**
- Discord: `szabolc.s`
- Website: [yoursit.ee/szabolcs](https://yoursit.ee/szabolcs)

## Star History

<a href="https://www.star-history.com/?repos=Szabolcs05%2FHySpawn&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/chart?repos=Szabolcs05/HySpawn&type=date&theme=dark&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/chart?repos=Szabolcs05/HySpawn&type=date&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/chart?repos=Szabolcs05/HySpawn&type=date&legend=top-left" />
 </picture>
</a>
