package com.yooshyasha.factcheckerpet.agent.fact.checking

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.*
import ai.koog.agents.core.environment.ReceivedToolResult
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import com.yooshyasha.factcheckerpet.agent.common.AgentProvider
import com.yooshyasha.factcheckerpet.agent.common.tool.GoogleSearchTool
import com.yooshyasha.factcheckerpet.dto.FactCheckResult
import org.springframework.stereotype.Component

@Component
class FactCheckingAgent(
    private val googleSearchTool: GoogleSearchTool,
) : AgentProvider {
    override val title: String
        get() = "Fact checking agent"
    override val description: String
        get() = "I'm a fact checking agent"

    override suspend fun provideAgent(
        onToolCallEvent: suspend (String) -> Unit,
        onErrorEvent: suspend (String) -> Unit,
        onAssistantMessage: suspend (String) -> String
    ): AIAgent<String, String> {
        val executor = simpleOpenAIExecutor("")  // TODO

        val toolRegistry = ToolRegistry {
            tool(FactCheckingTools.CheckOriginTool())
            tool(googleSearchTool)
        }

        val strategy = strategy<String, FactCheckResult>(title) {
            val nodeInitialRequest by nodeLLMRequest()
            val nodeExecuteSearch by nodeExecuteTool()
            val nodeCompressHistory by nodeLLMCompressHistory<ReceivedToolResult>()
            val nodeSendSearchResult by nodeLLMSendToolResult()
            val nodeExecuteCheckOrigin by nodeExecuteTool()
            val nodeSendCheckOriginResult by nodeLLMSendToolResult()

            val nodeFinish by node<ReceivedToolResult, FactCheckResult> {
                FactCheckResult(
                    false,
                    "Thinking...",
                    listOf(),
                )
            }

            edge(nodeStart forwardTo nodeInitialRequest)

            edge(nodeInitialRequest forwardTo nodeExecuteSearch onToolCall { true })

            edge(nodeExecuteSearch forwardTo nodeCompressHistory)

            edge(nodeCompressHistory forwardTo nodeSendSearchResult)

            edge(nodeSendSearchResult forwardTo nodeExecuteCheckOrigin onToolCall { true })

            edge(nodeExecuteCheckOrigin forwardTo nodeSendCheckOriginResult)
        }
        TODO()
    }
}