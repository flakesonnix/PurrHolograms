package gay.nyaa.purrholograms

import com.purrcore.PurrCorePlugin
import gay.nyaa.purrholograms.api.HologramManager
import gay.nyaa.purrholograms.command.HologramCommand
import gay.nyaa.purrholograms.integration.PurrItemsHook
import gay.nyaa.purrholograms.listener.HologramInteractionListener
import gay.nyaa.purrholograms.persistence.HologramRepository
import gay.nyaa.purrholograms.rendering.DisplayRenderer
import gay.nyaa.purrholograms.ui.HologramEditor
import org.bukkit.plugin.java.JavaPlugin

class PurrHologramsPlugin : JavaPlugin() {
    private lateinit var manager: HologramManager
    private lateinit var renderer: DisplayRenderer
    private lateinit var editor: HologramEditor

    override fun onEnable() {
        saveDefaultConfig()

        val purrCore = server.pluginManager.getPlugin("PurrCore") as? PurrCorePlugin
        if (purrCore == null) {
            logger.severe("PurrCore not found! Disabling...")
            server.pluginManager.disablePlugin(this)
            return
        }

        val repository = HologramRepository(purrCore.database, logger)
        try {
            repository.migrate()
        } catch (e: Exception) {
            logger.severe("DB migrate failed: ${e.message}")
        }

        manager = HologramManager()
        val itemsHook = PurrItemsHook(logger)
        itemsHook.refresh()
        renderer = DisplayRenderer(this, itemsHook)
        editor = HologramEditor(this, manager, repository, renderer)

        // load persisted holograms async, spawn on main thread
        server.scheduler.runTaskAsynchronously(
            this,
            Runnable {
                val all = try {
                    repository.loadAll()
                } catch (e: Exception) {
                    logger.warning("Failed to load holograms: ${e.message}")
                    emptyList()
                }
                server.scheduler.runTask(
                    this,
                    Runnable {
                        manager.loadAll(all)
                        all.forEach { renderer.render(it) }
                        logger.info("Loaded ${all.size} holograms")
                    },
                )
            },
        )

        server.pluginManager.registerEvents(editor, this)
        server.pluginManager.registerEvents(HologramInteractionListener(renderer), this)

        val executor = HologramCommand(manager, repository, renderer, editor)
        getCommand("hologram")?.apply {
            setExecutor(executor)
            tabCompleter = executor
        }

        Instance = this
        logger.info("PurrHolograms enabled (alpha)")
    }

    override fun onDisable() {
        if (::renderer.isInitialized) renderer.removeAll()
        Instance = null
        logger.info("PurrHolograms disabled")
    }

    fun getManager(): HologramManager = manager

    companion object {
        var Instance: PurrHologramsPlugin? = null
            private set

        fun get(): PurrHologramsPlugin = Instance ?: throw IllegalStateException("PurrHolograms not enabled")
    }
}
