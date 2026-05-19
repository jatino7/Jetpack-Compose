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
        maven {
            url = uri("https://chaquo.com/maven")
        }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.zego.im") } // Add this
        maven { url = uri("https://www.jitpack.io") } // Add this

        maven {
            url = uri("https://chaquo.com/maven")
        }
    }
}

rootProject.name = "Android_Compose"
include(":app")
