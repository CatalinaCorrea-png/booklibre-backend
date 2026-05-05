package ar.edu.unsam.phm.domain

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val uploadsPath = Paths.get("uploads").toFile().absolutePath

        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:$uploadsPath/")
    }
}