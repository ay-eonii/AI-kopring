package com.test.kopring.service

import com.test.kopring.domain.entity.Chat
import com.test.kopring.domain.entity.Thread
import com.test.kopring.domain.entity.User
import com.test.kopring.domain.entity.UserRole
import com.test.kopring.domain.repository.ChatRepository
import com.test.kopring.domain.repository.ThreadRepository
import com.test.kopring.dto.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.Duration
import java.time.Instant

@Service
@Transactional
class ChatService(
    private val chatRepository: ChatRepository,
    private val threadRepository: ThreadRepository,
    private val aiService: AIService,
    private val activityService: ActivityService
) {
    companion object {
        private const val THREAD_TIMEOUT_MINUTES = 30L
    }

    fun createChat(user: User, request: ChatRequest): ChatResponse {
        // Find or create thread
        val thread = findOrCreateThread(user)

        // Get chat history for context
        val chatHistory = chatRepository.findByThread(thread)

        // Generate response from AI
        val answer = aiService.generateResponse(
            question = request.question,
            chatHistory = chatHistory,
            model = request.model
        )

        // Save chat
        val chat = Chat(
            thread = thread,
            question = request.question,
            answer = answer
        )
        val savedChat = chatRepository.save(chat)

        // Update thread's lastChatAt
        thread.lastChatAt = Instant.now()
        threadRepository.save(thread)

        // Record activity
        activityService.recordChat()

        return ChatResponse(
            chatId = savedChat.id!!,
            question = savedChat.question,
            answer = savedChat.answer,
            threadId = thread.id!!,
            createdAt = savedChat.createdAt
        )
    }

    fun createStreamingChat(user: User, request: ChatRequest): Pair<SseEmitter, Thread> {
        val emitter = SseEmitter(Long.MAX_VALUE)

        // Find or create thread
        val thread = findOrCreateThread(user)

        // Get chat history for context
        val chatHistory = chatRepository.findByThread(thread)

        // Start streaming in a separate thread
        Thread {
            val fullAnswer = StringBuilder()

            try {
                // Generate streaming response and collect the full answer
                aiService.generateStreamingResponse(
                    question = request.question,
                    chatHistory = chatHistory,
                    model = request.model,
                    emitter = emitter,
                    onChunk = { chunk ->
                        // Collect each chunk to build the full answer
                        fullAnswer.append(chunk)
                    }
                )

                // Save the collected full answer to database
                val chat = Chat(
                    thread = thread,
                    question = request.question,
                    answer = fullAnswer.toString()
                )
                chatRepository.save(chat)

                thread.lastChatAt = Instant.now()
                threadRepository.save(thread)

                // Record activity
                activityService.recordChat()

            } catch (e: Exception) {
                emitter.completeWithError(e)
            }
        }.start()

        return Pair(emitter, thread)
    }

    @Transactional(readOnly = true)
    fun getChatList(
        user: User,
        page: Int,
        size: Int,
        sortDirection: String
    ): ChatListResponse {
        val sort = if (sortDirection.equals("asc", ignoreCase = true)) {
            Sort.by("createdAt").ascending()
        } else {
            Sort.by("createdAt").descending()
        }

        val pageable = PageRequest.of(page, size, sort)

        val threads = if (user.role == UserRole.ADMIN) {
            threadRepository.findAllByOrderByCreatedAtDesc()
        } else {
            threadRepository.findByUserOrderByCreatedAtDesc(user)
        }

        // Pagination on threads
        val start = page * size
        val end = minOf(start + size, threads.size)
        val paginatedThreads = if (start < threads.size) {
            threads.subList(start, end)
        } else {
            emptyList()
        }

        val threadResponses = paginatedThreads.map { thread ->
            val chats = chatRepository.findByThread(thread)
            ThreadResponse(
                threadId = thread.id!!,
                userId = thread.user.id!!,
                createdAt = thread.createdAt,
                lastChatAt = thread.lastChatAt,
                chats = chats.map { chat ->
                    ChatInThreadResponse(
                        chatId = chat.id!!,
                        question = chat.question,
                        answer = chat.answer,
                        createdAt = chat.createdAt
                    )
                }
            )
        }

        return ChatListResponse(
            threads = threadResponses,
            totalElements = threads.size.toLong(),
            totalPages = (threads.size + size - 1) / size,
            currentPage = page
        )
    }

    fun deleteThread(user: User, threadId: Long) {
        val thread = threadRepository.findById(threadId)
            .orElseThrow { IllegalArgumentException("스레드를 찾을 수 없습니다.") }

        if (thread.user.id != user.id) {
            throw IllegalArgumentException("자신이 생성한 스레드만 삭제할 수 있습니다.")
        }

        threadRepository.delete(thread)
    }

    private fun findOrCreateThread(user: User): Thread {
        val lastThread = threadRepository.findFirstByUserOrderByLastChatAtDesc(user)
            .orElse(null)

        return if (lastThread == null || isThreadExpired(lastThread)) {
            // Create new thread
            val newThread = Thread(user = user)
            threadRepository.save(newThread)
        } else {
            lastThread
        }
    }

    private fun isThreadExpired(thread: Thread): Boolean {
        val now = Instant.now()
        val duration = Duration.between(thread.lastChatAt, now)
        return duration.toMinutes() > THREAD_TIMEOUT_MINUTES
    }
}







