import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.GradleException

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.flyway.database.postgresql)
    }
}

plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.flyway)
    alias(libs.plugins.spotless)
}

val prettierPluginXmlVersion = libs.versions.prettier.plugin.xml.get()

fun resolveDbConfig(propName: String, envName: String): String? =
    providers.gradleProperty(propName)
        .orElse(providers.environmentVariable(envName))
        .orNull
        ?.takeIf { it.isNotBlank() }

fun requireDbConfig(propName: String, envName: String, description: String): String =
    resolveDbConfig(propName, envName)
        ?: throw GradleException("$description is missing. Set $envName or provide -P$propName.")

val dbJdbcDriver = resolveDbConfig("dbJdbcDriver", "DB_JDBC_DRIVER")
val dbJdbcUrl = resolveDbConfig("dbJdbcUrl", "DB_JDBC_URL")
val dbUser = resolveDbConfig("dbUser", "DB_USER")
val dbPassword = resolveDbConfig("dbPassword", "DB_PASSWORD")

val mbGenerate by configurations.creating
val myBatisGenerator by configurations.creating

configurations {
    mbGenerate.extendsFrom(myBatisGenerator)
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
    implementation(libs.postgresql)
    implementation(libs.mybatis.spring.boot.starter)
    mbGenerate(libs.postgresql)
    myBatisGenerator(libs.mybatis.generator.core)
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/java", "${layout.buildDirectory.get()}/mybatis/gen-src/main/java"))
        }
    }
}

configure<SpotlessExtension> {
    encoding("UTF-8")
    java {
        targetExclude("build/mybatis/gen-src/main/java/**")
        leadingTabsToSpaces()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        googleJavaFormat()
    }
    format("xml") {
        target("config/generator-config.xml", "src/**/*.xml")
        eclipseWtp(com.diffplug.spotless.extra.wtp.EclipseWtpFormatterStep.XML)
    }
}

flyway {
    driver = dbJdbcDriver
    url = dbJdbcUrl
    user = dbUser
    password = dbPassword
    locations = arrayOf("filesystem:${projectDir.path}/migrations")
}

tasks.register("mbGenerate") {
    doFirst {
        file("${layout.buildDirectory.get()}/mybatis/gen-src/main/java").mkdirs()
    }
    doLast {
        val generatorProps = mapOf(
            "driverClassName" to requireDbConfig("dbJdbcDriver", "DB_JDBC_DRIVER", "JDBC driver"),
            "url" to requireDbConfig("dbJdbcUrl", "DB_JDBC_URL", "JDBC url"),
            "username" to requireDbConfig("dbUser", "DB_USER", "DB user"),
            "password" to requireDbConfig("dbPassword", "DB_PASSWORD", "DB password"),
            "targetProject" to "${layout.buildDirectory.get().asFile.path}/mybatis/gen-src/main/java",
            "targetEntityPackage" to "aldra.database.domain.entity",
            "targetMapperPackage" to "aldra.database.domain.repository"
        )

        ant.withGroovyBuilder {
            generatorProps.forEach { (name, value) ->
                "property"("name" to name, "value" to (value ?: ""))
            }

            "taskdef"(
                "name" to "mbGenerator",
                "classname" to "org.mybatis.generator.ant.GeneratorAntTask",
                "classpath" to configurations["myBatisGenerator"].plus(mbGenerate).asPath
            )

            "mbGenerator"(
                "overwrite" to true,
                "configFile" to "${projectDir.path}/config/generator-config.xml",
                "verbose" to true
            ) {
                "propertyset" {
                    generatorProps.keys.forEach { key ->
                        "propertyref"("name" to key)
                    }
                }
            }
        }
    }
}

tasks.named("compileJava") {
    dependsOn("mbGenerate")
}
