package com.nova.assistant.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nova.assistant.data.models.ConversationItem

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userCommand: String,
    val assistantResponse: String,
    val isSuccess: Boolean = true,
    val intentId: String = "UNKNOWN"
) {
    fun toDomain(): ConversationItem {
        return ConversationItem(
            id = id,
            timestamp = timestamp,
            userCommand = userCommand,
            assistantResponse = assistantResponse,
            isSuccess = isSuccess,
            intentId = intentId
        )
    }

    companion object {
        fun fromDomain(item: ConversationItem): ConversationEntity {
            return ConversationEntity(
                id = item.id,
                timestamp = item.timestamp,
                userCommand = item.userCommand,
                assistantResponse = item.assistantResponse,
                isSuccess = item.isSuccess,
                intentId = item.intentId
            )
        }
    }
}
