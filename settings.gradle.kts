import java.net.URI
import java.net.URL
import java.net.URLEncoder

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
        jcenter()
        maven {
            url = URI.create("https://jitpack.io")

        }
    }
}

rootProject.name = "FilesViewer"
include(":app")
include(":features:browser:featureTesting")
include(":features:browser:domain")
include(":features:browser:data")
include(":features:browser:ui")
