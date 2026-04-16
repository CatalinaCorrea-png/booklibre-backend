package ar.edu.unsam.phm.controller.auth

import ar.edu.unsam.phm.dto.AuthRequest
import ar.edu.unsam.phm.dto.AuthenticationResponse
import ar.edu.unsam.phm.services.AuthenticationService
import ar.edu.unsam.phm.services.TokenService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@CrossOrigin("*")
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationService: AuthenticationService,
    private val tokenService: TokenService
) {

    @PostMapping
    fun authenticate(@RequestBody authRequest: AuthRequest): AuthenticationResponse =
        authenticationService.authentication(authRequest)

    @PostMapping("/refresh")
    fun refreshAccessToken(@RequestBody request: RefreshTokenResponse): TokenResponse =
        authenticationService.refreshAccessToken(request.token)
            ?.mapToTokenResponse()
            ?: throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Invalid refresh token!"
            )

    private fun String.mapToTokenResponse(): TokenResponse = TokenResponse( token = this )
}