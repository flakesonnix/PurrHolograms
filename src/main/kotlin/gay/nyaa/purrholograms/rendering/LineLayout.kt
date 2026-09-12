package gay.nyaa.purrholograms.rendering

import gay.nyaa.purrholograms.model.HologramLine

/**
 * Computes Y offsets for stacked display entities.
 * First line at baseY, each next line goes down.
 */
object LineLayout {
    const val TEXT_HEIGHT = 0.25
    const val ITEM_HEIGHT = 0.5

    fun heightOf(line: HologramLine): Double =
        when (line) {
            is HologramLine.TextLine -> TEXT_HEIGHT
            is HologramLine.ItemLine -> ITEM_HEIGHT
            is HologramLine.SpacerLine -> line.height
        }

    fun offsets(lines: List<HologramLine>, baseY: Double): List<Double> {
        val result = mutableListOf<Double>()
        var y = baseY
        for (line in lines) {
            result.add(y)
            y -= heightOf(line)
        }
        return result
    }

    fun totalHeight(lines: List<HologramLine>): Double = lines.sumOf { heightOf(it) }
}
