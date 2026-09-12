# PurrHolograms

Paper 1.21+ — Kotlin — hologram framework with MiniMessage, PurrItems displays and in-game editor.

Display-entity holograms with rich text, item lines, per-hologram settings, persistent storage and a GUI overview. No packet spam, no YAML hell.

## Features

- **Rich Text**: MiniMessage gradients, colors, decorations (validated on input)
- **Line Types**: `TextLine`, `ItemLine` (PurrItems or vanilla), `SpacerLine`
- **PurrItems Integration**: soft-depend, item lines resolve via PurrItems API with vanilla fallback
- **GUI Overview**: `/hologram edit <id>` shows lines + quick actions (remove last line, move here, delete)
- **Persistent**: HikariCP via PurrCore (`purrholograms` table), loads on enable
- **Placeholders**: `{player}` / `{name}` + custom map via `PlaceholderResolver` (per-viewer refresh planned)
- **Click Protection**: interaction hitbox cancelled, action bar identifies hologram (full click actions in beta)

## Commands

- `/hologram create <id>` - Create at your location
- `/hologram delete <id>` - Delete (aliases: remove)
- `/hologram list` - List all holograms
- `/hologram tp <id>` - Teleport to hologram
- `/hologram addline <id> <MiniMessage...>` - Add text line
- `/hologram additem <id> <itemId> [amount]` - Add item line
- `/hologram removeline <id> <index>` - Remove line by index
- `/hologram movehere <id>` - Move to your location
- `/hologram edit <id>` - Open GUI overview

**Aliases**: `/holo`, `/ph`

## Permissions

- `purrholograms.use` - All hologram commands (default: op)
- `purrholograms.admin` - Full admin (default: op)

## Building

```bash
gradle shadowJar
# → build/libs/PurrHolograms-0.1.0-alpha.jar
```

Requires `PurrCore`. Optionally integrates with `PurrItems`.

## Testing

```bash
gradle test
# unit tests: domain, placeholders, layout, minimessage, manager, serializer, clicks
```

## Development Status

**Alpha** - Core framework functional: CRUD, rendering, persistence, GUI overview.
Beta roadmap: per-player placeholder refresh task, click actions (`command:`/`message:`), chat-input line editor, distance-based visibility culling.

## Development

Pure logic lives outside Bukkit: `model/`, `rendering/` (resolver/layout/validator), `api/HologramManager`, `persistence/HologramSerializer`, `interaction/ClickAction`. Paper code at the edges: `rendering/DisplayRenderer`, `ui/HologramEditor`, `command/`, `listener/`.
