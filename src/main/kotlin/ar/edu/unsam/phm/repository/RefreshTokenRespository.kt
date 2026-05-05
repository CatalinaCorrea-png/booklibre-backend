package ar.edu.unsam.phm.repository

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Repository

@Repository
class RefreshTokenRepository {
    private val tokens: MutableMap<String, UserDetails> = mutableMapOf()

    fun save(token: String, userDetails: UserDetails) {
        tokens[token] = userDetails
    }

    fun findUserDetailsByToken(token: String): UserDetails? = tokens[token]

    fun deleteByToken(token: String) {
        tokens.remove(token)
    }
}