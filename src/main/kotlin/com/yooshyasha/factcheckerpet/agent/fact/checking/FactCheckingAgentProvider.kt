package com.yooshyasha.factcheckerpet.agent.fact.checking

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.*
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLModel
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.yooshyasha.factcheckerpet.agent.common.AgentProvider
import com.yooshyasha.factcheckerpet.dto.FactCheckResult
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class FactCheckingAgentProvider(
    private val executor: SingleLLMPromptExecutor,
    private val model: LLModel,
    private val searchMcpToolRegistry: ToolRegistry,
) : AgentProvider<FactCheckResult> {
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
        val toolRegistry = ToolRegistry {
            tool(FactCheckingTools.CheckOriginTool())
        } + searchMcpToolRegistry

        val strategy = strategy<String, FactCheckResult>(title) {
            val nodeInitialRequest by nodeLLMRequest()
            val nodeRunTool by nodeExecuteTool()
            val nodeSendToolResult by nodeLLMSendToolResult()

            val nodeFinalAnalytic by node<String, FactCheckResult> { content ->
                parseResult(content) ?: FactCheckResult(false, content, listOf())
            }

            edge(nodeStart forwardTo nodeInitialRequest)

            edge(nodeInitialRequest forwardTo nodeRunTool onToolCall { true })
            edge(nodeInitialRequest forwardTo nodeFinalAnalytic onAssistantMessage { true })

            edge(nodeRunTool forwardTo nodeSendToolResult)

            edge(nodeSendToolResult forwardTo nodeRunTool onToolCall { true })
            edge(nodeSendToolResult forwardTo nodeFinalAnalytic onAssistantMessage { true })

            edge(nodeFinalAnalytic forwardTo nodeFinish)
        }

        val agentConfig = AIAgentConfig(
            prompt = prompt("fact-checker") {
                system(
                    "Ты агент для проверки фактов новостей. Используй web_search (в аргументах ты должен " +
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
                            "(о котором говорилось ранее)." +
                            "Ты можешь вызвать инструменты максимум 6 раз"
                )
            },
            model = model,
            maxAgentIterations = 24,
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

    private fun parseResult(content: String): FactCheckResult? {
        val json = extractJson(content) ?: return null
        return try {
            objectMapper.readValue(json, FactCheckResult::class.java)
        } catch (_: IOException) {
            null
        }
    }

    private fun extractJson(content: String): String? {
        FENCED_JSON.find(content)?.let { return it.groupValues[1] }
        val start = content.indexOf('{')
        val end = content.lastIndexOf('}')
        return if (start != -1 && end > start) content.substring(start, end + 1) else null
    }

    private companion object {
        private val FENCED_JSON =
            Regex("```(?:json)?\\s*(\\{.*})\\s*```", RegexOption.DOT_MATCHES_ALL)
    }
}