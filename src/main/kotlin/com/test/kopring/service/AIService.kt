package com.test.kopring.service

import com.test.kopring.domain.entity.Chat
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

interface AIService {

    fun generateResponse(question: String, chatHistory: List<Chat>, model: String?): String

    fun generateStreamingResponse(
        question: String,
        chatHistory: List<Chat>,
        model: String?,
        emitter: SseEmitter,
        onChunk: ((String) -> Unit)? = null
    )
}

