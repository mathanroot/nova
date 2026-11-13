package com.nova.assistant.domain.parser

import com.nova.assistant.domain.context.ConversationContext
import com.nova.assistant.domain.intent.IntentMatchResult
import com.nova.assistant.domain.intent.NovaIntent
import com.nova.assistant.domain.matcher.CommandMatcher
import javax.inject.Inject
import javax.inject.Singleton

data class ParsedCommand(
    val rawText: String,
    val matchResult: IntentMatchResult
)

@Singleton
class CommandParser @Inject constructor(
    private val conversationContext: ConversationContext
) {

    fun parse(rawInput: String): List<ParsedCommand> {
        val cleaned = rawInput.trim()
        if (cleaned.isEmpty()) return emptyList()

        // Check if this is a follow-up query based on prior conversation context
        val contextualIntent = conversationContext.resolveContextualIntent(cleaned)
        if (contextualIntent != null) {
            return listOf(
                ParsedCommand(
                    rawText = cleaned,
                    matchResult = IntentMatchResult(
                        intent = contextualIntent,
                        confidence = 0.95f,
                        matchedPhrase = cleaned
                    )
                )
            )
        }

        // Split compound sentences by " and ", " then ", or semicolons/commas when combining commands
        val segments = splitCompoundCommands(cleaned)
        val parsedList = mutableListOf<ParsedCommand>()

        for (segment in segments) {
            val trimmedSegment = segment.trim()
            if (trimmedSegment.isNotEmpty()) {
                val match = CommandMatcher.match(trimmedSegment)
                parsedList.add(ParsedCommand(rawText = trimmedSegment, matchResult = match))
            }
        }

        return parsedList
    }

    private fun splitCompoundCommands(input: String): List<String> {
        var normalized = input
        // Remove wake words upfront if present
        normalized = normalized.replace(Regex("^(hey|ok|okay|hi|hello)?\\s*nova[,\\s]*", RegexOption.IGNORE_CASE), "")

        // Split on coordinating conjunctions where each part is a command
        val parts = normalized.split(Regex("\\s+(?:and|then)\\s+|[,;]\\s+"))
        if (parts.size <= 1) {
            return listOf(normalized)
        }

        // Validate that parts look like independent commands
        val result = mutableListOf<String>()
        for (part in parts) {
            val p = part.trim()
            if (p.isNotEmpty()) {
                result.add(p)
            }
        }
        return if (result.isEmpty()) listOf(normalized) else result
    }
}
