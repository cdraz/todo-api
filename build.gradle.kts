plugins {
    kotlin("jvm") version "2.0.21"
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

group = "com.github.cdraz"
version = "1.0-SNAPSHOT"

springBoot {
    mainClass.set("com.github.cdraz.todoapi.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Testing
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<io.gitlab.arturbosch.detekt.Detekt>("detekt") {
    setSource(files("src/main/kotlin", "src/test/kotlin"))
}
