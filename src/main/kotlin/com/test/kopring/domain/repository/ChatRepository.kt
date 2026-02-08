package com.test.kopring.domain.repository

import com.test.kopring.domain.entity.Chat
import com.test.kopring.domain.entity.Thread
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface ChatRepository : JpaRepository<Chat, Long> {
    fun findByThread(thread: Thread): List<Chat>

    @Query("SELECT c FROM Chat c WHERE c.createdAt >= :startTime")
    fun findAllByCreatedAtAfter(startTime: Instant): List<Chat>
}

