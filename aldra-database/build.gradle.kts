import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("com.diffplug.spotless") version "6.25.0"
    id("org.flywaydb.flyway") version "11.0.0"
}

val mbGenerate by configurations.creating

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
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.4")

    mbGenerate("org.postgresql:postgresql:42.7.4")
    mbGenerate("org.mybatis.generator:mybatis-generator-core:1.4.2")
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
        ant.withGroovyBuilder {
            "properties"("driverClassName" to System.getenv("DB_JDBC_DRIVER"))
            "properties"("url" to System.getenv("DB_JDBC_URL"))
            "properties"("username" to System.getenv("DB_USER"))
            "properties"("password" to System.getenv("DB_PASSWORD"))
            "properties"("targetProject" to "${layout.buildDirectory.get().asFile.path}/mybatis/gen-src/main/java")
            "properties"("targetEntityPackage" to "aldra.database.domain.entity")
            "properties"("targetMapperPackage" to "aldra.database.domain.repository")
            
            "taskdef"(
                "name" to "mbGenerator",
                "classname" to "org.mybatis.generator.ant.GeneratorAntTask",
                "classpath" to mbGenerate.asPath
            )
            
            "mbGenerator"(
                "overwrite" to true,
                "configFile" to "${projectDir.path}/config/generator-config.xml",
                "verbose" to true
            ) {
                "propertyset" {
                    "propertyref"("name" to "driverClassName")
                    "propertyref"("name" to "url")
                    "propertyref"("name" to "username")
                    "propertyref"("name" to "password")
                    "propertyref"("name" to "targetProject")
                    "propertyref"("name" to "targetEntityPackage")
                    "propertyref"("name" to "targetMapperPackage")
                }
            }
        }
    }
}
