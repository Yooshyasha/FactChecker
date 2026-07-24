package com.yooshyasha.factcheckerpet.config

import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AgentConfig(
    @param:Value("\${agents.api.key}") private val agentsApiKey: String
) {
    // я бы сделал нормальный резолвер, но нет я тороплюсь
    @Bean
    fun model() = AnthropicModels.Sonnet_4

    // todo(обновить koog на 1.0 для мультипровайдерности)
    @Bean
    fun executor() = simpleAnthropicExecutor(agentsApiKey)
}