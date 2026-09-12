package gay.nyaa.purrholograms.rendering

/**
 * Lightweight MiniMessage sanity check (no Adventure dep in unit tests).
 * Checks tag balance for <> tags, length, illegal chars.
 */
object MiniMessageValidator {
    private val tagRegex = Regex("</?([a-zA-Z0-9_:#\\-\\.]+)(:[^<>]*)?>")

    fun isValid(input: String): Boolean = validate(input).isEmpty()

    fun validate(input: String): List<String> {
        val errors = mutableListOf<String>()
        if (input.length > 500) errors.add("Text too long (max 500)")
        if (input.contains("§")) errors.add("Legacy § codes not allowed, use MiniMessage")

        val stack = ArrayDeque<String>()
        // Self-closing / known solo tags never need closing
        val solo = setOf("br", "newline", "reset")

        for (m in tagRegex.findAll(input)) {
            val full = m.value
            val name = m.groupValues[1].substringBefore(":").lowercase()
            if (name in solo) continue
            if (full.startsWith("</")) {
                if (stack.isEmpty() || stack.removeLast() != name) {
                    errors.add("Unbalanced closing tag: $full")
                }
            } else if (!full.endsWith("/>")) {
                // gradient/rainbow etc. are containers – push
                stack.addLast(name)
            }
        }
        if (stack.isNotEmpty()) errors.add("Unclosed tags: ${stack.joinToString(",")}")
        return errors
    }

    fun stripTags(input: String): String = tagRegex.replace(input, "")
}
