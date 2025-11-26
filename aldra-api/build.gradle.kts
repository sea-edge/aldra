import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spotless)
    alias(libs.plugins.openapi.generator)
}

dependencies {
    // Common dependencies
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    implementation(libs.guava)
    implementation(libs.commons.lang3)
    implementation(libs.commons.collections4)
    implementation(libs.commons.io)
    implementation(libs.aws.cognito)
    implementation(libs.aws.s3)
    implementation(libs.auth0.jwt)
    implementation(libs.auth0.jwks)
    testImplementation(libs.spring.boot.starter.test)

    // Module-specific dependencies
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.tx)
    implementation(libs.swagger.annotations)
    implementation(libs.jakarta.validation)
    implementation(project(":common"))
    implementation(project(":database"))

    implementation(libs.jackson.databind)
    implementation(libs.jackson.core)

    // JDBC for DataSource
    implementation(libs.spring.boot.starter.jdbc)
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
        leadingTabsToSpaces()
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
        "local-date" to "LocalDate",
        "local-datetime" to "LocalDateTime"
    ))
    importMappings.set(mapOf(
        "LocalDate" to "java.time.LocalDate",
        "LocalDateTime" to "java.time.LocalDateTime"
    ))
    schemaMappings.set(mapOf(
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
        "useBeanValidation" to "false",
        "useJakartaEe" to "true",
        "additionalModelTypeAnnotations" to "@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)"
    ))
}

tasks.named("compileJava") {
    dependsOn("oaGenerate")
}

tasks.named<BootRun>("bootRun") {
    doFirst {
        val envFile = file("${rootDir}/.envrc")
        if (envFile.exists()) {
            envFile.readLines()
                .filter { it.isNotBlank() && it.contains("=") }
                .mapNotNull { line ->
                    val parts = line.split(" ", limit = 2)
                    if (parts.size > 1) parts[1] else null
                }
                .forEach { envLine ->
                    val (key, value) = envLine.split("=", limit = 2)
                    environment(key, value)
                }
        }
    }
}
