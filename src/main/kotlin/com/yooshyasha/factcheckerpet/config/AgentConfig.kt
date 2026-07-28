package com.yooshyasha.factcheckerpet.config

import ai.koog.agents.mcp.McpToolRegistryProvider
import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import io.modelcontextprotocol.kotlin.sdk.client.SseClientTransport
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AgentConfig(
    @param:Value("\${agents.api.key}") private val agentsApiKey: String
) {
    // я бы сделал нормальный резолвер, но нет я тороплюсь
    @Bean
    fun model() = LLModel(
        provider = LLMProvider.Anthropic,
        id = "claude-3-5-haiku",
        capabilities = listOf(
            LLMCapability.Temperature,
            LLMCapability.Tools,
            LLMCapability.ToolChoice,
            LLMCapability.Vision.Image,
            LLMCapability.Schema.JSON.Full,
            LLMCapability.Completion
        )
    )

    // todo(обновить koog на 1.0 для мультипровайдерности)
    @Bean
    fun executor() = simpleAnthropicExecutor(agentsApiKey)

    @Bean
    fun searchMcpTransport() = McpToolRegistryProvider.defaultSseTransport("http://web-search-mcp:8000")

    @Bean
    fun searchMcpToolRegistry(searchMcpTransport: SseClientTransport) = runBlocking {
        McpToolRegistryProvider.fromTransport(searchMcpTransport)
    }
}