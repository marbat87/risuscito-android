plugins {
    id("com.android.application")
    id("io.fabric")
    id("com.google.gms.google-services")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}


android {

    compileSdkVersion(29)
    buildToolsVersion("29.0.2")
    defaultConfig {
        applicationId = "it.cammino.risuscito"
        minSdkVersion(16)
        targetSdkVersion(29)
        multiDexEnabled = true
        versionCode = 4420
        versionName = "4.4.0"
    }

//    sourceSets {
//        val main by getting
//        main.java.srcDirs("src/main/kotlin")
//    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    lintOptions {
        isAbortOnError = false
        disable("PrivateResource")
        disable("MissingTranslation")
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

    dexOptions {
        javaMaxHeapSize = "2g"
        jumboMode = true
    }

    packagingOptions {
        exclude("META-INF/library-core_release.kotlin_module")
    }

}

val preferenceFixVersion = "1.1.0-alpha05"
val fastAdapterVersion = "4.1.0-b01"
val materialDialogsVersion = "3.1.0"
val roomVersion = "2.1.0"
dependencies {
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.afollestad.material-dialogs:core:$materialDialogsVersion")
    implementation("com.afollestad.material-dialogs:input:$materialDialogsVersion")
    implementation("com.afollestad.material-dialogs:files:$materialDialogsVersion")
    implementation("me.zhanghai.android.materialprogressbar:library:1.6.1")
    implementation("com.mikepenz:materialdrawer:7.0.0-rc05")
    implementation("com.mikepenz:crossfader:1.6.0")
    implementation("com.mikepenz:iconics-core:4.0.1-b01")
    implementation("com.mikepenz:community-material-typeface:3.5.95.1-kotlin@aar")
    implementation("com.mikepenz:itemanimators:1.1.0")
    implementation("com.google.android.material:material:1.1.0-alpha09")
    implementation("androidx.viewpager2:viewpager2:1.0.0-beta03")
    implementation("androidx.drawerlayout:drawerlayout:1.1.0-alpha03")
    implementation("androidx.recyclerview:recyclerview:1.1.0-beta03")
    implementation("androidx.slidingpanelayout:slidingpanelayout:1.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.0.0")
    implementation("androidx.media:media:1.1.0-rc01")
    implementation("androidx.preference:preference:1.1.0-rc01")
    implementation("com.takisoft.preferencex:preferencex:$preferenceFixVersion")
    implementation("com.takisoft.preferencex:preferencex-simplemenu:$preferenceFixVersion")
    implementation("com.takisoft.preferencex:preferencex-colorpicker:$preferenceFixVersion")
    implementation("com.google.android.gms:play-services-auth:17.0.0")
    implementation("com.google.firebase:firebase-core:17.1.0")
    implementation("com.google.firebase:firebase-firestore-ktx:21.0.0")
    implementation("com.google.firebase:firebase-storage:19.0.0")
    implementation("com.google.firebase:firebase-auth:19.0.0")
    implementation("com.crashlytics.sdk.android:crashlytics:2.10.1@aar") { isTransitive = true }
    implementation("com.afollestad:material-cab:1.3.1")
    implementation("com.github.jrvansuita:MaterialAbout:0.2.3")
    implementation("com.getkeepsafe.taptargetview:taptargetview:1.12.0")
    implementation("com.mikepenz:fastadapter:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-drag:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-swipe:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-expandable:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-ui:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-utils:$fastAdapterVersion")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("pub.devrel:easypermissions:3.0.0")
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0-alpha03")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0-alpha03")
    implementation("it.marbat.pdfjet.lib:lib:1.0.0")
    implementation("androidx.activity:activity-ktx:1.1.0-alpha02")
    implementation("androidx.core:core-ktx:1.2.0-alpha03")
    implementation("androidx.fragment:fragment-ktx:1.2.0-alpha02")
    implementation("com.github.turing-tech:MaterialScrollBar:13.3.2")
    implementation("com.leinardi.android:speed-dial:3.0.0")
    implementation("com.github.mohammadatif:Animatoo:master")
    implementation("com.github.Ferfalk:SimpleSearchView:0.1.3")
    implementation("com.github.MFlisar:changelog:1.1.6")
    implementation("com.github.zawadz88.materialpopupmenu:material-popup-menu:3.4.0")
    implementation("com.github.stupacki:MultiFunctions:1.2.1")
}
