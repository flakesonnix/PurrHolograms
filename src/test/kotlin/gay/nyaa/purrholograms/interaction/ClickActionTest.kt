package gay.nyaa.purrholograms.interaction

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClickActionTest {
    @Test
    fun `parses command and message`() {
        assertEquals(ClickAction.Command("shop open"), ClickAction.parse("command:shop open"))
        assertEquals(ClickAction.Command("x"), ClickAction.parse("cmd:x"))
        assertEquals(ClickAction.Message("hi"), ClickAction.parse("message:hi"))
        assertTrue(ClickAction.parse(null) is ClickAction.None)
        assertTrue(ClickAction.parse("none") is ClickAction.None)
    }

    @Test
    fun `bare text becomes message`() {
        assertEquals(ClickAction.Message("hello"), ClickAction.parse("hello"))
    }

    @Test
    fun `serialize roundtrip`() {
        val action = ClickAction.Command("shop")
        assertEquals(action, ClickAction.parse(action.serialize()))
    }
}
