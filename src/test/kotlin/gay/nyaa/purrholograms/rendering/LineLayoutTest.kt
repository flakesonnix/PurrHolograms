package gay.nyaa.purrholograms.rendering

import gay.nyaa.purrholograms.model.HologramLine
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LineLayoutTest {
    @Test
    fun `offsets stack downward`() {
        val lines = listOf(
            HologramLine.TextLine("a"),
            HologramLine.TextLine("b"),
            HologramLine.SpacerLine(0.5),
        )
        val offsets = LineLayout.offsets(lines, 100.0)
        assertEquals(listOf(100.0, 99.75, 99.5), offsets)
    }

    @Test
    fun `item is taller than text`() {
        assertEquals(0.5, LineLayout.heightOf(HologramLine.ItemLine("STONE")))
        assertEquals(0.25, LineLayout.heightOf(HologramLine.TextLine("x")))
    }

    @Test
    fun `total height sums lines`() {
        val lines = listOf(HologramLine.TextLine("a"), HologramLine.ItemLine("X"))
        assertEquals(0.75, LineLayout.totalHeight(lines))
    }
}
