plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-kapt")
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.alon.filesviewer.browser.ui"
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
    buildFeatures {
        dataBinding = true
    }
    testOptions {
        unitTests {
            this.isIncludeAndroidResources = true
        }
    }
}

dependencies {

    // Project modules
    implementation(project(":features:browser:domain"))

    // Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.activityKtx)

    // Rx
    implementation(libs.rxKotlin)
    implementation(libs.rxJava)
    implementation(libs.rxAndroid)

    // Hilt
    implementation(libs.hilt)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    kapt(libs.hiltCompiler)

    // Local testing
    testImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    testImplementation(libs.junit)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.espresso.core)
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation(libs.mockk)
    testImplementation(libs.googleTruth)
    testImplementation(libs.junitParams)
    testImplementation(libs.roboLectric)
    kaptTest(libs.dataBindingCompiler)
    testImplementation(libs.hiltTest)
    kaptTest(libs.hiltCompiler)
}

kapt {
    correctErrorTypes = true
}