package com.test.kopring.service

import com.test.kopring.domain.entity.FeedbackStatus
import com.test.kopring.domain.entity.User
import com.test.kopring.domain.entity.UserRole
import com.test.kopring.domain.entity.Feedback
import com.test.kopring.domain.repository.ChatRepository
import com.test.kopring.domain.repository.FeedbackRepository
import com.test.kopring.dto.FeedbackListResponse
import com.test.kopring.dto.FeedbackRequest
import com.test.kopring.dto.FeedbackResponse
import com.test.kopring.dto.FeedbackStatusUpdateRequest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
    private val chatRepository: ChatRepository
) {

    fun createFeedback(user: User, request: FeedbackRequest): FeedbackResponse {
        val chat = chatRepository.findById(request.chatId)
            .orElseThrow { IllegalArgumentException("대화를 찾을 수 없습니다.") }

        // Check if user can create feedback for this chat
        if (user.role != UserRole.ADMIN && chat.thread.user.id != user.id) {
            throw IllegalArgumentException("자신이 생성한 대화에만 피드백을 생성할 수 있습니다.")
        }

        // Check if feedback already exists for this user and chat
        if (feedbackRepository.existsByUserAndChat(user, chat)) {
            throw IllegalArgumentException("이미 해당 대화에 피드백을 생성했습니다.")
        }

        val feedback = Feedback(
            user = user,
            chat = chat,
            isPositive = request.isPositive
        )

        val savedFeedback = feedbackRepository.save(feedback)

        return toFeedbackResponse(savedFeedback)
    }

    @Transactional(readOnly = true)
    fun getFeedbackList(
        user: User,
        page: Int,
        size: Int,
        sortDirection: String,
        isPositive: Boolean?
    ): FeedbackListResponse {
        val sort = if (sortDirection.equals("asc", ignoreCase = true)) {
            Sort.by("createdAt").ascending()
        } else {
            Sort.by("createdAt").descending()
        }

        val pageable = PageRequest.of(page, size, sort)

        val feedbackPage = when {
            user.role == UserRole.ADMIN && isPositive != null -> {
                feedbackRepository.findByIsPositive(isPositive, pageable)
            }
            user.role == UserRole.ADMIN -> {
                feedbackRepository.findAllFeedbacks(pageable)
            }
            isPositive != null -> {
                feedbackRepository.findByUserAndIsPositive(user, isPositive, pageable)
            }
            else -> {
                feedbackRepository.findByUser(user, pageable)
            }
        }

        return FeedbackListResponse(
            feedbacks = feedbackPage.content.map { toFeedbackResponse(it) },
            totalElements = feedbackPage.totalElements,
            totalPages = feedbackPage.totalPages,
            currentPage = page
        )
    }

    fun updateFeedbackStatus(user: User, feedbackId: Long, request: FeedbackStatusUpdateRequest): FeedbackResponse {
        if (user.role != UserRole.ADMIN) {
            throw IllegalArgumentException("관리자만 피드백 상태를 변경할 수 있습니다.")
        }

        val feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow { IllegalArgumentException("피드백을 찾을 수 없습니다.") }

        feedback.status = request.status
        val updatedFeedback = feedbackRepository.save(feedback)

        return toFeedbackResponse(updatedFeedback)
    }

    private fun toFeedbackResponse(feedback: Feedback): FeedbackResponse {
        return FeedbackResponse(
            feedbackId = feedback.id!!,
            userId = feedback.user.id!!,
            chatId = feedback.chat.id!!,
            isPositive = feedback.isPositive,
            status = feedback.status,
            createdAt = feedback.createdAt
        )
    }
}

