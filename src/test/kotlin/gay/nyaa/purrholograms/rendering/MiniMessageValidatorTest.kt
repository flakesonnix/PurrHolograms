package gay.nyaa.purrholograms.rendering

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MiniMessageValidatorTest {
    @Test
    fun `accepts plain and simple tags`() {
        assertTrue(MiniMessageValidator.isValid("Hello world"))
        assertTrue(MiniMessageValidator.isValid("<red>Hello</red>"))
        assertTrue(MiniMessageValidator.isValid("<gradient:yellow:gold>Shop</gradient>"))
    }

    @Test
    fun `rejects unclosed tags`() {
        assertFalse(MiniMessageValidator.isValid("<red>Hello"))
    }

    @Test
    fun `rejects legacy codes`() {
        assertFalse(MiniMessageValidator.isValid("§aHello"))
    }

    @Test
    fun `strips tags`() {
        assertTrue(MiniMessageValidator.stripTags("<red>Hi</red>").contains("Hi"))
        assertFalse(MiniMessageValidator.stripTags("<red>Hi</red>").contains("<red>"))
    }
}
