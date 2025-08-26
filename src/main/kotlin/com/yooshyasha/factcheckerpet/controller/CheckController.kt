package com.yooshyasha.factcheckerpet.controller

import com.yooshyasha.factcheckerpet.dto.FactCheckResult
import com.yooshyasha.factcheckerpet.dto.RequestFactCheck
import com.yooshyasha.factcheckerpet.service.FactCheckingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/fact")
class CheckController(
    private val factCheckingService: FactCheckingService,
) {
    @GetMapping("/check")
    suspend fun factCheck(@ModelAttribute factData: RequestFactCheck): ResponseEntity<FactCheckResult> {
        return ResponseEntity.ok(factCheckingService.factCheckNews(factData.news))
    }
}