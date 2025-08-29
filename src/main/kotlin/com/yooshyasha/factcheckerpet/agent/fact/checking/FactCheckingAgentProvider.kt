package com.yooshyasha.factcheckerpet.agent.fact.checking

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.*
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.yooshyasha.factcheckerpet.agent.common.AgentProvider
import com.yooshyasha.factcheckerpet.agent.common.tool.GoogleSearchTool
import com.yooshyasha.factcheckerpet.dto.FactCheckResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class FactCheckingAgentProvider(
    private val googleSearchTool: GoogleSearchTool,
) : AgentProvider<FactCheckResult> {
    @Value("\${agents.api.key}")
    private lateinit var agentsApiKey: String

    override val title: String
        get() = "factCheckingAgent"
    override val description: String
        get() = "I'm a fact checking agent"

    private final val objectMapper = jacksonObjectMapper()

    override suspend fun provideAgent(
        onToolCallEvent: suspend (String) -> Unit,
        onErrorEvent: suspend (String) -> Unit,
        onAssistantMessage: suspend (String) -> String
    ): AIAgent<String, FactCheckResult> {
        val executor = simpleOpenAIExecutor(agentsApiKey)

        val toolRegistry = ToolRegistry {
            tool(FactCheckingTools.CheckOriginTool())
            tool(googleSearchTool)
        }

        val strategy = strategy<String, FactCheckResult>(title) {
            val nodeInitialRequest by nodeLLMRequest()
            val nodeExecuteSearch by nodeExecuteTool()
            val nodeSendSearchResult by nodeLLMSendToolResult()
            val nodeExecuteCheckOrigin by nodeExecuteTool()
            val nodeSendCheckOriginResult by nodeLLMSendToolResult()

            val nodeFinalAnalytic by node<String, FactCheckResult> {
                try {
                    objectMapper.reader().readValue(it, FactCheckResult::class.java)
                } catch (e: IOException) {
                    FactCheckResult(false, it, listOf())
                }
            }

            edge(nodeStart forwardTo nodeInitialRequest)

            edge(nodeInitialRequest forwardTo nodeExecuteSearch onToolCall {
                it.tool == "googleSearchTool"
            })

            edge(nodeExecuteSearch forwardTo nodeSendSearchResult)

            edge(nodeSendSearchResult forwardTo nodeExecuteCheckOrigin onToolCall {
                it.tool == "checkOriginTool"
            })

            edge(nodeExecuteCheckOrigin forwardTo nodeSendCheckOriginResult)

            edge(nodeSendCheckOriginResult forwardTo nodeFinalAnalytic onAssistantMessage { message ->
                !message.content.contains("нужно найти") &&
                        !message.content.contains("поиск") &&
                        !message.content.contains("проверить")
            })

            edge(nodeSendSearchResult forwardTo nodeExecuteSearch onToolCall {
                it.tool == "googleSearchTool"
            })

            edge(nodeSendSearchResult forwardTo nodeFinalAnalytic onAssistantMessage { message ->
                val json = try {
                    objectMapper.readTree(message.content)
                } catch (e: Exception) {
                    null
                }
                json != null && json.hasNonNull("isReliable") && json.hasNonNull("explanation")
            })

            edge(nodeSendSearchResult forwardTo nodeExecuteSearch onToolCall {
                it.tool == "googleSearchTool"
            })

            edge(nodeFinalAnalytic forwardTo nodeFinish)

            edge(nodeInitialRequest forwardTo nodeFinalAnalytic onAssistantMessage { true })
        }

        val agentConfig = AIAgentConfig(
            prompt = prompt("fact-checker") {
                system(
                    "Ты агент для проверки фактов новостей. Используй googleSearchTool (в аргументах ты должен " +
                            "передать query) для поиска информации " +
                            "и checkOriginTool (в аргументах ты должен передать origin) для проверки источников. " +
                            "Ты можешь игнорировать checkOriginTool, если " +
                            "источники на 100% независимые; Ты можешь, если у независимых найти не удалось, использовать " +
                            "новости от иных, но при этом помечая факт чекинг провальным, со ссылкой на найденные " +
                            "результаты от зависимых СМИ. Повторяй запросы при необходимости. На основе найденной " +
                            "информации формируй json с полями: isReliable (правда ли утверждение), explanation " +
                            "(объяснение результата и, если пользователю может быть интересно, причины этого), sources " +
                            "(Collection<String>) (список источников)." +
                            "Если ты готов вернуть результат, ты должен прислать валидный json без символов снаружи его " +
                            "(о котором говорилось ранее)."
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