package com.test.kopring.config

import com.test.kopring.service.AIService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class AIServiceConfig {

    @Value("\${ai.provider:openai}")
    private lateinit var provider: String

    @Bean
    @Primary
    fun aiService(
        @Qualifier("openAIService") openAIService: AIService,
    ): AIService {
        return when (provider.lowercase()) {
            "openai" -> openAIService
            else -> throw IllegalArgumentException("지원하지 않는 AI provider입니다: $provider (openai 또는 claude를 사용하세요)")
        }
    }
}

