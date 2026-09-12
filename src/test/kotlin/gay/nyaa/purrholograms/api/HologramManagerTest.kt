package gay.nyaa.purrholograms.api

import gay.nyaa.purrholograms.model.Hologram
import gay.nyaa.purrholograms.model.HologramId
import gay.nyaa.purrholograms.model.HologramLine
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HologramManagerTest {
    private lateinit var manager: HologramManager

    @BeforeEach
    fun setup() {
        manager = HologramManager()
    }

    private fun holo(id: String) = Hologram(
        id = HologramId(id), world = "world", x = 0.0, y = 64.0, z = 0.0,
    )

    @Test
    fun `create and get`() {
        manager.create(holo("shop"))
        assertEquals("shop", manager.get(HologramId("shop"))?.id?.value)
        assertEquals("shop", manager.get("SHOP")?.id?.value)
    }

    @Test
    fun `duplicate create fails`() {
        manager.create(holo("shop"))
        assertThrows<IllegalArgumentException> { manager.create(holo("shop")) }
    }

    @Test
    fun `delete removes`() {
        manager.create(holo("shop"))
        assertEquals(true, manager.delete(HologramId("shop")))
        assertNull(manager.get("shop"))
        assertEquals(false, manager.delete(HologramId("shop")))
    }

    @Test
    fun `update unknown fails`() {
        assertThrows<IllegalArgumentException> { manager.update(holo("ghost")) }
    }

    @Test
    fun `add and remove lines`() {
        manager.create(holo("shop"))
        manager.addLine(HologramId("shop"), HologramLine.TextLine("hi"))
        assertEquals(1, manager.get("shop")!!.lines.size)
        manager.removeLine(HologramId("shop"), 0)
        assertEquals(0, manager.get("shop")!!.lines.size)
    }

    @Test
    fun `loadAll replaces state`() {
        manager.create(holo("old"))
        manager.loadAll(listOf(holo("aaa"), holo("bbb")))
        assertEquals(2, manager.size())
        assertNull(manager.get("old"))
    }
}
