plugins {
    id("com.android.application")
    id("io.fabric")
    id("com.google.gms.google-services")
    kotlin("android")
    kotlin("kapt")
}


android {

    compileSdkVersion(29)
    buildToolsVersion("30.0.0-rc1")
    defaultConfig {
        applicationId = "it.cammino.risuscito"
        minSdkVersion(16)
        targetSdkVersion(29)
        multiDexEnabled = true
        versionCode = 4501
        versionName = "4.5.0"
        vectorDrawables.useSupportLibrary = true
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = mapOf(
                        "room.schemaLocation" to "$projectDir/schemas",
                        "room.incremental" to "true",
                        "room.expandProjection" to "true")
            }
        }
        viewBinding {
            isEnabled = true
        }
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

val preferenceFixVersion = "1.1.0"
val fastAdapterVersion = "5.0.0"
val materialDrawerVersion = "8.0.0"
val materialDialogsVersion = "3.3.0"
val roomVersion = "2.2.5"
val iconicVersion = "5.0.1"
dependencies {
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.google.android.play:core:1.7.1")
    implementation("androidx.activity:activity-ktx:1.1.0")
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.core:core-ktx:1.2.0")
    implementation("androidx.fragment:fragment-ktx:1.2.3")
    implementation("com.google.android.material:material:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("androidx.slidingpanelayout:slidingpanelayout:1.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0-beta01")
    implementation("androidx.media:media:1.2.0-alpha01")
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("androidx.preference:preference-ktx:1.1.0")
    implementation("com.takisoft.preferencex:preferencex:$preferenceFixVersion")
    implementation("com.takisoft.preferencex:preferencex-simplemenu:$preferenceFixVersion")
    implementation("com.takisoft.preferencex:preferencex-colorpicker:$preferenceFixVersion")
    implementation("com.google.android.gms:play-services-auth:17.0.0")
    implementation("com.google.firebase:firebase-core:17.2.3")
    implementation("com.google.firebase:firebase-firestore-ktx:21.4.1")
    implementation("com.google.firebase:firebase-storage-ktx:19.1.1")
    implementation("com.google.firebase:firebase-auth:19.3.0")
    implementation("com.crashlytics.sdk.android:crashlytics:2.10.1@aar") { isTransitive = true }
    implementation("com.afollestad.material-dialogs:core:$materialDialogsVersion")
    implementation("com.afollestad.material-dialogs:input:$materialDialogsVersion")
    implementation("com.afollestad.material-dialogs:files:$materialDialogsVersion")
    implementation("me.zhanghai.android.materialprogressbar:library:1.6.1")
    implementation("com.mikepenz:materialdrawer:$materialDrawerVersion")
    implementation("com.mikepenz:materialdrawer-iconics:$materialDrawerVersion")
    implementation("com.mikepenz:crossfader:1.6.0")
    implementation("com.mikepenz:iconics-core:$iconicVersion")
    implementation("com.mikepenz:iconics-views:$iconicVersion")
    implementation("com.mikepenz:community-material-typeface:5.0.45.1-kotlin@aar")
    implementation("com.mikepenz:itemanimators:1.1.0")
    implementation("com.afollestad:material-cab:1.3.1")
    implementation("com.github.jrvansuita:MaterialAbout:0.2.3")
    implementation("com.getkeepsafe.taptargetview:taptargetview:1.13.0")
    implementation("com.mikepenz:fastadapter:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-drag:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-swipe:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-expandable:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-ui:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-binding:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-utils:$fastAdapterVersion")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("pub.devrel:easypermissions:3.0.0")
    implementation("it.marbat.pdfjet.lib:lib:1.0.0")
    implementation("com.github.turing-tech:MaterialScrollBar:13.3.2")
    implementation("com.leinardi.android:speed-dial:3.1.1")
    implementation("com.github.mohammadatif:Animatoo:master")
    implementation("com.github.Ferfalk:SimpleSearchView:0.1.5")
    implementation("com.github.MFlisar:changelog:1.1.7")
    implementation("com.github.stupacki:MultiFunctions:1.2.2")
    implementation("com.wada811:android-material-design-colors:3.0.0")
}
