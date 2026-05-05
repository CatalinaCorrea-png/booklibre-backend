package ar.edu.unsam.phm.controller.auth

import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.AuthRegisterRequest
import ar.edu.unsam.phm.dto.AuthRequest
import ar.edu.unsam.phm.dto.AuthResponse
import ar.edu.unsam.phm.dto.AuthenticationResponse
import ar.edu.unsam.phm.services.AuthenticationService
import ar.edu.unsam.phm.services.UserService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
//@CrossOrigin("*")
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val authenticationService: AuthenticationService,
) {

    @PostMapping
    fun authenticate(
        @RequestBody authRequest: AuthRequest,
        response: HttpServletResponse // esto no lo manda el front, es para armar vos la respuesta
    ): AuthenticationResponse {
        val authResponse = authenticationService.authentication(authRequest)

        val refreshCookie = ResponseCookie.from("refreshToken", authResponse.refreshToken)
            .httpOnly(true)
            .secure(false) // todo: esto se tiene que cambiar, ahora local host no es secure
            .path("/")
            .maxAge(1800)
            .sameSite("Strict")
            .build()

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString())

        return authResponse.copy(refreshToken = "") // devolvemos el AccessToken pero vaciamos el RefreshToken del JSON
    }

    @PostMapping("/refresh")
    fun refreshAccessToken(
        @CookieValue(name = "refreshToken") token: String?,
        response: HttpServletResponse
    ): TokenResponse {
        if (token.isNullOrEmpty()) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

        val (newAccess, newRefresh) = authenticationService.refreshAccessToken(token)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

        val newCookie = ResponseCookie.from("refreshToken", newRefresh)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(1800)
            .sameSite("Strict")
            .build()

        response.addHeader(HttpHeaders.SET_COOKIE, newCookie.toString())

        return TokenResponse(token = newAccess)
    }

    @PostMapping("/register")
    fun createUser(@RequestBody request: AuthRegisterRequest): AuthResponse {
        val user = User(
            email = request.email,
            password = request.password,
            name = request.name
        )
        val savedUser = userService.create(user)
        return AuthResponse(
            email = savedUser.email,
            name = savedUser.name,
            id = savedUser.id!!  //esto esta garantizado por JPA que no va a venir null ya que el es el encargado de crearlo
        )
    }
}