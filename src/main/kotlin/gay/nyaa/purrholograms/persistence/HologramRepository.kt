package gay.nyaa.purrholograms.persistence

import com.purrcore.db.Database
import gay.nyaa.purrholograms.model.Hologram
import gay.nyaa.purrholograms.model.HologramId
import java.util.logging.Logger

class HologramRepository(
    private val database: Database,
    private val logger: Logger,
) {
    fun migrate() {
        database.getConnection().use { conn ->
            conn.createStatement().execute(
                """
                CREATE TABLE IF NOT EXISTS purrholograms (
                    id VARCHAR(32) PRIMARY KEY,
                    world VARCHAR(64) NOT NULL,
                    x DOUBLE NOT NULL,
                    y DOUBLE NOT NULL,
                    z DOUBLE NOT NULL,
                    lines TEXT NOT NULL,
                    settings TEXT NOT NULL
                )
                """.trimIndent(),
            )
        }
        logger.info("DB migrate done (PurrHolograms)")
    }

    fun save(hologram: Hologram) {
        val row = HologramSerializer.hologramToRow(hologram)
        database.getConnection().use { conn ->
            // delete + insert = portable upsert for sqlite/mysql/pg
            conn.prepareStatement("DELETE FROM purrholograms WHERE id = ?").use { stmt ->
                stmt.setString(1, hologram.id.value)
                stmt.executeUpdate()
            }
            conn.prepareStatement(
                "INSERT INTO purrholograms (id, world, x, y, z, lines, settings) VALUES (?, ?, ?, ?, ?, ?, ?)",
            ).use { stmt ->
                stmt.setString(1, row["id"] as String)
                stmt.setString(2, row["world"] as String)
                stmt.setDouble(3, row["x"] as Double)
                stmt.setDouble(4, row["y"] as Double)
                stmt.setDouble(5, row["z"] as Double)
                stmt.setString(6, row["lines"] as String)
                stmt.setString(7, row["settings"] as String)
                stmt.executeUpdate()
            }
        }
    }

    fun loadAll(): List<Hologram> {
        database.getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT * FROM purrholograms")
                val out = mutableListOf<Hologram>()
                while (rs.next()) {
                    try {
                        out.add(
                            HologramSerializer.hologramFromRow(
                                mapOf(
                                    "id" to rs.getString("id"),
                                    "world" to rs.getString("world"),
                                    "x" to rs.getDouble("x"),
                                    "y" to rs.getDouble("y"),
                                    "z" to rs.getDouble("z"),
                                    "lines" to rs.getString("lines"),
                                    "settings" to rs.getString("settings"),
                                ),
                            ),
                        )
                    } catch (e: Exception) {
                        logger.warning("Skipping corrupt hologram row: ${e.message}")
                    }
                }
                return out
            }
        }
    }

    fun delete(id: HologramId): Boolean {
        database.getConnection().use { conn ->
            conn.prepareStatement("DELETE FROM purrholograms WHERE id = ?").use { stmt ->
                stmt.setString(1, id.value)
                return stmt.executeUpdate() > 0
            }
        }
    }
}
