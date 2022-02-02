plugins {
    id("com.android.application")
    id("com.google.firebase.crashlytics")
    kotlin("android")
    kotlin("kapt")
    id("com.google.gms.google-services")
}


android {

    compileSdk = 32
    buildToolsVersion = "32.0.0"
    defaultConfig {
        applicationId = "it.cammino.risuscito"
        minSdk = 21
        targetSdk = 32
        multiDexEnabled = true
        versionCode = 5004
        versionName = "5.0.0"
        kapt {
            arguments {
                arg("room.incremental", "true")
                arg("room.schemaLocation", "$projectDir/schemas")
                arg("room.expandProjection", "true")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
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

//    dexOptions {
//        javaMaxHeapSize = "2g"
//        jumboMode = true
//    }

    buildFeatures {
        viewBinding = true
    }

}

val preferenceFixVersion = "1.1.0"
val fastAdapterVersion = "5.6.0"
val roomVersion = "2.4.1"
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.10")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.google.android.play:core:1.10.3")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.appcompat:appcompat-resources:1.4.1")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.fragment:fragment-ktx:1.4.1")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.slidingpanelayout:slidingpanelayout:1.2.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0-beta01")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.media:media:1.5.0-rc01")
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.work:work-runtime-ktx:2.7.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0-RC3")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("com.google.android.gms:play-services-auth:20.0.1")
    implementation(platform("com.google.firebase:firebase-bom:29.0.3"))
    implementation("com.google.firebase:firebase-core")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.mikepenz:itemanimators:1.1.0")
    implementation("com.github.jrvansuita:MaterialAbout:0.2.6")
    implementation("com.getkeepsafe.taptargetview:taptargetview:1.13.3")
    implementation("com.mikepenz:fastadapter:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-drag:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-swipe:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-expandable:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-ui:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-binding:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-utils:$fastAdapterVersion")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("it.marbat.pdfjet.lib:lib:1.0.0")
    implementation("com.github.turing-tech:MaterialScrollBar:13.3.4")
    implementation("com.leinardi.android:speed-dial:3.2.0")
    implementation("com.github.Ferfalk:SimpleSearchView:0.2.0")
    implementation("com.github.MFlisar:changelog:1.1.7")
    implementation("com.wada811:android-material-design-colors:3.0.0")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
}