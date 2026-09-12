package gay.nyaa.purrholograms.model

/**
 * Persistent hologram definition.
 * Position is world + xyz. Lines render top-to-bottom.
 */
data class Hologram(
    val id: HologramId,
    val world: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val lines: List<HologramLine> = emptyList(),
    val settings: HologramSettings = HologramSettings.defaults(),
) {
    init {
        require(world.isNotBlank()) { "World cannot be blank" }
        require(lines.size <= 20) { "Max 20 lines per hologram" }
    }

    fun withLines(newLines: List<HologramLine>): Hologram {
        require(newLines.size <= 20) { "Max 20 lines per hologram" }
        return copy(lines = newLines.toList())
    }

    fun addLine(line: HologramLine): Hologram {
        require(lines.size < 20) { "Max 20 lines per hologram" }
        return copy(lines = lines + line)
    }

    fun removeLine(index: Int): Hologram {
        require(index in lines.indices) { "Line index out of bounds: $index" }
        return copy(lines = lines.toMutableList().also { it.removeAt(index) })
    }

    fun moveTo(world: String, x: Double, y: Double, z: Double): Hologram {
        require(world.isNotBlank()) { "World cannot be blank" }
        return copy(world = world, x = x, y = y, z = z)
    }

    fun textLineCount(): Int = lines.count { it is HologramLine.TextLine }

    fun itemLineCount(): Int = lines.count { it is HologramLine.ItemLine }
}
