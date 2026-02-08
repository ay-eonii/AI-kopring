package com.test.kopring.service

import com.test.kopring.config.JwtTokenProvider
import com.test.kopring.domain.entity.User
import com.test.kopring.domain.entity.UserRole
import com.test.kopring.domain.repository.UserRepository
import com.test.kopring.dto.AuthResponse
import com.test.kopring.dto.LoginRequest
import com.test.kopring.dto.SignUpRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val activityService: ActivityService
) {

    fun signUp(request: SignUpRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("이미 존재하는 이메일입니다.")
        }

        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            role = UserRole.MEMBER
        )

        val savedUser = userRepository.save(user)

        // Record activity
        activityService.recordSignUp()

        val token = jwtTokenProvider.generateToken(savedUser.email, savedUser.role.name)

        return AuthResponse(
            token = token,
            email = savedUser.email,
            name = savedUser.name,
            role = savedUser.role.name
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { IllegalArgumentException("이메일 또는 비밀번호가 잘못되었습니다.") }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("이메일 또는 비밀번호가 잘못되었습니다.")
        }

        // Record activity
        activityService.recordLogin()

        val token = jwtTokenProvider.generateToken(user.email, user.role.name)

        return AuthResponse(
            token = token,
            email = user.email,
            name = user.name,
            role = user.role.name
        )
    }
}

