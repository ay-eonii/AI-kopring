package com.test.kopring.domain.repository

import com.test.kopring.domain.entity.Thread
import com.test.kopring.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.Optional

@Repository
interface ThreadRepository : JpaRepository<Thread, Long> {
    fun findFirstByUserOrderByLastChatAtDesc(user: User): Optional<Thread>
    fun findByUserOrderByCreatedAtDesc(user: User): List<Thread>
    fun findAllByOrderByCreatedAtDesc(): List<Thread>
}

