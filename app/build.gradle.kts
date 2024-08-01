plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.alon.filesviewer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.alon.filesviewer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.alon.filesviewer.runner.CustomAndroidTestRunner"
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
    buildFeatures {
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Project modules
    implementation(project(":features:browser:data"))
    implementation(project(":features:browser::domain"))
    implementation(project(":features:browser:ui"))

    // Hilt
    implementation(libs.hilt)
    kapt(libs.hiltCompiler)

    // Rx
    implementation("io.reactivex.rxjava2:rxjava:2.2.19")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")

    debugImplementation("androidx.tracing:tracing:1.1.0")

    // Instrumentation testing
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("androidx.test:runner:1.1.0")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.3.0")
    androidTestImplementation(libs.googleTruth)
    androidTestImplementation(libs.junitParams)
    kaptAndroidTest(libs.dataBindingCompiler)
    androidTestImplementation(libs.hilt)
    androidTestImplementation(libs.hiltTest)
    kaptAndroidTest(libs.hiltCompiler)
    androidTestImplementation(libs.greenCoffee)
}