package com.mybank.backend.config

import com.mybank.backend.security.JwtAuthenticationFilter
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(private val jwtAuthenticationFilter: JwtAuthenticationFilter) {

    private val log = LoggerFactory.getLogger(SecurityConfig::class.java)

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/auth/**", "/actuator/health","/error").permitAll()
                it.anyRequest().authenticated()
            }
            .exceptionHandling {
                it.accessDeniedHandler(AccessDeniedHandler { request, response, ex ->
                    val auth = SecurityContextHolder.getContext().authentication
                    log.warn(
                        "ACCESS DENIED for {} {} — authentication={}, principal={}, authenticated={}, authorities={}, reason={}",
                        request.method, request.requestURI,
                        auth, auth?.principal, auth?.isAuthenticated, auth?.authorities, ex.message
                    )
                    response.status = 403
                })
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}