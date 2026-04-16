package ar.edu.unsam.phm.services

import ar.edu.unsam.phm.config.JwtProperties
import ar.edu.unsam.phm.dto.AuthRequest
import ar.edu.unsam.phm.dto.AuthenticationResponse
import ar.edu.unsam.phm.repository.RefreshTokenRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.web.servlet.HandlerMapping
import java.util.Date

@Service
class AuthenticationService(
    private val authManager: AuthenticationManager,
    private val userDetailsService: CustomUserDetailsService,
    private val tokenService: TokenService,
    private val jwtProperties: JwtProperties,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userService: UserService
) {
    fun authentication(request: AuthRequest): AuthenticationResponse {
        authManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.email,
                request.password
            )
        )

        val user = userDetailsService.loadUserByUsername(request.email)
        val accessToken = generateAccessToken(user)
        val refreshToken = generateRefreshToken(user)
        refreshTokenRepository.save(refreshToken, user)
        val userOK = userService.getUserByEmail(user.username)

        return AuthenticationResponse(accessToken, refreshToken, userOK.name, userOK.email, userOK.id!!)
    }

    fun refreshAccessToken(token: String): String?{
        val extractedEmail = tokenService.extractEmail(token)

        return extractedEmail?.let { email ->
            val currentUserDetails = userDetailsService.loadUserByUsername(email)
            val refreshTokenUserDetails = refreshTokenRepository.findUserDetailsByToken(token)

            if(!tokenService.isExpired(token) && currentUserDetails.username == refreshTokenUserDetails?.username)
                generateAccessToken(currentUserDetails)
            else
                null
        }
    }

    private fun generateRefreshToken(user: UserDetails): String = tokenService.generate(
        userDetails = user,
        expirationDate = Date(System.currentTimeMillis() + jwtProperties.refreshTokenExpiration)
    )

    private fun generateAccessToken(user: UserDetails): String = tokenService.generate(
        userDetails = user,
        expirationDate = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration)
    )

}
