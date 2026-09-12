package gay.nyaa.purrholograms.rendering

import gay.nyaa.purrholograms.integration.PurrItemsHook
import gay.nyaa.purrholograms.model.Hologram
import gay.nyaa.purrholograms.model.HologramLine
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Display
import org.bukkit.entity.Interaction
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Spawns Paper Display entities per hologram line.
 * One Interaction hitbox per hologram for click handling.
 */
class DisplayRenderer(
    private val plugin: JavaPlugin,
    private val itemsHook: PurrItemsHook,
) {
    private val miniMessage = MiniMessage.miniMessage()
    private val spawned = ConcurrentHashMap<String, MutableList<UUID>>()
    private val hitboxToHologram = ConcurrentHashMap<UUID, String>()

    fun hologramIdForHitbox(entityId: UUID): String? = hitboxToHologram[entityId]

    fun isManaged(entityId: UUID): Boolean =
        hitboxToHologram.containsKey(entityId) || spawned.values.any { it.contains(entityId) }

    fun remove(id: String) {
        val ids = spawned.remove(id) ?: return
        for (uuid in ids) {
            Bukkit.getEntity(uuid)?.remove()
            hitboxToHologram.remove(uuid)
        }
    }

    fun removeAll() {
        for (id in spawned.keys.toList()) remove(id)
    }

    fun render(hologram: Hologram) {
        val world = Bukkit.getWorld(hologram.world) ?: return
        remove(hologram.id.value)

        val base = Location(world, hologram.x, hologram.y, hologram.z)
        val offsets = LineLayout.offsets(hologram.lines, base.y)
        val entities = mutableListOf<UUID>()

        hologram.lines.forEachIndexed { index, line ->
            val loc = Location(world, hologram.x, offsets[index], hologram.z)
            when (line) {
                is HologramLine.TextLine -> spawnText(loc, line.text, hologram)?.let { entities.add(it) }
                is HologramLine.ItemLine -> spawnItem(loc, line.itemId, line.amount)?.let { entities.add(it) }
                is HologramLine.SpacerLine -> Unit // gap only
            }
        }

        // click hitbox covering full height
        val totalH = LineLayout.totalHeight(hologram.lines).coerceAtLeast(0.5)
        try {
            val interaction = world.spawn(base.clone().add(0.0, -totalH / 2, 0.0), Interaction::class.java) { e ->
                e.interactionWidth = 2.0f
                e.interactionHeight = totalH.toFloat()
                e.isResponsive = true
            }
            entities.add(interaction.uniqueId)
            hitboxToHologram[interaction.uniqueId] = hologram.id.value
        } catch (_: Exception) {
            // Interaction not available on this fork — clicks disabled, displays still work
        }

        spawned[hologram.id.value] = entities
    }

    private fun spawnText(loc: Location, text: String, hologram: Hologram): UUID? = try {
        var entity: UUID? = null
        loc.world?.spawn(loc, org.bukkit.entity.TextDisplay::class.java) { display ->
            display.text(miniMessage.deserialize(text))
            display.billboard = billboard(hologram.settings.billboard)
            display.isSeeThrough = hologram.settings.seeThrough
            display.isShadowed = hologram.settings.shadowed
            display.viewRange = hologram.settings.viewDistance / 64f
            entity = display.uniqueId
        }
        entity
    } catch (_: Exception) {
        null
    }

    private fun spawnItem(loc: Location, itemId: String, amount: Int): UUID? = try {
        val stack = resolveStack(itemId, amount)
        var entity: UUID? = null
        loc.world?.spawn(loc, org.bukkit.entity.ItemDisplay::class.java) { display ->
            display.setItemStack(stack)
            display.billboard = Display.Billboard.CENTER
            entity = display.uniqueId
        }
        entity
    } catch (_: Exception) {
        null
    }

    private fun resolveStack(itemId: String, amount: Int): ItemStack {
        itemsHook.createItem(itemId, amount)?.let { return it }
        val material = Material.matchMaterial(itemId.uppercase()) ?: Material.STONE
        return ItemStack(material, amount.coerceIn(1, 64))
    }

    private fun billboard(raw: String): Display.Billboard = when (raw.lowercase()) {
        "fixed" -> Display.Billboard.FIXED
        "vertical" -> Display.Billboard.VERTICAL
        "horizontal" -> Display.Billboard.HORIZONTAL
        else -> Display.Billboard.CENTER
    }
}
