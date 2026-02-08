package com.test.kopring.controller

import com.test.kopring.domain.entity.User
import com.test.kopring.dto.*
import com.test.kopring.service.FeedbackService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/feedbacks")
class FeedbackController(
    private val feedbackService: FeedbackService
) {

    @PostMapping
    fun createFeedback(
        @AuthenticationPrincipal user: User,
        @Valid @RequestBody request: FeedbackRequest
    ): ResponseEntity<FeedbackResponse> {
        val response = feedbackService.createFeedback(user, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getFeedbackList(
        @AuthenticationPrincipal user: User,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "desc") sort: String,
        @RequestParam(required = false) isPositive: Boolean?
    ): ResponseEntity<FeedbackListResponse> {
        val response = feedbackService.getFeedbackList(user, page, size, sort, isPositive)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{feedbackId}/status")
    fun updateFeedbackStatus(
        @AuthenticationPrincipal user: User,
        @PathVariable feedbackId: Long,
        @Valid @RequestBody request: FeedbackStatusUpdateRequest
    ): ResponseEntity<FeedbackResponse> {
        val response = feedbackService.updateFeedbackStatus(user, feedbackId, request)
        return ResponseEntity.ok(response)
    }
}

