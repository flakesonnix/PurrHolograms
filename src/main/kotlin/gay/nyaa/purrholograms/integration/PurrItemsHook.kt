package gay.nyaa.purrholograms.integration

import gay.nyaa.purritems.PurrItemsPlugin
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.logging.Logger

/**
 * Soft integration with PurrItems via its public API.
 * Falls back to null so DisplayRenderer uses vanilla materials.
 *
 * NOTE: PurrItems exposes `getAPI()` (capital I). A previous version used
 * reflection against `getApi()` which never existed, so item lines silently
 * never resolved. Do NOT go back to reflection here.
 */
class PurrItemsHook(
    private val logger: Logger,
    private val pluginLookup: () -> Plugin? = { Bukkit.getPluginManager().getPlugin("PurrItems") },
) {
    fun refresh() {
        if (isAvailable()) logger.info("PurrItems integration enabled")
    }

    fun isAvailable(): Boolean {
        val plugin = pluginLookup()
        return plugin != null && plugin.isEnabled
    }

    fun createItem(
        itemId: String,
        amount: Int,
    ): ItemStack? {
        val plugin = pluginLookup() as? PurrItemsPlugin ?: return null
        if (!plugin.isEnabled) return null
        return try {
            plugin.api.createItem(itemId, amount)
        } catch (e: Exception) {
            logger.fine("PurrItems lookup failed for $itemId: ${e.message}")
            null
        }
    }
}
