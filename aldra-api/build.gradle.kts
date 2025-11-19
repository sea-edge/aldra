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
