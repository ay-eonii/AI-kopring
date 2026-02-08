package com.test.kopring.service

import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Service
class ActivityService {
    private val signUpCounts = ConcurrentHashMap<String, AtomicLong>()
    private val loginCounts = ConcurrentHashMap<String, AtomicLong>()
    private val chatCounts = ConcurrentHashMap<String, AtomicLong>()

    fun recordSignUp() {
        val dateKey = getDateKey()
        signUpCounts.computeIfAbsent(dateKey) { AtomicLong(0) }.incrementAndGet()
    }

    fun recordLogin() {
        val dateKey = getDateKey()
        loginCounts.computeIfAbsent(dateKey) { AtomicLong(0) }.incrementAndGet()
    }

    fun recordChat() {
        val dateKey = getDateKey()
        chatCounts.computeIfAbsent(dateKey) { AtomicLong(0) }.incrementAndGet()
    }

    fun getSignUpCount(dateKey: String): Long {
        return signUpCounts[dateKey]?.get() ?: 0
    }

    fun getLoginCount(dateKey: String): Long {
        return loginCounts[dateKey]?.get() ?: 0
    }

    fun getChatCount(dateKey: String): Long {
        return chatCounts[dateKey]?.get() ?: 0
    }

    private fun getDateKey(): String {
        val now = Instant.now()
        return now.toString().substring(0, 10)
    }

    fun getCurrentDateKey(): String = getDateKey()
}

