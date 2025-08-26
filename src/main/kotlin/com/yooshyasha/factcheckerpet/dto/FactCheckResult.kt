package com.yooshyasha.factcheckerpet.dto

data class FactCheckResult(
    val isReliable: Boolean,
    val explanation: String,
    val sources: Collection<String>,
)
