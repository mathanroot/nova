package com.nova.assistant

import com.nova.assistant.domain.context.ConversationContext
import com.nova.assistant.domain.intent.NovaIntent
import com.nova.assistant.domain.parser.CommandParser
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ConversationContextTest {

    private lateinit var conversationContext: ConversationContext
    private lateinit var commandParser: CommandParser

    @Before
    fun setUp() {
        conversationContext = ConversationContext()
        commandParser = CommandParser(conversationContext)
    }

    @Test
    fun testContextualStorageFollowUp() {
        // Step 1: User asks battery
        val firstTurn = commandParser.parse("what is my battery percentage")
        assertEquals(NovaIntent.BATTERY_CHECK, firstTurn[0].matchResult.intent)

        conversationContext.updateContext(
            intent = NovaIntent.BATTERY_CHECK,
            query = "what is my battery percentage",
            response = "Your battery is at 78 percent."
        )

        // Step 2: User asks "what about my storage?"
        val followUp = commandParser.parse("what about my storage?")
        assertEquals(1, followUp.size)
        assertEquals(NovaIntent.STORAGE_CHECK, followUp[0].matchResult.intent)
    }

    @Test
    fun testContextualToggleFollowUp() {
        conversationContext.updateContext(
            intent = NovaIntent.FLASHLIGHT_ON,
            query = "turn on flashlight",
            response = "Flashlight turned on."
        )

        val followUp = commandParser.parse("turn it off")
        assertEquals(NovaIntent.FLASHLIGHT_OFF, followUp[0].matchResult.intent)
    }
}
