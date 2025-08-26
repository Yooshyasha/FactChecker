package com.yooshyasha.factcheckerpet.dto

data class FactCheckResult(
    var isReliable: Boolean,
    var explanation: String,
    val sources: Collection<String>,
)
