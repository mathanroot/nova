package com.nova.assistant.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nova.assistant.data.models.ConversationItem
import com.nova.assistant.data.repository.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    val conversations: StateFlow<List<ConversationItem>> = conversationRepository.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun clearHistory() {
        viewModelScope.launch {
            conversationRepository.clearHistory()
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            conversationRepository.deleteConversation(id)
        }
    }
}
