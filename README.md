# ItemSine

Makes ItemDisplay entities bob up and down with a smooth sine wave.

## Features
- Configurable speed (bobs per second) and height amplitude.
- Uses client‑side interpolation for smooth motion.
- Lightweight – one global task, no per‑entity memory leaks.

## Requirements
- Minecraft 1.19.4 or newer (uses display entities).
- Paper / Spigot / Fork.

## Installation
1. Drop the `ItemSine.jar` into your server's `plugins/` folder.
2. Restart the server.
3. Tag any ItemDisplay with `bob` (case‑sensitive).

## Configuration
`config.yml` (auto‑generated on first run):
```yaml
# How many full up‑down cycles per tick (decimal allowed)
bobs-per-tick: 0.1
# Movement height in blocks
amplitude: 0.5
