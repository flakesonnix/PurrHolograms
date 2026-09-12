package gay.nyaa.purrholograms.persistence

import gay.nyaa.purrholograms.model.Hologram
import gay.nyaa.purrholograms.model.HologramId
import gay.nyaa.purrholograms.model.HologramLine
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class HologramSerializerTest {
    @Test
    fun `lines roundtrip`() {
        val lines = listOf(
            HologramLine.TextLine("<red>Hi</red>"),
            HologramLine.ItemLine("DIAMOND", 3),
            HologramLine.SpacerLine(0.5),
        )
        val json = HologramSerializer.linesToJson(lines)
        assertEquals(lines, HologramSerializer.linesFromJson(json))
    }

    @Test
    fun `empty lines roundtrip`() {
        assertEquals(emptyList(), HologramSerializer.linesFromJson("[]"))
        assertEquals(emptyList(), HologramSerializer.linesFromJson(""))
    }

    @Test
    fun `hologram row roundtrip`() {
        val holo = Hologram(
            id = HologramId("shop"), world = "world",
            x = 1.5, y = 64.0, z = -2.5,
            lines = listOf(HologramLine.TextLine("hi")),
        )
        val row = HologramSerializer.hologramToRow(holo)
        val back = HologramSerializer.hologramFromRow(row)
        assertEquals(holo, back)
    }

    @Test
    fun `corrupt settings fall back to defaults`() {
        val s = HologramSerializer.settingsFromJson("not json{{{")
        assertEquals(32, s.viewDistance)
    }
}
