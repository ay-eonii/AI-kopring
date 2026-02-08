package com.test.kopring.service

import com.test.kopring.domain.entity.Chat
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Service
class OpenAIService(
    @Value("\${openai.api-key}") private val apiKey: String,
    @Value("\${openai.default-model}") private val defaultModel: String,
    @Value("\${openai.max-history-messages}") private val maxHistoryMessages: Int,
    @Value("\${openai.max-tokens}") private val maxTokens: Int
) : AIService {
    private val restTemplate = RestTemplate()
    private val apiUrl = "https://api.openai.com/v1/chat/completions"

    override fun generateResponse(question: String, chatHistory: List<Chat>, model: String?): String {
        val messages = buildMessages(question, chatHistory)

        val requestBody = mapOf(
            "model" to (model ?: defaultModel),
            "messages" to messages,
            "max_tokens" to maxTokens
        )

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(apiKey)
        }

        val entity = HttpEntity(requestBody, headers)

        return try {
            val response = restTemplate.postForObject(apiUrl, entity, Map::class.java)
            @Suppress("UNCHECKED_CAST")
            val choices = response?.get("choices") as? List<Map<String, Any>>
            @Suppress("UNCHECKED_CAST")
            val message = choices?.firstOrNull()?.get("message") as? Map<String, Any>
            message?.get("content") as? String ?: "답변을 생성할 수 없습니다."
        } catch (e: Exception) {
            "OpenAI API 호출 중 오류가 발생했습니다: ${e.message}"
        }
    }

    override fun generateStreamingResponse(
        question: String,
        chatHistory: List<Chat>,
        model: String?,
        emitter: SseEmitter,
        onChunk: ((String) -> Unit)?
    ) {
        try {
            // 현재는 간단한 구현으로 전체 응답을 생성 후 한 번에 전송합니다
            val answer = generateResponse(question, chatHistory, model)

            // 스트리밍처럼 보이게 하기 위해 문자 단위로 전송
            answer.chunked(10).forEach { chunk ->
                emitter.send(chunk)
                onChunk?.invoke(chunk)
                Thread.sleep(50) // 스트리밍 효과
            }

            emitter.complete()
        } catch (e: Exception) {
            emitter.completeWithError(e)
        }
    }

    /**
     * 대화 히스토리를 최근 N개로 제한하여 메시지 리스트 생성
     * 토큰 비용 절감을 위해 최근 대화만 컨텍스트로 사용
     */
    private fun buildMessages(question: String, chatHistory: List<Chat>): List<Map<String, String>> {
        val messages = mutableListOf<Map<String, String>>()

        // 최근 N개의 대화만 사용 (maxHistoryMessages 제한)
        val limitedHistory = if (chatHistory.size > maxHistoryMessages) {
            chatHistory.takeLast(maxHistoryMessages)
        } else {
            chatHistory
        }

        // Add limited chat history
        limitedHistory.forEach { chat ->
            messages.add(mapOf("role" to "user", "content" to chat.question))
            messages.add(mapOf("role" to "assistant", "content" to chat.answer))
        }

        // Add current question
        messages.add(mapOf("role" to "user", "content" to question))

        return messages
    }
}


