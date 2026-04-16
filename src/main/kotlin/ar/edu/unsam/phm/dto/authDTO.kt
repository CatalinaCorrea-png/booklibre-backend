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
    val name: String,
    val email: String,
    val id: Long
) {}

data class AuthResponse(
    val name: String,
    val email: String,
    val id: Long
) {}

data class AuthRegisterRequest(
    val name: String,
    val email: String,
    val password: String
) {}


data class AuthUserRegisterRequest(
    val name: String = "nombre",
    var lastName: String = "apellido",
    val email: String,
    val password: String
) {}