pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "aldra"

include("api")
include("batch")
include("common")
include("database")

project(":api").projectDir = file("aldra-api")
project(":batch").projectDir = file("aldra-batch")
project(":common").projectDir = file("aldra-common")
project(":database").projectDir = file("aldra-database")
