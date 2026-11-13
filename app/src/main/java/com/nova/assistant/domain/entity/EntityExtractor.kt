package com.nova.assistant.domain.entity

import java.util.Locale
import java.util.regex.Pattern

object EntityExtractor {

    private val PERCENTAGE_REGEX = Pattern.compile("(\\d{1,3})\\s*(?:percent|%)", Pattern.CASE_INSENSITIVE)
    private val NUMBER_REGEX = Pattern.compile("\\b(\\d+(?:\\.\\d+)?)\\b")
    private val PHONE_NUMBER_REGEX = Pattern.compile("\\b(\\+?\\d[\\d\\s\\-]{6,14}\\d)\\b")

    // Contact extraction patterns
    private val CALL_CONTACT_REGEX = Pattern.compile("(?:call|dial)\\s+([a-zA-Z\\s]+)", Pattern.CASE_INSENSITIVE)
    private val SMS_REGEX = Pattern.compile("(?:send\\s+(?:a\\s+)?(?:message|sms|text)\\s+to|message)\\s+([a-zA-Z]+)(?:\\s+(?:saying|that)\\s+(.+))?", Pattern.CASE_INSENSITIVE)

    // Timer patterns
    private val TIMER_REGEX = Pattern.compile("(?:timer\\s+for|set\\s+a?\\s*timer\\s+for)\\s+(\\d+)\\s*(second|seconds|sec|minute|minutes|min|hour|hours|hr)", Pattern.CASE_INSENSITIVE)
    private val ALARM_REGEX = Pattern.compile("(?:alarm\\s+for|set\\s+a?\\s*alarm\\s+for)\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", Pattern.CASE_INSENSITIVE)

    // Math patterns
    private val MATH_PERCENT_OF_REGEX = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:percent|%)\\s+of\\s+(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE)
    private val MATH_BASIC_REGEX = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(plus|minus|multiplied by|times|multiplied|divided by|divided|\\+|\\-|\\*|\\/|x)\\s*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE)

    // Unit conversion patterns
    private val CONVERT_REGEX = Pattern.compile("convert\\s+(\\d+(?:\\.\\d+)?)\\s+([a-zA-Z]+)\\s+(?:to|in|into)\\s+([a-zA-Z]+)", Pattern.CASE_INSENSITIVE)

    fun extractPercentage(text: String): Int? {
        val matcher = PERCENTAGE_REGEX.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)?.toIntOrNull()?.coerceIn(0, 100)
        }
        // Fallback for "set volume to 50"
        val generalNum = NUMBER_REGEX.matcher(text)
        while (generalNum.find()) {
            val num = generalNum.group(1)?.toIntOrNull()
            if (num != null && num in 0..100) {
                return num
            }
        }
        return null
    }

    fun extractAppName(text: String): String? {
        val clean = text.lowercase(Locale.ROOT)
            .replace("open the ", "open ")
            .replace("launch the ", "launch ")
            .replace("start the ", "start ")

        val prefixes = listOf("open ", "launch ", "start ", "go to ")
        for (prefix in prefixes) {
            val idx = clean.indexOf(prefix)
            if (idx != -1) {
                val candidate = clean.substring(idx + prefix.length).trim()
                if (candidate.isNotEmpty()) {
                    return candidate
                }
            }
        }
        return null
    }

    fun extractContactAndMessage(text: String): Pair<String?, String?> {
        val smsMatcher = SMS_REGEX.matcher(text)
        if (smsMatcher.find()) {
            val contact = smsMatcher.group(1)?.trim()
            val message = smsMatcher.group(2)?.trim()
            return Pair(contact, message)
        }

        val callMatcher = CALL_CONTACT_REGEX.matcher(text)
        if (callMatcher.find()) {
            val contactOrNumber = callMatcher.group(1)?.trim()
            return Pair(contactOrNumber, null)
        }

        return Pair(null, null)
    }

    fun extractPhoneNumber(text: String): String? {
        val matcher = PHONE_NUMBER_REGEX.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)?.replace(" ", "")?.replace("-", "")
        }
        return null
    }

    fun extractTimerSeconds(text: String): Int? {
        val matcher = TIMER_REGEX.matcher(text)
        if (matcher.find()) {
            val amount = matcher.group(1)?.toIntOrNull() ?: return null
            val unit = matcher.group(2)?.lowercase(Locale.ROOT) ?: "minutes"
            return when {
                unit.startsWith("sec") -> amount
                unit.startsWith("min") -> amount * 60
                unit.startsWith("hour") || unit.startsWith("hr") -> amount * 3600
                else -> amount * 60
            }
        }
        return null
    }

    fun extractAlarmTime(text: String): Triple<Int, Int, Boolean?>? {
        val matcher = ALARM_REGEX.matcher(text)
        if (matcher.find()) {
            var hour = matcher.group(1)?.toIntOrNull() ?: return null
            val minute = matcher.group(2)?.toIntOrNull() ?: 0
            val amPm = matcher.group(3)?.lowercase(Locale.ROOT)
            val isPm = when (amPm) {
                "pm" -> true
                "am" -> false
                else -> null
            }

            if (isPm == true && hour < 12) hour += 12
            if (isPm == false && hour == 12) hour = 0

            return Triple(hour, minute, isPm)
        }
        return null
    }

    fun extractMath(text: String): Pair<String, Double>? {
        // Percentage calculation: e.g. "20 percent of 500"
        val percentMatcher = MATH_PERCENT_OF_REGEX.matcher(text)
        if (percentMatcher.find()) {
            val pct = percentMatcher.group(1)?.toDoubleOrNull() ?: return null
            val total = percentMatcher.group(2)?.toDoubleOrNull() ?: return null
            val result = (pct / 100.0) * total
            return Pair("$pct% of $total", result)
        }

        // Basic arithmetic
        val mathMatcher = MATH_BASIC_REGEX.matcher(text)
        if (mathMatcher.find()) {
            val a = mathMatcher.group(1)?.toDoubleOrNull() ?: return null
            val op = mathMatcher.group(2)?.lowercase(Locale.ROOT) ?: return null
            val b = mathMatcher.group(3)?.toDoubleOrNull() ?: return null

            val result = when (op) {
                "+", "plus" -> a + b
                "-", "minus" -> a - b
                "*", "x", "times", "multiplied by", "multiplied" -> a * b
                "/", "divided by", "divided" -> if (b != 0.0) a / b else Double.NaN
                else -> null
            }
            if (result != null) {
                return Pair("$a $op $b", result)
            }
        }
        return null
    }

    fun extractUnitConversion(text: String): ExtractedEntities? {
        val matcher = CONVERT_REGEX.matcher(text)
        if (matcher.find()) {
            val value = matcher.group(1)?.toDoubleOrNull() ?: return null
            val unitFrom = matcher.group(2)?.lowercase(Locale.ROOT) ?: return null
            val unitTo = matcher.group(3)?.lowercase(Locale.ROOT) ?: return null

            val converted = performConversion(value, unitFrom, unitTo)
            return ExtractedEntities(
                unitValue = value,
                unitFrom = unitFrom,
                unitTo = unitTo,
                convertedValue = converted
            )
        }
        return null
    }

    private fun performConversion(value: Double, from: String, to: String): Double? {
        val f = normalizeUnit(from)
        val t = normalizeUnit(to)

        // Length
        if (f == "kilometer" && t == "meter") return value * 1000.0
        if (f == "meter" && t == "kilometer") return value / 1000.0
        if (f == "mile" && t == "kilometer") return value * 1.60934
        if (f == "kilometer" && t == "mile") return value / 1.60934
        if (f == "foot" && t == "meter") return value * 0.3048
        if (f == "meter" && t == "foot") return value / 0.3048
        if (f == "inch" && t == "centimeter") return value * 2.54
        if (f == "centimeter" && t == "inch") return value / 2.54

        // Weight
        if (f == "kilogram" && t == "gram") return value * 1000.0
        if (f == "gram" && t == "kilogram") return value / 1000.0
        if (f == "pound" && t == "kilogram") return value * 0.453592
        if (f == "kilogram" && t == "pound") return value / 0.453592

        // Temperature
        if (f == "fahrenheit" && t == "celsius") return (value - 32.0) * 5.0 / 9.0
        if (f == "celsius" && t == "fahrenheit") return (value * 9.0 / 5.0) + 32.0

        return null
    }

    private fun normalizeUnit(unit: String): String {
        val u = unit.lowercase(Locale.ROOT)
        return when {
            u in listOf("km", "kilometer", "kilometers") -> "kilometer"
            u in listOf("m", "meter", "meters") -> "meter"
            u in listOf("cm", "centimeter", "centimeters") -> "centimeter"
            u in listOf("mi", "mile", "miles") -> "mile"
            u in listOf("ft", "foot", "feet") -> "foot"
            u in listOf("in", "inch", "inches") -> "inch"
            u in listOf("kg", "kilogram", "kilograms") -> "kilogram"
            u in listOf("g", "gram", "grams") -> "gram"
            u in listOf("lb", "lbs", "pound", "pounds") -> "pound"
            u in listOf("f", "fahrenheit") -> "fahrenheit"
            u in listOf("c", "celsius") -> "celsius"
            else -> u
        }
    }
}
