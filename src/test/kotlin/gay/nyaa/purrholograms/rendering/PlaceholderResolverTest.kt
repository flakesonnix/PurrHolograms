package gay.nyaa.purrholograms.rendering

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlaceholderResolverTest {
    @Test
    fun `resolves player placeholders`() {
        assertEquals("Hi Steve!", PlaceholderResolver.resolve("Hi {player}!", "Steve"))
        assertEquals("Hi Steve!", PlaceholderResolver.resolve("Hi {name}!", "Steve"))
    }

    @Test
    fun `resolves custom placeholders`() {
        assertEquals("5 coins", PlaceholderResolver.resolve("{amount} coins", "x", mapOf("amount" to "5")))
    }

    @Test
    fun `leaves unknown placeholders`() {
        assertEquals("{foo} bar", PlaceholderResolver.resolve("{foo} bar", "Steve"))
    }

    @Test
    fun `extracts placeholders`() {
        assertEquals(setOf("player", "coins"), PlaceholderResolver.extractPlaceholders("Hi {player} you have {coins}"))
    }

    @Test
    fun `detects player placeholder`() {
        assertTrue(PlaceholderResolver.hasPlayerPlaceholder("Hi {player}"))
        assertFalse(PlaceholderResolver.hasPlayerPlaceholder("static text"))
    }
}
