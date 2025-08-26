package com.yooshyasha.factcheckerpet.agent.fact.checking

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.*
import ai.koog.agents.core.environment.ReceivedToolResult
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import com.yooshyasha.factcheckerpet.agent.common.AgentProvider
import com.yooshyasha.factcheckerpet.agent.common.tool.GoogleSearchTool
import com.yooshyasha.factcheckerpet.dto.FactCheckResult
import org.springframework.stereotype.Component

@Component
class FactCheckingAgent(
    private val googleSearchTool: GoogleSearchTool,
) : AgentProvider<FactCheckResult> {
    override val title: String
        get() = "factCheckingAgent"
    override val description: String
        get() = "I'm a fact checking agent"

    override suspend fun provideAgent(
        onToolCallEvent: suspend (String) -> Unit,
        onErrorEvent: suspend (String) -> Unit,
        onAssistantMessage: suspend (String) -> String
    ): AIAgent<String, FactCheckResult> {
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

            val nodeFinalAnalytic by node<String, FactCheckResult> {
                FactCheckResult(
                    false,
                    "Thinking...",
                    listOf(),
                )
            }

            edge(nodeStart forwardTo nodeInitialRequest)

            edge(nodeInitialRequest forwardTo nodeExecuteSearch onToolCall {
                it.tool == "googleSearchTool"
            })

            edge(nodeExecuteSearch forwardTo nodeCompressHistory)

            edge(nodeCompressHistory forwardTo nodeSendSearchResult)

            edge(nodeSendSearchResult forwardTo nodeExecuteCheckOrigin onToolCall {
                it.tool == "checkOriginTool"
            })

            edge(nodeExecuteCheckOrigin forwardTo nodeSendCheckOriginResult)

            edge(nodeSendCheckOriginResult forwardTo nodeFinalAnalytic onAssistantMessage { message ->
                !message.content.contains("нужно найти") &&
                        !message.content.contains("поиск") &&
                        !message.content.contains("проверить")
            })

            edge(nodeSendSearchResult forwardTo nodeExecuteCheckOrigin onToolCall {
                it.tool == "googleSearchTool"
            })

            edge(nodeFinalAnalytic forwardTo nodeFinish)
        }

        val agentConfig = AIAgentConfig(
            prompt = prompt("fact-checker") {
                system(
                    "Ты агент для проверки фактов новостей. Используй googleSearchTool для поиска информации " +
                            "и checkOriginTool для проверки источников. Ты можешь игнорировать checkOriginTool, если " +
                            "источники на 100% независимые. Повторяй запросы при необходимости. На основе найденной " +
                            "информации формируй FactCheckResult с полями: isTrue (правда ли утверждение), comment " +
                            "(объяснение результата), sources (список источников)."
                )
            },
            model = OpenAIModels.Chat.GPT4o,
            maxAgentIterations = 50,
        )

        return AIAgent(
            promptExecutor = executor,
            strategy = strategy,
            agentConfig = agentConfig,
            toolRegistry = toolRegistry
        ) {
            handleEvents {
                onAgentRunError { onErrorEvent("${it.throwable.message}") }
            }
        }
    }
}