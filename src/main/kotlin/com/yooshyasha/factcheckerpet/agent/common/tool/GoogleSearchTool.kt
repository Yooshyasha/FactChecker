package com.yooshyasha.factcheckerpet.agent.common.tool

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.ToolDescriptor
import com.yooshyasha.factcheckerpet.dto.GoogleSearchDTO
import com.yooshyasha.factcheckerpet.services.GoogleApiService
import kotlinx.serialization.KSerializer
import org.springframework.stereotype.Component


@Component
class GoogleSearchTool(
    private val googleApiService: GoogleApiService,
) : SimpleTool<GoogleSearchDTO>() {

    override val descriptor: ToolDescriptor
        get() = ToolDescriptor(
            name = "googleSearchTool",
            description = "Tool for search text with google",
        )

    override val argsSerializer: KSerializer<GoogleSearchDTO>
        get() = GoogleSearchDTO.serializer()

    override suspend fun doExecute(args: GoogleSearchDTO): String {
        return googleApiService.search(args.query)
    }
}