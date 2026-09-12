package gay.nyaa.purrholograms.api

import gay.nyaa.purrholograms.model.Hologram
import gay.nyaa.purrholograms.model.HologramId
import gay.nyaa.purrholograms.model.HologramLine

/**
 * In-memory registry for holograms. Thread-safe for main-thread use.
 * Persistence lives in HologramRepository, rendering in DisplayRenderer.
 */
class HologramManager {
    private val holograms = LinkedHashMap<String, Hologram>()

    fun create(hologram: Hologram): Hologram {
        require(!holograms.containsKey(hologram.id.value)) {
            "Hologram already exists: ${hologram.id}"
        }
        holograms[hologram.id.value] = hologram
        return hologram
    }

    fun get(id: HologramId): Hologram? = holograms[id.value]

    fun get(id: String): Hologram? = holograms[id.lowercase().trim()]

    fun list(): List<Hologram> = holograms.values.toList()

    fun size(): Int = holograms.size

    fun delete(id: HologramId): Boolean = holograms.remove(id.value) != null

    fun update(hologram: Hologram): Hologram {
        require(holograms.containsKey(hologram.id.value)) {
            "Unknown hologram: ${hologram.id}"
        }
        holograms[hologram.id.value] = hologram
        return hologram
    }

    fun addLine(id: HologramId, line: HologramLine): Hologram {
        val current = get(id) ?: throw IllegalArgumentException("Unknown hologram: $id")
        return update(current.addLine(line))
    }

    fun removeLine(id: HologramId, index: Int): Hologram {
        val current = get(id) ?: throw IllegalArgumentException("Unknown hologram: $id")
        return update(current.removeLine(index))
    }

    fun loadAll(holograms: Collection<Hologram>) {
        this.holograms.clear()
        for (h in holograms) this.holograms[h.id.value] = h
    }

    fun clear() = holograms.clear()
}
