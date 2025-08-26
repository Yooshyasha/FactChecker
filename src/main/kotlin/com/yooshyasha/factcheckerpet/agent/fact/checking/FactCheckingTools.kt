package com.yooshyasha.factcheckerpet.agent.fact.checking

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.ToolDescriptor
import com.yooshyasha.factcheckerpet.dto.News
import kotlinx.serialization.KSerializer

object FactCheckingTools {
    class CheckOriginTool : SimpleTool<News>() {
        override val argsSerializer: KSerializer<News>
            get() = News.serializer()
        override val descriptor: ToolDescriptor
            get() = ToolDescriptor(
                name = "checkOriginTool",
                description = "Tool for checking news origin. Use the whitelist"
            )

        override suspend fun doExecute(args: News): String {
            val trustedSources = listOf("BBC", "Reuters", "The Guardian")
            return (args.origin in trustedSources).toString()
        }
    }
}