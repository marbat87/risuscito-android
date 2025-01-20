plugins {
    id("com.android.application")
    id("com.google.firebase.crashlytics")
    kotlin("android")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
}


android {

    compileSdk = 35
    buildToolsVersion = "35.0.0"
    defaultConfig {
        applicationId = "it.cammino.risuscito"
        minSdk = 23
        targetSdk = 35
        multiDexEnabled = true
        versionCode = 6001
        versionName = "6.0.0"
        ksp {
            arg("room.incremental", "true")
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.expandProjection", "true")
            arg("room.generateKotlin", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
    }
    namespace = "it.cammino.risuscito"

}

val fastAdapterVersion = "5.7.0"
val roomVersion = "2.6.1"
dependencies {
    implementation(files("libs/pfdjet.aar"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.0")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.activity:activity-ktx:1.10.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.appcompat:appcompat-resources:1.7.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.slidingpanelayout:slidingpanelayout:1.2.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.media:media:1.7.0")
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.mikepenz:itemanimators:1.1.0")
    implementation("com.getkeepsafe.taptargetview:taptargetview:1.13.3")
    implementation("com.mikepenz:fastadapter:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-drag:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-swipe:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-expandable:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-ui:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-binding:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-utils:$fastAdapterVersion")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.leinardi.android:speed-dial:3.3.0")
    implementation("com.github.MFlisar:changelog:1.1.7")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.github.daniel-stoneuk:material-about-library:3.2.0-rc01")
    implementation("com.jakewharton:process-phoenix:3.0.0")
    implementation("com.google.android.play:feature-delivery-ktx:2.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}