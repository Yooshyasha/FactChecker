package com.yooshyasha.factcheckerpet.agent.common

import ai.koog.agents.core.agent.AIAgent

interface AgentProvider {
    val title: String

    val description: String

    suspend fun provideAgent(
        onToolCallEvent: suspend (String) -> Unit,
        onErrorEvent: suspend (String) -> Unit,
        onAssistantMessage: suspend (String) -> String
    ): AIAgent<String, String>
}