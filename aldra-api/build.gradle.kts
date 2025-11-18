import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("org.springframework.boot") version "3.4.0"
    id("com.diffplug.spotless") version "6.25.0"
    id("org.openapi.generator") version "7.10.0"
}

dependencies {
    // Common dependencies
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    testCompileOnly("org.projectlombok:lombok:1.18.36")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.36")
    implementation("com.google.guava:guava:33.3.1-jre")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("org.apache.commons:commons-collections4:4.5.0-M2")
    implementation("commons-io:commons-io:2.18.0")
    implementation("com.amazonaws:aws-java-sdk-cognitoidp:1.12.780")
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.780")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("com.auth0:jwks-rsa:0.22.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.4.0")
    
    // Module-specific dependencies
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.0")
    implementation("org.springframework.boot:spring-boot-starter-actuator:3.4.0")
    implementation("org.springframework.boot:spring-boot-starter-security:3.4.0")
    implementation("org.springframework:spring-tx:6.2.0")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.26")
    // FIXME https://github.com/OpenAPITools/openapi-generator/issues/12603
    implementation("jakarta.validation:jakarta.validation-api:3.1.0")
    implementation(project(":common"))
    implementation(project(":database"))
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/java", "${layout.buildDirectory.get()}/openapi/gen-src/main/java"))
        }
    }
}

spotless {
    encoding("UTF-8")
    java {
        targetExclude("build/openapi/gen-src/main/java/**")
        indentWithSpaces()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        googleJavaFormat()
    }
}

// see. https://openapi-generator.tech/docs/plugins#gradle
tasks.register<GenerateTask>("oaGenerate") {
    generatorName.set("spring")
    inputSpec.set("${projectDir}/openapi/index.yml")
    outputDir.set("${layout.buildDirectory.get()}/openapi")
    typeMappings.set(mapOf(
        "string+local-date" to "LocalDate",
        "string+local-datetime" to "LocalDateTime"
    ))
    importMappings.set(mapOf(
        "LocalDate" to "java.time.LocalDate",
        "LocalDateTime" to "java.time.LocalDateTime"
    ))
    configOptions.set(mapOf(
        "hideGenerationTimestamp" to "true",
        "sourceFolder" to "gen-src/main/java",
        "basePackage" to "aldra.api",
        "apiPackage" to "aldra.api.adapter.web.controller",
        "modelPackage" to "aldra.api.adapter.web.dto",
        "skipDefaultInterface" to "true",
        "oas3" to "true",
        "dateLibrary" to "java8",
        "delegatePattern" to "false",
        "interfaceOnly" to "true",
        "openApiNullable" to "false",
        "useTags" to "true",
        "disallowAdditionalPropertiesIfNotPresent" to "false",
        "useBeanValidation" to "false"
    ))
}

tasks.named<BootRun>("bootRun") {
    doFirst {
        file("${rootDir}/.envrc").readLines().forEach {
            val parts = it.split(" ")[1].split("=")
            val key = parts[0]
            val value = parts[1]
            environment(key, value)
        }
    }
}
