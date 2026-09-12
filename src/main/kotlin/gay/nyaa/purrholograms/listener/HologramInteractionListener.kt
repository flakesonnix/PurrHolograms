package gay.nyaa.purrholograms.listener

import gay.nyaa.purrholograms.rendering.DisplayRenderer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent

/**
 * Protects hologram entities from interaction (no item frames popping etc).
 * Click actions arrive in beta; for alpha we cancel + identify.
 */
class HologramInteractionListener(
    private val renderer: DisplayRenderer,
) : Listener {
    @EventHandler
    fun onInteract(event: PlayerInteractEntityEvent) {
        val id = event.rightClicked.uniqueId
        if (!renderer.isManaged(id)) return
        event.isCancelled = true
        val holoId = renderer.hologramIdForHitbox(id)
        if (holoId != null) {
            event.player.sendActionBar(net.kyori.adventure.text.Component.text("Hologram: $holoId"))
        }
    }
}
