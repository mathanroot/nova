package com.nova.assistant.di

import android.content.Context
import androidx.room.Room
import com.nova.assistant.data.database.ConversationDao
import com.nova.assistant.data.database.NovaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNovaDatabase(
        @ApplicationContext context: Context
    ): NovaDatabase {
        return Room.databaseBuilder(
            context,
            NovaDatabase::class.java,
            "nova_assistant.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideConversationDao(database: NovaDatabase): ConversationDao {
        return database.conversationDao()
    }
}
