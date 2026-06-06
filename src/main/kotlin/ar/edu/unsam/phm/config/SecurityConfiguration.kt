package ar.edu.unsam.phm.config

import ar.edu.unsam.phm.domain.UserTypes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@Profile("!test")
class SecurityConfiguration(
    private val authenticationProvider: AuthenticationProvider
) {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter
    ): DefaultSecurityFilterChain =
        http
            .cors(Customizer.withDefaults())
            .csrf {
                // Access token viaja en header Authorization (no cookie), inmune a CSRF
                // Refresh token en cookie SameSite=Strict, el browser no la manda cross-site
                it.disable()
                //it.ignoringRequestMatchers("/api/auth", "/refresh")

                // esto genera la cookie XSRF-TOKEN
                //it.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                //it.csrfTokenRequestHandler(SpaCsrfTokenRequestHandler())
            }
            .authorizeHttpRequests {
                it
                    // Endpoints publicos
                    .requestMatchers("/api/auth", "/api/auth/refresh", "/error", "/books/**", "/book-titulo/**").permitAll()
                    .requestMatchers("/assets/**").permitAll() // para que no rompan las imagenes de perfil
                    // GraphQL: endpoint del tablero de KPIs. Abierto para la demo del TP.
                    // (Es para un admin: en producción convendría restringirlo a un rol.)
                    .requestMatchers("/graphql", "/graphiql", "/graphiql/**").permitAll()
                    .requestMatchers(HttpMethod.OPTIONS)
                    .permitAll() // esto es para react pregunta antes de hacer la request real
                    .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()

                    // Endpoints por rol
                    .requestMatchers(HttpMethod.GET, "/filtered-books")
                    .hasAnyAuthority(UserTypes.READER.name, UserTypes.COMBINED.name)
                    .requestMatchers(HttpMethod.GET, "/book-detail/**")
                    .hasAnyAuthority(UserTypes.READER.name, UserTypes.COMBINED.name, UserTypes.PUBLISHER.name)
                    .requestMatchers(HttpMethod.POST, "/create-book")
                    .hasAnyAuthority(UserTypes.PUBLISHER.name, UserTypes.COMBINED.name)
                    .requestMatchers(HttpMethod.POST, "/create-reservation")
                    .hasAnyAuthority(UserTypes.READER.name, UserTypes.COMBINED.name)
                    .requestMatchers(HttpMethod.PUT, "/edit-book/**")
                    .hasAnyAuthority(UserTypes.PUBLISHER.name, UserTypes.COMBINED.name)
                    .requestMatchers(HttpMethod.POST, "/*/calificar")
                    .hasAnyAuthority(UserTypes.READER.name, UserTypes.COMBINED.name)
                    .requestMatchers(HttpMethod.DELETE, "/delete-book/**")
                    .hasAnyAuthority(UserTypes.PUBLISHER.name, UserTypes.COMBINED.name)
                    .requestMatchers(HttpMethod.GET, "/userOwnBooks/**")
                    .hasAnyAuthority(UserTypes.PUBLISHER.name, UserTypes.COMBINED.name)

                    .anyRequest().fullyAuthenticated() // el resto esta bloqueado si no se autentica
            }

            .sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // la session es stateless por que estamos usando rest api paaaa
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling {
                it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // esto manda 401 si no tenes token o es invalido
            }
            .build()

    @Bean // Spring Security (antes de los filtros de seguridad)
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:5173")
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("WWW-Authenticate")
            allowCredentials = true
            maxAge = 3600 // esto cachea el options
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }

}