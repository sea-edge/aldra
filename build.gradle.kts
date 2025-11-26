import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

val libsCatalog: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
val springBootVersion = libsCatalog.findVersion("spring-boot").get().requiredVersion

allprojects {
    apply(plugin = "idea")
    apply(plugin = "eclipse")

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    if (!pluginManager.hasPlugin("org.springframework.boot")) {
        apply(plugin = "io.spring.dependency-management")
    }

    extensions.configure<JavaPluginExtension>("java") {
        toolchain.languageVersion.set(JavaLanguageVersion.of(libsCatalog.findVersion("java").get().requiredVersion.split(".").first().toInt()))
    }

    pluginManager.withPlugin("io.spring.dependency-management") {
        extensions.configure<DependencyManagementExtension>("dependencyManagement") {
            imports {
                mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
            }
        }
    }

    configurations.all {
        resolutionStrategy {
            force(
                "tools.jackson.core:jackson-core:${libsCatalog.findVersion("jackson").get().requiredVersion}",
                "tools.jackson.core:jackson-databind:${libsCatalog.findVersion("jackson").get().requiredVersion}",
                "tools.jackson.datatype:jackson-datatype-jdk8:${libsCatalog.findVersion("jackson").get().requiredVersion}",
                "tools.jackson.datatype:jackson-datatype-jsr310:${libsCatalog.findVersion("jackson").get().requiredVersion}"
            )
        }
    }

    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

tasks.wrapper {
    gradleVersion = libsCatalog.findVersion("gradle").get().requiredVersion
}
