package com.nova.assistant.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ConversationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NovaDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
}
