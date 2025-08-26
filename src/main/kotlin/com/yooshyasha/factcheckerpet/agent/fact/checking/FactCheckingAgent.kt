package com.yooshyasha.factcheckerpet.agent.fact.checking

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteMultipleTools
import ai.koog.agents.core.dsl.extension.nodeExecuteTool
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import com.yooshyasha.factcheckerpet.agent.common.AgentProvider

class FactCheckingAgent : AgentProvider {
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
            // TODO("Google search tool")
        }

        val strategy = strategy<String, String>(title) {
            val executeTool = nodeExecuteMultipleTools()

            // TODO
        }
        TODO()
    }
}