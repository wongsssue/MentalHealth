plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.mentalhealthemotion"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mentalhealthemotion"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            //excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/NOTICE.md"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.play.services.nearby)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Added dependencies
    implementation("androidx.compose.ui:ui:1.5.1")
    implementation("androidx.compose.material:material:1.5.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.1")
    implementation("androidx.navigation:navigation-compose:2.7.3")
    implementation("androidx.compose.foundation:foundation:1.5.1")
    implementation ("com.google.android.gms:play-services-auth:20.6.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0") // For API calls
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Gson converter

    // Jetpack Compose dependencies
    implementation ("androidx.compose.runtime:runtime-livedata:1.5.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.0") // To observe LiveData in Compose

    // Kotlin coroutine dependencies
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // or latest version

    implementation ("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")
    implementation ("io.ktor:ktor-client-core:2.3.1")
    implementation ("io.ktor:ktor-client-okhttp:2.3.1")
    implementation ("io.ktor:ktor-client-serialization:2.3.1")
    implementation ("io.ktor:ktor-client-content-negotiation:2.3.1")
    implementation ("io.ktor:ktor-serialization-kotlinx-json:2.3.1")

    // Room dependencies
    implementation ("androidx.room:room-runtime:2.5.0")
    implementation ("androidx.room:room-ktx:2.5.0")
    ksp ("androidx.room:room-compiler:2.5.0")

    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore-ktx")  // Firestore SDK

    //for profile picture update
    implementation("io.coil-kt:coil-compose:2.4.0")


    //for email verification (code)
    implementation ("com.sun.mail:android-mail:1.6.7")
    implementation ("com.sun.mail:android-activation:1.6.7")

    //Audio Recording
    implementation ("com.google.accompanist:accompanist-permissions:0.28.0")

    //Chart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    //YouTube Android Player API for Video Playback
    implementation ("com.squareup.okhttp3:okhttp:4.9.3") // HTTP Client for API requests
    implementation ("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")

}
