plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-kapt")
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.alon.filesviewer.browser.data"
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
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {

    // Project modules
    implementation(project(":features:browser:domain"))

    // Rx
    implementation(libs.rxKotlin)
    implementation(libs.rxJava)

    // Javax annotation
    implementation(libs.javaInject)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Hilt
    implementation(libs.hilt)
    kapt(libs.hiltCompiler)

    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.googleTruth)
    testImplementation(libs.junitParams)
    testImplementation(libs.roboLectric)
}

kapt {
    correctErrorTypes = true
}