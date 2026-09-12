package gay.nyaa.purrholograms.model

/**
 * Per-hologram display settings.
 */
data class HologramSettings(
    val viewDistance: Int = 32,
    val updateIntervalTicks: Long = 20L,
    val billboard: String = "center",
    val seeThrough: Boolean = false,
    val shadowed: Boolean = false,
    val persistent: Boolean = true,
) {
    init {
        require(viewDistance in 4..128) { "viewDistance must be 4-128" }
        require(updateIntervalTicks in 1..72000) { "updateInterval must be 1-72000 ticks" }
        require(billboard in setOf("fixed", "vertical", "horizontal", "center")) {
            "billboard must be fixed|vertical|horizontal|center"
        }
    }

    companion object {
        fun defaults(): HologramSettings = HologramSettings()
    }
}
