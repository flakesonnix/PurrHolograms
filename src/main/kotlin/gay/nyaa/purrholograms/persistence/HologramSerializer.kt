package gay.nyaa.purrholograms.persistence

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import gay.nyaa.purrholograms.model.Hologram
import gay.nyaa.purrholograms.model.HologramId
import gay.nyaa.purrholograms.model.HologramLine
import gay.nyaa.purrholograms.model.HologramSettings

/**
 * JSON (de)serialization for hologram lines + full holograms.
 * Stored as TEXT column, keeps schema flexible for new line types.
 */
object HologramSerializer {
    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, Any>>() {}.type

    fun linesToJson(lines: List<HologramLine>): String {
        val maps = lines.map { lineToMap(it) }
        return gson.toJson(maps)
    }

    @Suppress("UNCHECKED_CAST")
    fun linesFromJson(json: String): List<HologramLine> {
        if (json.isBlank() || json == "[]") return emptyList()
        val raw: List<Map<String, Any>> = gson.fromJson(json, TypeToken.getParameterized(List::class.java, Map::class.java).type)
        return raw.map { lineFromMap(it) }
    }

    fun settingsToJson(settings: HologramSettings): String = gson.toJson(
        mapOf(
            "viewDistance" to settings.viewDistance,
            "updateIntervalTicks" to settings.updateIntervalTicks,
            "billboard" to settings.billboard,
            "seeThrough" to settings.seeThrough,
            "shadowed" to settings.shadowed,
            "persistent" to settings.persistent,
        ),
    )

    @Suppress("UNCHECKED_CAST")
    fun settingsFromJson(json: String): HologramSettings {
        if (json.isBlank()) return HologramSettings.defaults()
        return try {
            val map: Map<String, Any> = gson.fromJson(json, mapType)
            HologramSettings(
                viewDistance = (map["viewDistance"] as? Number)?.toInt() ?: 32,
                updateIntervalTicks = (map["updateIntervalTicks"] as? Number)?.toLong() ?: 20L,
                billboard = map["billboard"] as? String ?: "center",
                seeThrough = map["seeThrough"] as? Boolean ?: false,
                shadowed = map["shadowed"] as? Boolean ?: false,
                persistent = map["persistent"] as? Boolean ?: true,
            )
        } catch (_: Exception) {
            HologramSettings.defaults()
        }
    }

    fun hologramToRow(h: Hologram): Map<String, Any> = mapOf(
        "id" to h.id.value,
        "world" to h.world,
        "x" to h.x,
        "y" to h.y,
        "z" to h.z,
        "lines" to linesToJson(h.lines),
        "settings" to settingsToJson(h.settings),
    )

    fun hologramFromRow(row: Map<String, Any?>): Hologram = Hologram(
        id = HologramId(row["id"] as String),
        world = row["world"] as String,
        x = (row["x"] as Number).toDouble(),
        y = (row["y"] as Number).toDouble(),
        z = (row["z"] as Number).toDouble(),
        lines = linesFromJson(row["lines"] as? String ?: "[]"),
        settings = settingsFromJson(row["settings"] as? String ?: ""),
    )

    private fun lineToMap(line: HologramLine): Map<String, Any> = when (line) {
        is HologramLine.TextLine -> mapOf("type" to "text", "text" to line.text)
        is HologramLine.ItemLine -> mapOf("type" to "item", "itemId" to line.itemId, "amount" to line.amount)
        is HologramLine.SpacerLine -> mapOf("type" to "spacer", "height" to line.height)
    }

    private fun lineFromMap(map: Map<String, Any>): HologramLine = when (map["type"] as? String) {
        "item" -> HologramLine.ItemLine(
            itemId = map["itemId"] as? String ?: "STONE",
            amount = (map["amount"] as? Number)?.toInt() ?: 1,
        )
        "spacer" -> HologramLine.SpacerLine(
            height = (map["height"] as? Number)?.toDouble() ?: 0.25,
        )
        else -> HologramLine.TextLine(text = map["text"] as? String ?: "")
    }
}
