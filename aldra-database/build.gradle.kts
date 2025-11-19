import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.flyway)
    alias(libs.plugins.spotless)
}

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
    myBatisGenerator("org.mybatis.generator:mybatis-generator-core:1.4.2")
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
        indentWithSpaces()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        googleJavaFormat()
    }
    format("xml") {
        target("config/generator-config.xml", "src/**/*.xml")
        // https://github.com/prettier/plugin-xml
        prettier(mapOf("@prettier/plugin-xml" to "2.1.0"))
            .config(mapOf(
                "useTabs" to false,
                "tabWidth" to 4,
                "printWidth" to 120,
                "xmlWhitespaceSensitivity" to "strict",
                "singleAttributePerLine" to true
            ))
    }
}

flyway {
    driver = System.getenv("DB_JDBC_DRIVER")
    url = System.getenv("DB_JDBC_URL")
    user = System.getenv("DB_USER")
    password = System.getenv("DB_PASSWORD")
    locations = arrayOf("filesystem:${projectDir.path}/migrations")
}

tasks.register("mbGenerate") {
    doFirst {
        file("${layout.buildDirectory.get()}/mybatis/gen-src/main/java").mkdirs()
    }
    doLast {
        val generatorProps = mapOf(
            "driverClassName" to System.getenv("DB_JDBC_DRIVER"),
            "url" to System.getenv("DB_JDBC_URL"),
            "username" to System.getenv("DB_USER"),
            "password" to System.getenv("DB_PASSWORD"),
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
