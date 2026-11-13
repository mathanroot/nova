package com.nova.assistant

import com.nova.assistant.speech.wakeword.WakeWordEngine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WakeWordEngineTest {

    private lateinit var wakeWordEngine: WakeWordEngine

    @Before
    fun setUp() {
        wakeWordEngine = WakeWordEngine()
    }

    @Test
    fun testWakeWordPhrases() {
        assertTrue(wakeWordEngine.processRecognizedText("hey nova"))
        assertTrue(wakeWordEngine.processRecognizedText("ok nova how are you"))
        assertTrue(wakeWordEngine.processRecognizedText("nova turn on the torch"))
        assertTrue(wakeWordEngine.processRecognizedText("hi nova"))

        assertFalse(wakeWordEngine.processRecognizedText("turn on flashlight"))
        assertFalse(wakeWordEngine.processRecognizedText("hello world"))
    }
}
