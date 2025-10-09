pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven {
          url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        }
        mavenCentral()
        gradlePluginPortal()
    }

    // Handle resolution of plugins published without Gradle Plugin Portal manually
    resolutionStrategy {
      eachPlugin {
        if (requested.id.id == "com.mapbox.sdkRegistry") {
          useModule("com.mapbox.gradle.plugins:sdk-registry:${requested.version}")
        }
      }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        }
    }
}

rootProject.name = "mapbox-android-gestures"
include(":app")
include(":library")