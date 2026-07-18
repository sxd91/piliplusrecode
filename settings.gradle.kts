pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PiliPlusRecodeing"

includeBuild("../miuix") {
    dependencySubstitution {
        substitute(module("top.yukonga.miuix.kmp:miuix-ui")).using(project(":miuix-ui"))
        substitute(module("top.yukonga.miuix.kmp:miuix-preference")).using(project(":miuix-preference"))
        substitute(module("top.yukonga.miuix.kmp:miuix-icons")).using(project(":miuix-icons"))
    }
}
includeBuild("../AndroidLiquidGlass-kmp") {
    dependencySubstitution {
        substitute(module("io.github.kyant0:backdrop")).using(project(":backdrop"))
    }
}

include(":shared")
include(":androidApp")
include(":desktopApp")
include(":macosApp")
