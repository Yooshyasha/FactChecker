package com.yooshyasha.factcheckerpet.agent.fact.checking

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.ToolDescriptor
import com.yooshyasha.factcheckerpet.dto.News
import kotlinx.serialization.KSerializer

object FactCheckingTools {
    class CheckOriginTool(
        override val argsSerializer: KSerializer<News>,
        override val descriptor: ToolDescriptor
    ) : SimpleTool<News>() {
        override suspend fun doExecute(args: News): String {
            val trustedSources = listOf("BBC", "Reuters", "The Guardian")
            return (args.origin in trustedSources).toString()
        }
    }
}