plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-kapt")
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.alon.filesviewer.browser.featuretesting"
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
    implementation(project(":features:browser:data"))
    implementation(project(":features:browser:ui"))

    // Android core & ui
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Local testing
    testImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    testImplementation(libs.junit)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.espresso.core)
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("androidx.test.espresso:espresso-intents:3.3.0")
    testImplementation(libs.mockk)
    testImplementation(libs.googleTruth)
    testImplementation(libs.junitParams)
    testImplementation(libs.roboLectric)
    kaptTest(libs.dataBindingCompiler)
    testImplementation(libs.hilt)
    testImplementation(libs.hiltTest)
    kaptTest(libs.hiltCompiler)
    testImplementation(libs.greenCoffee)
    testImplementation("io.reactivex.rxjava2:rxjava:2.2.19")
    testImplementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    testImplementation("androidx.room:room-runtime:2.6.1")
    kaptTest("androidx.room:room-compiler:2.6.1")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("commons-io:commons-io:2.16.1")
}

kapt {
    correctErrorTypes = true
}