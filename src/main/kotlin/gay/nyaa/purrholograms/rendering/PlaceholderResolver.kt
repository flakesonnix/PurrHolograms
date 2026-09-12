package gay.nyaa.purrholograms.rendering

/**
 * Pure placeholder resolver.
 * Built-ins: {player} {name}
 * Custom: any {key} from extra map.
 * Unknown placeholders are left as-is.
 */
object PlaceholderResolver {
    fun resolve(text: String, playerName: String, extra: Map<String, String> = emptyMap()): String {
        if (text.isEmpty()) return text
        var out = text
        out = out.replace("{player}", playerName)
        out = out.replace("{name}", playerName)
        for ((k, v) in extra) {
            if (k.isBlank()) continue
            out = out.replace("{$k}", v)
        }
        return out
    }

    fun extractPlaceholders(text: String): Set<String> {
        val regex = Regex("\\{([a-zA-Z0-9_]+)\\}")
        return regex.findAll(text).map { it.groupValues[1] }.toSet()
    }

    fun hasPlayerPlaceholder(text: String): Boolean =
        text.contains("{player}") || text.contains("{name}")
}
