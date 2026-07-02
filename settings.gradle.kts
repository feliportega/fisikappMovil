pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}



rootProject.name = "FisikappMovil"

include(":app")
include(":facesdk")
//include(":unityLibrary")
include(":unityLibrary:xrmanifest.androidlib")

project(":unityLibrary:xrmanifest.androidlib").projectDir =
    file("unityLibrary/xrmanifest.androidlib")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        google()
        mavenCentral()

        flatDir {
            dirs(file("unityLibrary/libs"))
        }

    }
}