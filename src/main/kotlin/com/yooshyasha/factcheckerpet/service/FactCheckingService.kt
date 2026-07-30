package com.yooshyasha.factcheckerpet.service

import com.yooshyasha.factcheckerpet.agent.fact.checking.FactCheckingAgentProvider
import com.yooshyasha.factcheckerpet.dto.FactCheckResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class FactCheckingService(
    private val factCheckingAgentProvider: FactCheckingAgentProvider,
) {
    private final val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun factCheckNews(news: String, context: String): FactCheckResult {
        val agent = factCheckingAgentProvider.provideAgent({}, {
            logger.error("Агент вернул ошибку: $it")
        }, {
            logger.debug("Агент написал: $it")
            ""
        })

        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        val input = "$news\nCONTEXT: $context; \nCURRENT_TIME: $now"

        return agent.run(input)
    }
}