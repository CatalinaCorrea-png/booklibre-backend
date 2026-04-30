package ar.edu.unsam.phm.config

import ar.edu.unsam.phm.services.CustomUserDetailsService
import ar.edu.unsam.phm.services.TokenService
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Profile("!test")
class JwtAuthenticationFilter(
    private val userDetailsService: CustomUserDetailsService,
    private val tokenService: TokenService,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader: String? = request.getHeader("Authorization")

        // Si no hay Bearer token, dejar pasar porque Spring Security decidira si el endpoint requiere autenticacion o no
        if (authHeader.doesNotContainBearerToken()) {
            filterChain.doFilter(request, response)
            return
        }

        val jwtToken = authHeader!!.extractTokenValue()

        try {
            val email = tokenService.extractEmail(jwtToken)

            if (email != null && SecurityContextHolder.getContext().authentication == null) {
                val foundUser = userDetailsService.loadUserByUsername(email)

                if (tokenService.isValid(jwtToken, foundUser)) {
                    updateContext(foundUser, request)
                }
            }
        } catch (ex: ExpiredJwtException) {
            writeUnauthorized(
                response,
                wwwAuthenticate = """Bearer error="invalid_token", error_description="The access token expired"""",
                message = "El token de acceso expiró"
            )
            return
        } catch (ex: JwtException) {
            writeUnauthorized(
                response,
                wwwAuthenticate = """Bearer error="invalid_token", error_description="The access token is invalid"""",
                message = "Token inválido"
            )
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun writeUnauthorized(
        response: HttpServletResponse,
        wwwAuthenticate: String,
        message: String
    ) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, wwwAuthenticate)
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(
            objectMapper.writeValueAsString(mapOf("error" to message))
        )
    }

    private fun updateContext(foundUser: UserDetails, request: HttpServletRequest) {
        val authToken = UsernamePasswordAuthenticationToken(foundUser, null, foundUser.authorities)
        authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authToken
    }

    private fun String?.doesNotContainBearerToken(): Boolean =
        this == null || !this.startsWith("Bearer ")

    private fun String.extractTokenValue(): String =
        this.substringAfter("Bearer ")
}