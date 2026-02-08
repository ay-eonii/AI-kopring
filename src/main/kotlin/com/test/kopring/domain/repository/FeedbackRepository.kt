package com.test.kopring.domain.repository

import com.test.kopring.domain.entity.Chat
import com.test.kopring.domain.entity.Feedback
import com.test.kopring.domain.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FeedbackRepository : JpaRepository<Feedback, Long> {
    fun existsByUserAndChat(user: User, chat: Chat): Boolean
    fun findByUser(user: User, pageable: Pageable): Page<Feedback>

    @Query("SELECT f FROM Feedback f")
    fun findAllFeedbacks(pageable: Pageable): Page<Feedback>

    @Query("SELECT f FROM Feedback f WHERE f.user = :user AND f.isPositive = :isPositive")
    fun findByUserAndIsPositive(user: User, isPositive: Boolean, pageable: Pageable): Page<Feedback>

    @Query("SELECT f FROM Feedback f WHERE f.isPositive = :isPositive")
    fun findByIsPositive(isPositive: Boolean, pageable: Pageable): Page<Feedback>
}

