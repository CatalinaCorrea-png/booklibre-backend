package ar.edu.unsam.phm.config

import ar.edu.unsam.phm.domain.UserTypes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfiguration(
    private val authenticationProvider: AuthenticationProvider
) {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter
    ): DefaultSecurityFilterChain =
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/api/auth", "/api/auth/refresh", "/error")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/register")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/create-book").hasAnyAuthority(UserTypes.PUBLISHER.name,
                        UserTypes.COMBINED.name)
                    .requestMatchers(HttpMethod.POST, "/create-reservation").hasAnyAuthority(UserTypes.READER.name,
                        UserTypes.COMBINED.name)
                    .requestMatchers(HttpMethod.PUT, "/edit-book/**").hasAnyAuthority(UserTypes.PUBLISHER.name, UserTypes.COMBINED.name)
                    .requestMatchers(HttpMethod.PATCH, "/**/calificar").hasAnyAuthority(UserTypes.READER.name,
                        UserTypes.COMBINED.name)
                    .requestMatchers(HttpMethod.DELETE, "/delete-book/**").hasAnyAuthority(UserTypes.PUBLISHER.name,
                        UserTypes.COMBINED.name)
                    .requestMatchers(HttpMethod.GET, "/userOwnBooks/**").hasAnyAuthority(UserTypes.PUBLISHER.name,
                        UserTypes.COMBINED.name)
                    .anyRequest()
                    .fullyAuthenticated()
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
}