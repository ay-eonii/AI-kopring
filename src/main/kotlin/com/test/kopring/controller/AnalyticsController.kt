package com.test.kopring.controller

import com.test.kopring.domain.entity.User
import com.test.kopring.dto.UserActivityResponse
import com.test.kopring.service.AnalyticsService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/analytics")
class AnalyticsController(
    private val analyticsService: AnalyticsService
) {

    @GetMapping("/activity")
    fun getUserActivity(
        @AuthenticationPrincipal user: User
    ): ResponseEntity<UserActivityResponse> {
        val response = analyticsService.getUserActivity(user)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/report")
    fun generateReport(
        @AuthenticationPrincipal user: User
    ): ResponseEntity<String> {
        val csvContent = analyticsService.generateReport(user)

        val headers = HttpHeaders()
        headers.contentType = MediaType.parseMediaType("text/csv")
        headers.setContentDispositionFormData("attachment", "chat_report_${Instant.now()}.csv")

        return ResponseEntity.ok()
            .headers(headers)
            .body(csvContent)
    }
}

