package gay.nyaa.purrholograms.ui

import gay.nyaa.purrholograms.api.HologramManager
import gay.nyaa.purrholograms.model.Hologram
import gay.nyaa.purrholograms.model.HologramLine
import gay.nyaa.purrholograms.persistence.HologramRepository
import gay.nyaa.purrholograms.rendering.DisplayRenderer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

/**
 * In-game GUI overview for a hologram: shows lines + quick actions.
 * Line text editing stays in commands for alpha (chat-input comes in beta).
 */
class HologramEditor(
    private val plugin: JavaPlugin,
    private val manager: HologramManager,
    private val repository: HologramRepository,
    private val renderer: DisplayRenderer,
) : Listener {
    private val openSessions = mutableMapOf<String, String>() // playerName -> hologramId

    fun open(player: Player, hologram: Hologram) {
        val inv = Bukkit.createInventory(null, 27, Component.text("Hologram: ${hologram.id}"))

        hologram.lines.take(18).forEachIndexed { index, line ->
            inv.setItem(index, lineIcon(index, line))
        }

        inv.setItem(22, named(Material.BARRIER, "Remove last line"))
        inv.setItem(24, named(Material.ENDER_PEARL, "Move here"))
        inv.setItem(26, named(Material.REDSTONE_BLOCK, "Delete hologram"))

        openSessions[player.name] = hologram.id.value
        player.openInventory(inv)
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val holoId = openSessions[player.name] ?: return
        if (event.view.title() != Component.text("Hologram: $holoId")) return
        event.isCancelled = true

        val holo = manager.get(holoId) ?: run {
            player.closeInventory()
            openSessions.remove(player.name)
            return
        }
        when (event.rawSlot) {
            22 -> {
                if (holo.lines.isEmpty()) {
                    player.sendMessage("No lines to remove.")
                    return
                }
                val updated = manager.removeLine(holo.id, holo.lines.size - 1)
                repository.save(updated)
                renderer.render(updated)
                player.sendMessage("Removed last line.")
                player.closeInventory()
                openSessions.remove(player.name)
            }
            24 -> {
                val loc = player.location
                val updated = holo.moveTo(loc.world?.name ?: holo.world, loc.x, loc.y + 2.0, loc.z)
                manager.update(updated)
                repository.save(updated)
                renderer.render(updated)
                player.sendMessage("Moved ${holo.id} here.")
                player.closeInventory()
                openSessions.remove(player.name)
            }
            26 -> {
                renderer.remove(holo.id.value)
                manager.delete(holo.id)
                repository.delete(holo.id)
                player.sendMessage("Deleted ${holo.id}.")
                player.closeInventory()
                openSessions.remove(player.name)
            }
        }
    }

    fun closeSession(playerName: String) {
        openSessions.remove(playerName)
    }

    private fun lineIcon(index: Int, line: HologramLine): ItemStack {
        val (material, name) = when (line) {
            is HologramLine.TextLine -> Material.PAPER to "#$index text"
            is HologramLine.ItemLine -> Material.CHEST to "#$index item ${line.itemId}"
            is HologramLine.SpacerLine -> Material.GLASS_PANE to "#$index spacer"
        }
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta?.displayName(Component.text(name))
        meta?.lore(detail(line).map { Component.text(it) })
        item.itemMeta = meta
        return item
    }

    private fun detail(line: HologramLine): List<String> = when (line) {
        is HologramLine.TextLine -> listOf(line.text.take(60))
        is HologramLine.ItemLine -> listOf("${line.itemId} x${line.amount}")
        is HologramLine.SpacerLine -> listOf("gap ${line.height}")
    }

    private fun named(material: Material, name: String): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta?.displayName(Component.text(name))
        item.itemMeta = meta
        return item
    }
}
