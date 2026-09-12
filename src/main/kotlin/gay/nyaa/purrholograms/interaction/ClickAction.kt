package gay.nyaa.purrholograms.interaction

/**
 * Click action attached to a hologram. Pure data, no Bukkit.
 * Serialized as "type:payload" e.g. "command:shop open", "message:Hello!".
 */
sealed interface ClickAction {
    val type: String
    val payload: String

    fun serialize(): String = "$type:$payload"

    data class Command(val command: String) : ClickAction {
        override val type = "command"
        override val payload get() = command
        init { require(command.isNotBlank()) { "Command cannot be blank" } }
    }

    data class Message(val message: String) : ClickAction {
        override val type = "message"
        override val payload get() = message
        init { require(message.isNotBlank()) { "Message cannot be blank" } }
    }

    data object None : ClickAction {
        override val type = "none"
        override val payload = ""
    }

    companion object {
        fun parse(raw: String?): ClickAction {
            if (raw.isNullOrBlank() || raw == "none") return None
            val idx = raw.indexOf(':')
            if (idx < 0) return Message(raw)
            return when (raw.substring(0, idx).lowercase()) {
                "command", "cmd" -> Command(raw.substring(idx + 1))
                "message", "msg" -> Message(raw.substring(idx + 1))
                "none" -> None
                else -> Message(raw)
            }
        }
    }
}
