package com.mybank.backend.config

import com.mybank.backend.security.JwtAuthenticationFilter
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(private val jwtAuthenticationFilter: JwtAuthenticationFilter) {

    private val log = LoggerFactory.getLogger(SecurityConfig::class.java)

    @Bean fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
                .csrf { it.disable() }
                .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
                .authorizeHttpRequests {
                    it.requestMatchers("/auth/**", "/actuator/health", "/error").permitAll()
                    it.anyRequest().authenticated()
                }
                .exceptionHandling {
                    // Not Authenticated (no/invalid/expired JWT token) ->401
                    it.authenticationEntryPoint(
                            AuthenticationEntryPoint { request, response, authException ->
                                response.setHeader("WWW-Authenticate", "Bearer")
                                writeErrorResponse(
                                        response,
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "Authentication required"
                                )
                            }
                    )

                    it.accessDeniedHandler(
                            AccessDeniedHandler { _, response, _ ->
                                writeErrorResponse(
                                        response,
                                        HttpServletResponse.SC_FORBIDDEN,
                                        "Access denied"
                                )
                            }
                    )
                }
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter::class.java
                )
        return http.build()
    }

    private fun writeErrorResponse(response: HttpServletResponse, status: Int, message: String) {
        response.status = status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        val jsonResponse = """{"error": "$message"}"""
        response.writer.write(jsonResponse)
    }
}
