package com.mybank.backend.security

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(private val jwtService: JwtService) : OncePerRequestFilter() {

    override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.removePrefix("Bearer ")
            try {
                val customerId = jwtService.extractCustomerId(token)
                val authentication =
                        UsernamePasswordAuthenticationToken(customerId, null, emptyList())
                SecurityContextHolder.getContext().authentication = authentication
                logger.debug(
                        "JWT validated successfully, authentication set for customerId=$customerId"
                )
            } catch (e: ExpiredJwtException) {
                SecurityContextHolder.clearContext()
                request.setAttribute(JWT_ERROR_ATTRIBUTE, "expired")
                logger.debug("JWT validation failed : ${e.message}")
            } catch (e: JwtException) {
                SecurityContextHolder.clearContext()
                request.setAttribute(JWT_ERROR_ATTRIBUTE, "invalid_token")
                logger.debug("JWT validation failed : ${e.message}")
            } catch (e: IllegalArgumentException) {
                 SecurityContextHolder.clearContext()
                request.setAttribute(JWT_ERROR_ATTRIBUTE, "invalid_token")
                logger.debug("JWT rejected : malformmed subject")
            } 
        }
        filterChain.doFilter(request, response)
    }
    
       companion object {
        const val JWT_ERROR_ATTRIBUTE = "jwt_error"
    }
}
