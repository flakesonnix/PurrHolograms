package gay.nyaa.purrholograms.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class HologramSettingsTest {
    @Test
    fun `defaults are sane`() {
        val s = HologramSettings.defaults()
        assertEquals(32, s.viewDistance)
        assertEquals("center", s.billboard)
    }

    @Test
    fun `rejects bad view distance`() {
        assertThrows<IllegalArgumentException> { HologramSettings(viewDistance = 2) }
        assertThrows<IllegalArgumentException> { HologramSettings(viewDistance = 500) }
    }

    @Test
    fun `rejects bad billboard`() {
        assertThrows<IllegalArgumentException> { HologramSettings(billboard = "spin") }
    }
}
