package com.nova.assistant

import com.nova.assistant.domain.context.ConversationContext
import com.nova.assistant.domain.intent.NovaIntent
import com.nova.assistant.domain.parser.CommandParser
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CompoundCommandTest {

    private lateinit var parser: CommandParser
    private lateinit var context: ConversationContext

    @Before
    fun setUp() {
        context = ConversationContext()
        parser = CommandParser(context)
    }

    @Test
    fun testCompoundCommandSplitting() {
        val parsed = parser.parse("turn on flashlight and increase volume")
        assertEquals(2, parsed.size)
        assertEquals(NovaIntent.FLASHLIGHT_ON, parsed[0].matchResult.intent)
        assertEquals(NovaIntent.VOLUME_INCREASE, parsed[1].matchResult.intent)
    }

    @Test
    fun testWakeWordPrefixCompoundCommand() {
        val parsed = parser.parse("Hey Nova, turn off the torch and open whatsapp")
        assertEquals(2, parsed.size)
        assertEquals(NovaIntent.FLASHLIGHT_OFF, parsed[0].matchResult.intent)
        assertEquals(NovaIntent.WHATSAPP_OPEN, parsed[1].matchResult.intent)
    }
}
