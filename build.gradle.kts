// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsKotlinJvm) apply false
    alias(libs.plugins.hilt) apply false
}

// Custom task that runs all local unit test for this app
tasks.register("unitTests") {
    subprojects.forEach { subproject ->
        if (subproject.plugins.findPlugin("java-library") != null) {
            dependsOn(subproject.tasks.named("test"))
        }
        if (subproject.plugins.findPlugin("com.android.library") != null && subproject.name != "featureTesting") {
            dependsOn(subproject.tasks.named("testDebugUnitTest"))
        }
    }
}

// Custom task that runs all feature/acceptance tests (integration test in scope) for this app
tasks.register("featureTests") {
    subprojects.forEach { subproject ->
        if (subproject.plugins.findPlugin("com.android.library") != null && subproject.name == "featureTesting") {
            dependsOn(subproject.tasks.named("testDebugUnitTest"))
        }
    }
}