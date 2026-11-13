package com.nova.assistant.data.repository

import com.nova.assistant.data.database.ConversationDao
import com.nova.assistant.data.database.ConversationEntity
import com.nova.assistant.data.models.ConversationItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepository @Inject constructor(
    private val conversationDao: ConversationDao
) {
    fun getAllConversations(): Flow<List<ConversationItem>> {
        return conversationDao.getAllConversations().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getRecentConversations(limit: Int = 10): List<ConversationItem> {
        return conversationDao.getRecentConversations(limit).map { it.toDomain() }
    }

    suspend fun recordConversation(
        userCommand: String,
        assistantResponse: String,
        isSuccess: Boolean = true,
        intentId: String = "UNKNOWN"
    ): Long {
        val entity = ConversationEntity(
            timestamp = System.currentTimeMillis(),
            userCommand = userCommand.trim(),
            assistantResponse = assistantResponse.trim(),
            isSuccess = isSuccess,
            intentId = intentId
        )
        return conversationDao.insertConversation(entity)
    }

    suspend fun clearHistory() {
        conversationDao.clearAllConversations()
    }

    suspend fun deleteConversation(id: Long) {
        conversationDao.deleteConversationById(id)
    }
}
