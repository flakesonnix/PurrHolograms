package gay.nyaa.purrholograms.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class HologramTest {
    private fun holo(lines: List<HologramLine> = emptyList()) = Hologram(
        id = HologramId("shop"),
        world = "world",
        x = 1.0, y = 64.0, z = -3.0,
        lines = lines,
    )

    @Test
    fun `add and remove lines`() {
        var h = holo()
        h = h.addLine(HologramLine.TextLine("hi"))
        h = h.addLine(HologramLine.ItemLine("STONE"))
        assertEquals(2, h.lines.size)
        assertEquals(1, h.textLineCount())
        assertEquals(1, h.itemLineCount())
        h = h.removeLine(0)
        assertEquals(1, h.lines.size)
    }

    @Test
    fun `max 20 lines enforced`() {
        var h = holo((1..20).map { HologramLine.SpacerLine() })
        assertThrows<IllegalArgumentException> { h.addLine(HologramLine.TextLine("x")) }
        assertThrows<IllegalArgumentException> { h.withLines((1..21).map { HologramLine.SpacerLine() }) }
    }

    @Test
    fun `remove out of bounds fails`() {
        assertThrows<IllegalArgumentException> { holo().removeLine(0) }
    }

    @Test
    fun `moveTo updates position`() {
        val moved = holo().moveTo("nether", 5.0, 70.0, 5.0)
        assertEquals("nether", moved.world)
        assertEquals(70.0, moved.y)
    }

    @Test
    fun `blank world rejected`() {
        assertThrows<IllegalArgumentException> { holo().moveTo(" ", 0.0, 0.0, 0.0) }
    }
}
