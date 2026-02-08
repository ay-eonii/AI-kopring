package com.test.kopring.controller

import com.test.kopring.domain.entity.User
import com.test.kopring.dto.ChatListResponse
import com.test.kopring.dto.ChatRequest
import com.test.kopring.service.ChatService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chats")
class ChatController(
    private val chatService: ChatService
) {

    @PostMapping
    fun createChat(
        @AuthenticationPrincipal user: User,
        @Valid @RequestBody request: ChatRequest
    ): ResponseEntity<*> {
        return if (request.isStreaming == true) {
            val (emitter, _) = chatService.createStreamingChat(user, request)
            ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(emitter)
        } else {
            val response = chatService.createChat(user, request)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        }
    }

    @GetMapping
    fun getChatList(
        @AuthenticationPrincipal user: User,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "desc") sort: String
    ): ResponseEntity<ChatListResponse> {
        val response = chatService.getChatList(user, page, size, sort)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/threads/{threadId}")
    fun deleteThread(
        @AuthenticationPrincipal user: User,
        @PathVariable threadId: Long
    ): ResponseEntity<Void> {
        chatService.deleteThread(user, threadId)
        return ResponseEntity.noContent().build()
    }
}

