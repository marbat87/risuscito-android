plugins {
    id("com.android.application")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-parcelize")
}


android {

    compileSdk = 37
    buildToolsVersion = "37.0.0"
    defaultConfig {
        applicationId = "it.cammino.risuscito"
        minSdk = 24
        targetSdk = 37
        multiDexEnabled = true
        versionCode = 6104
        versionName = "6.1.0"

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
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

kotlin {
    compilerOptions {
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0
    }
}

ksp {
    arg("room.incremental", "true")
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.expandProjection", "true")
    arg("room.generateKotlin", "true")
}
val roomVersion = "2.8.4"
val composepreferences = "3.0.0"
dependencies {
    implementation(files("libs/pfdjet.aar"))
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.activity:activity-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.appcompat:appcompat-resources:1.7.1")
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.media3:media3-exoplayer:1.10.1")
    implementation("androidx.media3:media3-session:1.10.1")
    implementation("androidx.media3:media3-common:1.10.1")
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.work:work-runtime-ktx:2.11.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.11.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    //noinspection LoginCredentials
    implementation("com.google.android.gms:play-services-auth:21.6.0")
    //noinspection LoginCredentials
    implementation("androidx.credentials:credentials:1.6.0")
    //noinspection LoginCredentials
    implementation("androidx.credentials:credentials-play-services-auth:1.6.0")
    //noinspection LoginCredentials
    implementation("com.google.android.libraries.identity.googleid:googleid:1.2.0")
    implementation(platform("com.google.firebase:firebase-bom:34.15.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    //noinspection LoginCredentials
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.code.gson:gson:2.14.0")
    implementation("com.leinardi.android:speed-dial:3.3.0")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.jakewharton:process-phoenix:3.0.0")
    implementation("com.google.android.play:feature-delivery-ktx:2.1.0")
    // ci sono conflitti di versioni dalla 2.5.0 in poi, in alcune librerie http - ce ne si accorge facendo il ripristino delle preferences
    //noinspection NewerVersionAvailable
    implementation("com.google.api-client:google-api-client:2.4.0")
    implementation("io.coil-kt.coil3:coil-compose:3.5.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.5.0")
    implementation("sh.calvin.reorderable:reorderable:3.1.0")
    implementation("io.github.mflisar.composechangelog:core-android:4.0.0")
    implementation("io.github.mflisar.composechangelog:statesaver-preferences-android:4.0.0") // core
    implementation("io.github.mflisar.composepreferences:core:$composepreferences") // modules
    implementation("io.github.mflisar.composepreferences:screen-bool:$composepreferences")
    implementation("io.github.mflisar.composepreferences:screen-list:${composepreferences}")
    implementation("io.github.fornewid:material-motion-compose-core:2.0.1")

    implementation(platform("androidx.compose:compose-bom:2026.06.00"))
    // Material Design 3
    implementation("androidx.compose.material3:material3:1.5.0-alpha22")
    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-text-google-fonts")
    implementation("com.google.accompanist:accompanist-drawablepainter:0.37.3")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.navigation:navigation-compose")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation-graphics")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation")
    implementation("androidx.navigation:navigation-runtime-ktx:2.9.8")
    implementation("androidx.fragment:fragment-compose:1.8.9")
}