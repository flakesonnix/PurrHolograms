package gay.nyaa.purrholograms.integration

import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import java.util.logging.Logger

/**
 * Soft integration with PurrItems. Uses reflection-free safe cast;
 * falls back to null so DisplayRenderer uses vanilla materials.
 */
class PurrItemsHook(private val logger: Logger) {
    private var available = false

    fun refresh() {
        val plugin = Bukkit.getPluginManager().getPlugin("PurrItems")
        available = plugin != null && plugin.isEnabled
        if (available) logger.info("PurrItems integration enabled")
    }

    fun isAvailable(): Boolean {
        val plugin = Bukkit.getPluginManager().getPlugin("PurrItems")
        return plugin != null && plugin.isEnabled
    }

    fun createItem(itemId: String, amount: Int): ItemStack? {
        if (!isAvailable()) return null
        return try {
            val plugin = Bukkit.getPluginManager().getPlugin("PurrItems") ?: return null
            val api = plugin.javaClass.getMethod("getApi").invoke(plugin)
            val stack = api.javaClass.getMethod("createItem", String::class.java, Int::class.javaPrimitiveType)
                .invoke(api, itemId, amount) as? ItemStack
            stack
        } catch (e: Exception) {
            logger.fine("PurrItems lookup failed for $itemId: ${e.message}")
            null
        }
    }
}
