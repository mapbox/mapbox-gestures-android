plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.mapbox.android.gestures.testapp"

    defaultConfig {
        minSdk =  libs.versions.minSdk.get().toInt()
        //noinspection ExpiredTargetSdkVersion
        targetSdk = libs.versions.targetSdkVersion.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(project(":library"))
    implementation(libs.androidx.appCompatV7)

    testImplementation(libs.junit)
    testImplementation(libs.mockito)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.mockitoAndroid)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.espresso.intents)
}

apply(from = "${rootDir}/gradle/checkstyle.gradle")