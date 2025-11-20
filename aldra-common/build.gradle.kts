plugins {
    alias(libs.plugins.spotless)
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
    implementation(libs.spring.boot.configuration.processor)
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.spring.boot.starter.logging)
}

spotless {
    encoding("UTF-8")
    java {
        leadingTabsToSpaces()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        googleJavaFormat()
    }
}
