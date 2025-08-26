package com.yooshyasha.factcheckerpet.dto

import ai.koog.agents.core.tools.ToolArgs
import kotlinx.serialization.Serializable

@Serializable
data class GoogleSearchDTO(
    val query: String,
) : ToolArgs
