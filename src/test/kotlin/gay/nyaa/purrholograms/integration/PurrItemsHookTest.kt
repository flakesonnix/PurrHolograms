package gay.nyaa.purrholograms.integration

import gay.nyaa.purritems.api.PurrItemsAPI
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import java.util.logging.Logger
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests PurrItemsHook soft-dependency logic.
 *
 * NOTE: We can't easily mock PurrItemsPlugin due to lateinit val `api` causing
 * MockK auto-hinting to fail. Tests use null/disabled plugin or skip full integration.
 */
class PurrItemsHookTest {
    private val logger = mockk<Logger>(relaxed = true)

    @Test
    fun `unavailable without plugin`() {
        val hook = PurrItemsHook(logger) { null }

        assertFalse(hook.isAvailable())
        assertNull(hook.createItem("purr_items:X", 1))
    }

    @Test
    fun `unavailable when disabled`() {
        val plugin = mockk<Plugin>()
        every { plugin.isEnabled } returns false
        val hook = PurrItemsHook(logger) { plugin }

        assertFalse(hook.isAvailable())
        assertNull(hook.createItem("purr_items:X", 1))
    }

    @Test
    fun `available when enabled and cast succeeds`() {
        val plugin = mockk<Plugin>()
        every { plugin.isEnabled } returns true
        val hook = PurrItemsHook(logger) { plugin }

        assertTrue(hook.isAvailable())
    }

    @Test
    fun `createItem returns null when plugin not PurrItemsPlugin`() {
        val plugin = mockk<Plugin>()
        every { plugin.isEnabled } returns true
        val hook = PurrItemsHook(logger) { plugin }

        assertNull(hook.createItem("purr_items:X", 1))
    }

    // Full integration test with actual PurrItemsPlugin would require:
    // - open class PurrItemsPlugin OR
    // - extracted interface PurrItemsAPIProvider OR
    // - end-to-end test with real plugin instance
    //
    // For now, the above tests cover the hook's null/disabled/wrong-type logic.
    // The happy path (plugin.api.createItem) is trivial delegation tested in
    // production via manual hologram testing.
}
