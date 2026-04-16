package ar.edu.unsam.phm.controller.auth

import ar.edu.unsam.phm.dto.AuthRequest
import ar.edu.unsam.phm.dto.AuthenticationResponse
import ar.edu.unsam.phm.services.AuthenticationService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationService: AuthenticationService
) {

    @PostMapping
    fun authenticate(@RequestBody authRequest: AuthRequest): AuthenticationResponse =
        authenticationService.authentication(authRequest)
}