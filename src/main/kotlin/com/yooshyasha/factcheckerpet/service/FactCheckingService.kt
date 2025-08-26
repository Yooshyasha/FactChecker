package com.yooshyasha.factcheckerpet.service

import com.yooshyasha.factcheckerpet.agent.fact.checking.FactCheckingAgentProvider
import com.yooshyasha.factcheckerpet.dto.FactCheckResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FactCheckingService(
    private val factCheckingAgentProvider: FactCheckingAgentProvider,
) {
    private final val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun factCheckNews(news: String): FactCheckResult {
        val agent = factCheckingAgentProvider.provideAgent({}, {
            logger.error("Агент вернул ошибку: $it")
        }, { "" })

        return agent.run(news)
    }
}