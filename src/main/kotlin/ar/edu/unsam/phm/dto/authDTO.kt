package ar.edu.unsam.phm.dto

// REQUEST - Lo que recibe el endpoint
data class AuthRequest(
    var email: String,
    var password: String
) {}

// RESPONSE - Lo que devuelve el endpoint, lo que necesita el front
data class AuthenticationResponse(
    val accessToken: String,
    val refreshToken: String,
    val expirationTime:  Long,
    val id: String // pasamos esto por que si no tenemos que cambiar toda la logica en el front y los endpoints
) {}

data class AuthResponse(
    val name: String,
    val email: String,
    val id: String
) {}

data class AuthRegisterRequest(
    val name: String,
    val email: String,
    val password: String
) {}