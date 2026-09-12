package gay.nyaa.purrholograms.command

import gay.nyaa.purrholograms.api.HologramManager
import gay.nyaa.purrholograms.model.Hologram
import gay.nyaa.purrholograms.model.HologramId
import gay.nyaa.purrholograms.model.HologramLine
import gay.nyaa.purrholograms.persistence.HologramRepository
import gay.nyaa.purrholograms.rendering.DisplayRenderer
import gay.nyaa.purrholograms.rendering.MiniMessageValidator
import gay.nyaa.purrholograms.ui.HologramEditor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class HologramCommand(
    private val manager: HologramManager,
    private val repository: HologramRepository,
    private val renderer: DisplayRenderer,
    private val editor: HologramEditor,
) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }
        when (args[0].lowercase()) {
            "create" -> handleCreate(sender, args)
            "delete", "remove" -> handleDelete(sender, args)
            "list" -> handleList(sender)
            "tp", "teleport" -> handleTeleport(sender, args)
            "addline" -> handleAddLine(sender, args)
            "additem" -> handleAddItem(sender, args)
            "removeline" -> handleRemoveLine(sender, args)
            "movehere" -> handleMoveHere(sender, args)
            "edit" -> handleEdit(sender, args)
            else -> sendHelp(sender)
        }
        return true
    }

    private fun handleCreate(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage("Only players can create holograms (uses your location).")
            return
        }
        if (args.size < 2 || !HologramId.isValid(args[1])) {
            sender.sendMessage("Usage: /hologram create <id> (a-z 0-9 _ -, 3-32 chars)")
            return
        }
        val id = HologramId.parse(args[1])
        if (manager.get(id) != null) {
            sender.sendMessage("Hologram already exists: $id")
            return
        }
        val loc = sender.location
        val holo = Hologram(
            id = id,
            world = loc.world?.name ?: "world",
            x = loc.x, y = loc.y + 2.0, z = loc.z,
            lines = listOf(HologramLine.TextLine("<gradient:yellow:gold>New Hologram</gradient>")),
        )
        manager.create(holo)
        repository.save(holo)
        renderer.render(holo)
        sender.sendMessage("Created hologram $id. Use /hologram edit $id to customize.")
    }

    private fun handleDelete(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage("Usage: /hologram delete <id>")
            return
        }
        val id = HologramId.parse(args[1])
        renderer.remove(id.value)
        val gone = manager.delete(id)
        if (gone) {
            repository.delete(id)
            sender.sendMessage("Deleted hologram $id.")
        } else {
            sender.sendMessage("Unknown hologram: $id")
        }
    }

    private fun handleList(sender: CommandSender) {
        val all = manager.list()
        if (all.isEmpty()) {
            sender.sendMessage("No holograms yet. Create one with /hologram create <id>.")
            return
        }
        sender.sendMessage("Holograms (${all.size}):")
        for (h in all) sender.sendMessage(" - ${h.id} @ ${h.world} (${h.lines.size} lines)")
    }

    private fun handleTeleport(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage("Only players.")
            return
        }
        if (args.size < 2) {
            sender.sendMessage("Usage: /hologram tp <id>")
            return
        }
        val holo = manager.get(args[1]) ?: run {
            sender.sendMessage("Unknown hologram: ${args[1]}")
            return
        }
        val world = sender.server.getWorld(holo.world) ?: run {
            sender.sendMessage("World not loaded: ${holo.world}")
            return
        }
        sender.teleport(org.bukkit.Location(world, holo.x, holo.y - 2.0, holo.z))
        sender.sendMessage("Teleported to ${holo.id}.")
    }

    private fun handleAddLine(sender: CommandSender, args: Array<out String>) {
        if (args.size < 3) {
            sender.sendMessage("Usage: /hologram addline <id> <MiniMessage text...>")
            return
        }
        val holo = manager.get(args[1]) ?: run {
            sender.sendMessage("Unknown hologram: ${args[1]}")
            return
        }
        val text = args.drop(2).joinToString(" ")
        val errors = MiniMessageValidator.validate(text)
        if (errors.isNotEmpty()) {
            sender.sendMessage("Invalid MiniMessage: ${errors.joinToString("; ")}")
            return
        }
        try {
            val updated = manager.addLine(holo.id, HologramLine.TextLine(text))
            repository.save(updated)
            renderer.render(updated)
            sender.sendMessage("Line added to ${holo.id}.")
        } catch (e: IllegalArgumentException) {
            sender.sendMessage(e.message ?: "Cannot add line.")
        }
    }

    private fun handleAddItem(sender: CommandSender, args: Array<out String>) {
        if (args.size < 3) {
            sender.sendMessage("Usage: /hologram additem <id> <itemId> [amount]")
            return
        }
        val holo = manager.get(args[1]) ?: run {
            sender.sendMessage("Unknown hologram: ${args[1]}")
            return
        }
        val amount = args.getOrNull(3)?.toIntOrNull() ?: 1
        try {
            val updated = manager.addLine(holo.id, HologramLine.ItemLine(args[2], amount))
            repository.save(updated)
            renderer.render(updated)
            sender.sendMessage("Item line added to ${holo.id}.")
        } catch (e: IllegalArgumentException) {
            sender.sendMessage(e.message ?: "Cannot add item line.")
        }
    }

    private fun handleRemoveLine(sender: CommandSender, args: Array<out String>) {
        if (args.size < 3) {
            sender.sendMessage("Usage: /hologram removeline <id> <index>")
            return
        }
        val holo = manager.get(args[1]) ?: run {
            sender.sendMessage("Unknown hologram: ${args[1]}")
            return
        }
        val index = args[2].toIntOrNull() ?: run {
            sender.sendMessage("Index must be a number.")
            return
        }
        try {
            val updated = manager.removeLine(holo.id, index)
            repository.save(updated)
            renderer.render(updated)
            sender.sendMessage("Removed line $index from ${holo.id}.")
        } catch (e: IllegalArgumentException) {
            sender.sendMessage(e.message ?: "Cannot remove line.")
        }
    }

    private fun handleMoveHere(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage("Only players.")
            return
        }
        if (args.size < 2) {
            sender.sendMessage("Usage: /hologram movehere <id>")
            return
        }
        val holo = manager.get(args[1]) ?: run {
            sender.sendMessage("Unknown hologram: ${args[1]}")
            return
        }
        val loc = sender.location
        val updated = holo.moveTo(loc.world?.name ?: holo.world, loc.x, loc.y + 2.0, loc.z)
        manager.update(updated)
        repository.save(updated)
        renderer.render(updated)
        sender.sendMessage("Moved ${holo.id} to your location.")
    }

    private fun handleEdit(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage("Only players can use the GUI editor.")
            return
        }
        if (args.size < 2) {
            sender.sendMessage("Usage: /hologram edit <id>")
            return
        }
        val holo = manager.get(args[1]) ?: run {
            sender.sendMessage("Unknown hologram: ${args[1]}")
            return
        }
        editor.open(sender, holo)
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("Holograms: /hologram <create|delete|list|tp|addline|additem|removeline|movehere|edit>")
    }

    override fun onTabComplete(
        sender: CommandSender, cmd: Command, alias: String, args: Array<out String>,
    ): List<String> {
        if (args.size == 1) {
            return listOf("create", "delete", "list", "tp", "addline", "additem", "removeline", "movehere", "edit")
                .filter { it.startsWith(args[0].lowercase()) }
        }
        if (args.size == 2 && args[0].lowercase() != "create") {
            return manager.list().map { it.id.value }.filter { it.startsWith(args[1].lowercase()) }
        }
        return emptyList()
    }
}
