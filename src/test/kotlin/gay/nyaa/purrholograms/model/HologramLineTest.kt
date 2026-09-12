package gay.nyaa.purrholograms.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class HologramLineTest {
    @Test
    fun `text line requires content`() {
        assertThrows<IllegalArgumentException> { HologramLine.TextLine("  ") }
        assertThrows<IllegalArgumentException> { HologramLine.TextLine("x".repeat(501)) }
        assertEquals("text", HologramLine.TextLine("hi").type)
    }

    @Test
    fun `item line validates amount`() {
        assertThrows<IllegalArgumentException> { HologramLine.ItemLine("", 1) }
        assertThrows<IllegalArgumentException> { HologramLine.ItemLine("STONE", 0) }
        assertThrows<IllegalArgumentException> { HologramLine.ItemLine("STONE", 65) }
        assertEquals("item", HologramLine.ItemLine("STONE", 3).type)
    }

    @Test
    fun `spacer validates height`() {
        assertThrows<IllegalArgumentException> { HologramLine.SpacerLine(0.05) }
        assertThrows<IllegalArgumentException> { HologramLine.SpacerLine(5.0) }
        assertEquals("spacer", HologramLine.SpacerLine().type)
    }

    @Test
    fun `factory helpers`() {
        assertEquals(HologramLine.TextLine("a"), HologramLine.text("a"))
        assertEquals(HologramLine.ItemLine("X", 2), HologramLine.item("X", 2))
        assertEquals(HologramLine.SpacerLine(0.5), HologramLine.spacer(0.5))
    }
}
