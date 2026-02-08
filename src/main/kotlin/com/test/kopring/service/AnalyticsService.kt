package com.test.kopring.service

import com.opencsv.CSVWriter
import com.test.kopring.domain.entity.User
import com.test.kopring.domain.entity.UserRole
import com.test.kopring.domain.repository.ChatRepository
import com.test.kopring.dto.UserActivityResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.StringWriter
import java.time.Instant
import java.time.temporal.ChronoUnit


@Service
@Transactional(readOnly = true)
class AnalyticsService(
    private val chatRepository: ChatRepository,
    private val activityService: ActivityService
) {

    fun getUserActivity(user: User): UserActivityResponse {
        if (user.role != UserRole.ADMIN) {
            throw IllegalArgumentException("관리자만 사용자 활동 기록을 조회할 수 있습니다.")
        }

        val dateKey = activityService.getCurrentDateKey()
        val signUpCount = activityService.getSignUpCount(dateKey)
        val loginCount = activityService.getLoginCount(dateKey)
        val chatCount = activityService.getChatCount(dateKey)

        return UserActivityResponse(
            signUpCount = signUpCount,
            loginCount = loginCount,
            chatCount = chatCount,
            period = "Last 24 hours"
        )
    }

    fun generateReport(user: User): String {
        if (user.role != UserRole.ADMIN) {
            throw IllegalArgumentException("관리자만 보고서를 생성할 수 있습니다.")
        }

        val now = Instant.now()
        val oneDayAgo = now.minus(1, ChronoUnit.DAYS)

        val chats = chatRepository.findAllByCreatedAtAfter(oneDayAgo)

        val writer = StringWriter()
        val csvWriter = CSVWriter(writer)

        // Write header
        csvWriter.writeNext(arrayOf("Chat ID", "User Email", "User Name", "Question", "Answer", "Created At"))

        // Write data
        chats.forEach { chat ->
            csvWriter.writeNext(
                arrayOf(
                    chat.id.toString(),
                    chat.thread.user.email,
                    chat.thread.user.name,
                    chat.question,
                    chat.answer,
                    chat.createdAt.toString()
                )
            )
        }

        csvWriter.close()

        return writer.toString()
    }
}


