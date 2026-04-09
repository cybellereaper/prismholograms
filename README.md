# SpectraHolograms

A modern Paper hologram plugin using `TextDisplay` entities, designed for maintainability and extension.

## Features
- Multiline `TextDisplay` holograms
- Per-hologram YAML persistence (`holograms.yml`)
- CRUD commands (`/hologram`, `/holo` alias)
- Per-player show/hide state
- Placeholder pipeline with built-ins and optional PlaceholderAPI bridge
- Autosave and graceful malformed-entry handling

## Commands
- `/holo create <id> [text]`
- `/holo delete <id>`
- `/holo list`
- `/holo info <id>`
- `/holo tp <id>`
- `/holo movehere <id>`
- `/holo setline <id> <line> <text>`
- `/holo addline <id> <text>`
- `/holo insertline <id> <line> <text>`
- `/holo removeline <id> <line>`
- `/holo clearlines <id>`
- `/holo rename <oldId> <newId>`
- `/holo hide <id> [player]`
- `/holo show <id> [player]`
- `/holo reload`

## Permissions
- `spectraholograms.admin`
- `spectraholograms.create`
- `spectraholograms.delete`
- `spectraholograms.edit`
- `spectraholograms.view`
- `spectraholograms.teleport`
- `spectraholograms.reload`

## Build
```bash
./gradlew build
```

## Install
1. Build the plugin.
2. Copy the generated JAR from `build/libs` to your Paper server `plugins/` folder.
3. Start/restart the server.
