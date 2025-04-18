plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-kapt")
}

android {
    namespace = "com.alon.filesviewer.settings.ui"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    testOptions {
        unitTests {
            this.isIncludeAndroidResources = true
        }
    }
}

dependencies {

    // Project modules
    implementation(project(":features:common:ui"))
    implementation(project(":features:common:messaging"))

    // Android core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // AndroidX preferences
    implementation(libs.preferenceKtx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Fragment testing
    debugImplementation("androidx.fragment:fragment-testing-manifest:1.8.6")

    // Local tests
    testImplementation(libs.androidx.espresso.contrib)
    testImplementation(libs.androidx.espresso.intents)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.espresso.core)
    testImplementation(libs.androidx.testing)
    testImplementation(libs.mockk)
    testImplementation(libs.googleTruth)
    testImplementation(libs.junitParams)
    testImplementation(libs.roboLectric)
    kaptTest(libs.dataBindingCompiler)
    testImplementation(libs.hiltTest)
    kaptTest(libs.hiltCompiler)
    testImplementation("androidx.fragment:fragment-testing:1.8.6")
}