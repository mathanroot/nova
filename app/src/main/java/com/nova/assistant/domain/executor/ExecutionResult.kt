package com.nova.assistant.domain.executor

import com.nova.assistant.domain.intent.NovaIntent

data class ExecutionResult(
    val intent: NovaIntent,
    val isSuccess: Boolean,
    val spokenResponse: String,
    val detailText: String? = null
)
