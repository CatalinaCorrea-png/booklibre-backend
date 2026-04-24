package ar.edu.unsam.phm.config

import ar.edu.unsam.phm.domain.UserTypes
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.csrf.*
import org.springframework.util.StringUtils
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.util.function.Supplier

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
            .cors(Customizer.withDefaults())
            .csrf {
                it.ignoringRequestMatchers("/api/auth", "/refresh")
                it.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                it.csrfTokenRequestHandler(SpaCsrfTokenRequestHandler())
            }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/api/auth", "/api/auth/refresh", "/error")
                    .permitAll()
                    .requestMatchers(HttpMethod.OPTIONS).permitAll()
                    .requestMatchers(HttpMethod.POST, "/register")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/create-book").hasAnyAuthority(
                        UserTypes.PUBLISHER.name,
                        UserTypes.COMBINED.name
                    )
                    .requestMatchers(HttpMethod.POST, "/create-reservation").hasAnyAuthority(
                        UserTypes.READER.name,
                        UserTypes.COMBINED.name
                    )
                    .requestMatchers(HttpMethod.PUT, "/edit-book/**")
                    .hasAnyAuthority(UserTypes.PUBLISHER.name, UserTypes.COMBINED.name)
                    .requestMatchers(HttpMethod.PATCH, "/**/calificar").hasAnyAuthority(
                        UserTypes.READER.name,
                        UserTypes.COMBINED.name
                    )
                    .requestMatchers(HttpMethod.DELETE, "/delete-book/**").hasAnyAuthority(
                        UserTypes.PUBLISHER.name,
                        UserTypes.COMBINED.name
                    )
                    .requestMatchers(HttpMethod.GET, "/userOwnBooks/**").hasAnyAuthority(
                        UserTypes.PUBLISHER.name,
                        UserTypes.COMBINED.name
                    )
                    .anyRequest()
                    .fullyAuthenticated()
            }
//            .httpBasic(Customizer.withDefaults()) // esot pone el basic?
            .sessionManagement { configurer ->
                configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling(Customizer.withDefaults()) // todo: ver para que es esto
            .build()

//    @Bean
//    fun corsConfigurer(): WebMvcConfigurer {
//        return object : WebMvcConfigurer {
//            override fun addCorsMappings(registry: CorsRegistry) {
//                registry.addMapping("/**")
//                    .allowedOrigins("http://localhost:5173")
//                    .allowedHeaders("*")
//                    .allowedMethods("POST", "GET", "PUT", "DELETE")
//                    .allowCredentials(true)
//                    .exposedHeaders("WWW-Authenticate")
//            }
//        }
//    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:5173")
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("WWW-Authenticate")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }

}

class SpaCsrfTokenRequestHandler : CsrfTokenRequestHandler {
    private val plain: CsrfTokenRequestHandler = CsrfTokenRequestAttributeHandler()
    private val xor: CsrfTokenRequestHandler = XorCsrfTokenRequestAttributeHandler()

    val logger: Logger = LoggerFactory.getLogger(SpaCsrfTokenRequestHandler::class.java)

    override fun handle(request: HttpServletRequest, response: HttpServletResponse, csrfToken: Supplier<CsrfToken>) {
        /*
         * Always use XorCsrfTokenRequestAttributeHandler to provide BREACH protection of
         * the CsrfToken when it is rendered in the response body.
         */
        xor.handle(request, response, csrfToken)
        /*
         * Render the token value to a cookie by causing the deferred token to be loaded.
         */
        csrfToken.get()
    }

    override fun resolveCsrfTokenValue(request: HttpServletRequest, csrfToken: CsrfToken): String? {
        logger.info("header name ${csrfToken.headerName}")
        val headerValue = request.getHeader(csrfToken.headerName)
        /*
         * If the request contains a request header, use CsrfTokenRequestAttributeHandler
         * to resolve the CsrfToken. This applies when a single-page application includes
         * the header value automatically, which was obtained via a cookie containing the
         * raw CsrfToken.
         */
        logger.info("header value $headerValue")
        return if (StringUtils.hasText(headerValue)) {
            logger.info("plain $plain")
            plain
        } else {
            /*
             * In all other cases (e.g. if the request contains a request parameter), use
             * XorCsrfTokenRequestAttributeHandler to resolve the CsrfToken. This applies
             * when a server-side rendered form includes the _csrf request parameter as a
             * hidden input.
             */
            logger.info("xor $xor")
            xor
        }.resolveCsrfTokenValue(request, csrfToken)
    }
}