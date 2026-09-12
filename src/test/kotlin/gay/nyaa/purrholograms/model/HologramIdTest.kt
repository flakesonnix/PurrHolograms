package gay.nyaa.purrholograms.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HologramIdTest {
    @Test
    fun `accepts valid ids`() {
        assertEquals("shop", HologramId("shop").value)
        assertEquals("my-shop_1", HologramId("my-shop_1").value)
    }

    @Test
    fun `parse normalizes case and trims`() {
        assertEquals("shop", HologramId.parse("  SHOP ").value)
    }

    @Test
    fun `rejects too short`() {
        assertThrows<IllegalArgumentException> { HologramId("ab") }
    }

    @Test
    fun `rejects invalid chars`() {
        assertThrows<IllegalArgumentException> { HologramId("my shop!") }
    }

    @Test
    fun `isValid works`() {
        assertTrue(HologramId.isValid("shop-1"))
        assertFalse(HologramId.isValid("ab"))
        assertFalse(HologramId.isValid("bad id!"))
    }
}
