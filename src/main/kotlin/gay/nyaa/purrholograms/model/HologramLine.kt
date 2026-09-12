package gay.nyaa.purrholograms.model

/**
 * Line types supported by PurrHolograms.
 * TextLine: MiniMessage rich text
 * ItemLine: PurrItems itemId or vanilla material key
 * SpacerLine: vertical gap
 */
sealed interface HologramLine {
    val type: String

    data class TextLine(val text: String) : HologramLine {
        override val type: String = "text"

        init {
            require(text.isNotBlank()) { "Text line cannot be blank" }
            require(text.length <= 500) { "Text line too long (max 500)" }
        }
    }

    data class ItemLine(
        val itemId: String,
        val amount: Int = 1,
    ) : HologramLine {
        override val type: String = "item"

        init {
            require(itemId.isNotBlank()) { "Item id cannot be blank" }
            require(amount in 1..64) { "Amount must be 1-64" }
        }
    }

    data class SpacerLine(val height: Double = 0.25) : HologramLine {
        override val type: String = "spacer"

        init {
            require(height in 0.1..2.0) { "Spacer height must be 0.1-2.0" }
        }
    }

    companion object {
        fun text(text: String): TextLine = TextLine(text)

        fun item(itemId: String, amount: Int = 1): ItemLine = ItemLine(itemId, amount)

        fun spacer(height: Double = 0.25): SpacerLine = SpacerLine(height)
    }
}
