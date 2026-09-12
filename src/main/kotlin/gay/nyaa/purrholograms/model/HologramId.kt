package gay.nyaa.purrholograms.model

/**
 * Validated hologram identifier.
 * Lowercase, 3-32 chars, a-z 0-9 _ -
 */
@JvmInline
value class HologramId(val value: String) {
    init {
        require(value.length in 3..32) { "Hologram id must be 3-32 chars: $value" }
        require(value.matches(Regex("[a-z0-9_-]+"))) { "Hologram id must be lowercase alphanumeric + _ -: $value" }
    }

    override fun toString(): String = value

    companion object {
        fun parse(raw: String): HologramId = HologramId(raw.lowercase().trim())

        fun isValid(raw: String): Boolean =
            try {
                parse(raw)
                true
            } catch (_: IllegalArgumentException) {
                false
            }
    }
}
