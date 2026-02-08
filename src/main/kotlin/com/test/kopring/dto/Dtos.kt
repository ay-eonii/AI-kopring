package com.test.kopring.dto

import com.test.kopring.domain.entity.FeedbackStatus
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

// Auth DTOs
data class SignUpRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank @field:Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.") val password: String,
    @field:NotBlank val name: String
)

data class LoginRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val password: String
)

data class AuthResponse(
    val token: String,
    val email: String,
    val name: String,
    val role: String
)

// Chat DTOs
data class ChatRequest(
    @field:NotBlank val question: String,
    val isStreaming: Boolean? = false,
    val model: String? = null
)

data class ChatResponse(
    val chatId: Long,
    val question: String,
    val answer: String,
    val threadId: Long,
    val createdAt: Instant
)

data class ThreadResponse(
    val threadId: Long,
    val userId: Long,
    val createdAt: Instant,
    val lastChatAt: Instant,
    val chats: List<ChatInThreadResponse>
)

data class ChatInThreadResponse(
    val chatId: Long,
    val question: String,
    val answer: String,
    val createdAt: Instant
)

data class ChatListResponse(
    val threads: List<ThreadResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int
)

// Feedback DTOs
data class FeedbackRequest(
    @field:NotNull val chatId: Long,
    @field:NotNull val isPositive: Boolean
)

data class FeedbackResponse(
    val feedbackId: Long,
    val userId: Long,
    val chatId: Long,
    val isPositive: Boolean,
    val status: FeedbackStatus,
    val createdAt: Instant
)

data class FeedbackListResponse(
    val feedbacks: List<FeedbackResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int
)

data class FeedbackStatusUpdateRequest(
    @field:NotNull val status: FeedbackStatus
)

// Analytics DTOs
data class UserActivityResponse(
    val signUpCount: Long,
    val loginCount: Long,
    val chatCount: Long,
    val period: String
)

// Error Response
data class ErrorResponse(
    val message: String,
    val timestamp: Instant = Instant.now()
)

