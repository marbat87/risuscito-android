import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("com.google.firebase.crashlytics")
    kotlin("android")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.21"
    id("kotlin-parcelize")
}


android {

    compileSdk = 36
    buildToolsVersion = "36.1.0"
    defaultConfig {
        applicationId = "it.cammino.risuscito"
        minSdk = 23
        targetSdk = 36
        multiDexEnabled = true
        versionCode = 6100
        versionName = "6.1.0"

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }
    namespace = "it.cammino.risuscito"

    packaging {
        resources.excludes.add("META-INF/INDEX.LIST")
        resources.excludes.add("META-INF/DEPENDENCIES")
        resources.excludes.add("META-INF/LICENSE")
        resources.excludes.add("META-INF/LICENSE.txt")
        resources.excludes.add("META-INF/license.txt")
        resources.excludes.add("META-INF/NOTICE")
        resources.excludes.add("META-INF/NOTICE.txt")
        resources.excludes.add("META-INF/notice.txt")
        resources.excludes.add("META-INF/ASL2.0")
        resources.excludes.add("META-INF/*.kotlin_module")
    }


}

ksp {
    arg("room.incremental", "true")
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.expandProjection", "true")
    arg("room.generateKotlin", "true")
}
val roomVersion = "2.8.4"
val kotpreferences = "3.0.1"
val composepreferences = "2.0.0"
dependencies {
    implementation(files("libs/pfdjet.aar"))
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.google.android.material:material:1.14.0-alpha07")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.activity:activity-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.appcompat:appcompat-resources:1.7.1")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.media:media:1.7.1")
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.work:work-runtime-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.google.android.gms:play-services-auth:21.4.0")
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("com.leinardi.android:speed-dial:3.3.0")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.github.daniel-stoneuk:material-about-library:3.2.0-rc01")
    implementation("com.jakewharton:process-phoenix:3.0.0")
    implementation("com.google.android.play:feature-delivery-ktx:2.1.0")
    implementation("com.google.api-client:google-api-client:2.8.1")
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
    implementation("sh.calvin.reorderable:reorderable:3.0.0")
    implementation("io.github.mflisar.composechangelog:core-android:3.0.0")
    implementation("io.github.mflisar.composechangelog:statesaver-preferences-android:3.0.0") // core
    implementation("io.github.mflisar.composepreferences:core:$composepreferences") // modules
    implementation("io.github.mflisar.composepreferences:screen-bool:$composepreferences")
    implementation("io.github.mflisar.composepreferences:screen-list:${composepreferences}")
    implementation("io.github.fornewid:material-motion-compose-core:2.0.1")

    implementation(platform("androidx.compose:compose-bom:2025.11.01"))
    // Material Design 3
    implementation("androidx.compose.material3:material3:1.5.0-alpha04")
    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("com.google.accompanist:accompanist-drawablepainter:0.37.3")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.runtime:runtime:")
    implementation("androidx.navigation:navigation-compose")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation-graphics")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation")
    implementation("androidx.navigation:navigation-runtime-ktx:2.9.6")
    implementation("androidx.fragment:fragment-compose:1.8.9")
}