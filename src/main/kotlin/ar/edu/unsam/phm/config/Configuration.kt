package ar.edu.unsam.phm.config

import ar.edu.unsam.phm.services.CustomUserDetailsService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class Configuration {

    @Bean
    fun encoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationProvider(userDetailService: CustomUserDetailsService): AuthenticationProvider =
        DaoAuthenticationProvider()
            .also {
                it.setUserDetailsService(userDetailService)
                it.setPasswordEncoder(encoder())
            }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager
}