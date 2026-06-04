import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.serialization") version "1.9.0"
    kotlin("plugin.spring") version "1.9.24"
    kotlin("plugin.jpa") version "1.9.24"
    jacoco
    war
}

group = "ar.edu.unsam.phm"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.data:spring-data-commons")
    val kotestVersion = "5.8.0"

    // Traidos de nuestro proyecto
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    //para que jackson pueda deserializar los data class
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // básicos de cualquier proyecto Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-hateoas")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
//    testImplementation("org.testcontainers:mongodb:1.19.8")
//    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
    //testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.13.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")

    // testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")

    //para posgres y springboot
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("com.h2database:h2")

    // MongoDB
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Spring Security (base necesaria para JWT)
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.security:spring-security-test")

    // JWT - JJWT (librería más popular para Java/Kotlin)
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // graphql -> la 10.5.0 funciona ok, la 11 falla para Spring Boot 3.5.10
    implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:9.2.2"))
    implementation("com.netflix.graphql.dgs:graphql-dgs-spring-graphql-starter")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}
// Solves this WARNING when boot "Getter methods of lazy classes cannot be final: ar.edu.unsam.phm.domain.Book#getAuthor..."
// ~"...In Kotlin, all classes and methods are final by default. Hibernate needs to create proxy subclasses of your @Entity classes
// to support lazy loading — but it can't subclass or override final methods..."

allOpen {
    annotation("jakarta.persistence.Entity")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude("**/config/**", "**/entity/**", "**/*Application*.*", "**/ServletInitializer.*")
            }
        })
    )
    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }
}

tasks.register("runOnGitHub") {
    dependsOn("jacocoTestReport")
    group = "custom"
    description = "$ ./gradlew runOnGitHub # runs on GitHub Action"
}
