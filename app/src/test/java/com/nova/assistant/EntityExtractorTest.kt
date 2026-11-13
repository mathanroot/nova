package com.nova.assistant

import com.nova.assistant.domain.entity.EntityExtractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EntityExtractorTest {

    @Test
    fun testPercentageExtraction() {
        assertEquals(50, EntityExtractor.extractPercentage("set volume to 50 percent"))
        assertEquals(80, EntityExtractor.extractPercentage("brightness 80%"))
        assertEquals(75, EntityExtractor.extractPercentage("set volume to 75"))
    }

    @Test
    fun testAppNameExtraction() {
        assertEquals("whatsapp", EntityExtractor.extractAppName("open whatsapp"))
        assertEquals("youtube", EntityExtractor.extractAppName("launch youtube"))
        assertEquals("spotify", EntityExtractor.extractAppName("start spotify"))
    }

    @Test
    fun testTimerSecondsExtraction() {
        assertEquals(300, EntityExtractor.extractTimerSeconds("set a timer for 5 minutes"))
        assertEquals(45, EntityExtractor.extractTimerSeconds("timer for 45 seconds"))
        assertEquals(3600, EntityExtractor.extractTimerSeconds("set timer for 1 hour"))
    }

    @Test
    fun testMathExtraction() {
        val mult = EntityExtractor.extractMath("calculate 250 multiplied by 5")
        assertNotNull(mult)
        assertEquals(1250.0, mult!!.second, 0.001)

        val pct = EntityExtractor.extractMath("what is 20 percent of 500")
        assertNotNull(pct)
        assertEquals(100.0, pct!!.second, 0.001)

        val add = EntityExtractor.extractMath("what is 45 plus 15")
        assertNotNull(add)
        assertEquals(60.0, add!!.second, 0.001)
    }

    @Test
    fun testUnitConversion() {
        val kmToM = EntityExtractor.extractUnitConversion("convert 5 kilometers to meters")
        assertNotNull(kmToM)
        assertEquals(5000.0, kmToM!!.convertedValue!!, 0.001)

        val fToC = EntityExtractor.extractUnitConversion("convert 100 fahrenheit to celsius")
        assertNotNull(fToC)
        assertEquals(37.777, fToC!!.convertedValue!!, 0.01)
    }
}
