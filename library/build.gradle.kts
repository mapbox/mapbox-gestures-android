plugins {
    id("com.android.library")
    kotlin("android")
}

apply(from = "${rootDir}/gradle/sdk-registry.gradle")
apply(from = "${rootDir}/gradle/checkstyle.gradle")

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.mapbox.android.gestures"

    defaultConfig {
        minSdk =  libs.versions.minSdk.get().toInt()
        //noinspection ExpiredTargetSdkVersion
        targetSdk = libs.versions.targetSdkVersion.get().toInt()
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.androidx.annotations)
    testImplementation(libs.junit)
    testImplementation(libs.mockito)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.mockitoAndroid)
    androidTestImplementation(libs.androidx.test.espresso.core)
}