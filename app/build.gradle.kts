plugins {
    id("com.android.application")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
    kotlin("android")
    kotlin("kapt")
}


android {

    compileSdkVersion(30)
    buildToolsVersion("30.0.3")
    defaultConfig {
        applicationId = "it.cammino.risuscito"
        minSdkVersion(16)
        targetSdkVersion(30)
        multiDexEnabled = true
        versionCode = 4612
        versionName = "4.6.0"
        vectorDrawables.useSupportLibrary = true
        kapt {
            arguments {
                arg("room.incremental", "true")
                arg("room.schemaLocation", "$projectDir/schemas")
                arg("room.expandProjection", "true")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        useIR = true
    }

    lintOptions {
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

    buildFeatures {
        viewBinding = true
    }

}

val preferenceFixVersion = "1.1.0"
val fastAdapterVersion = "5.4.1"
val materialDrawerVersion = "8.4.0"
val materialDialogsVersion = "3.3.0"
val roomVersion = "2.3.0"
val iconicVersion = "5.2.8"
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.32")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.google.android.play:core:1.10.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.activity:activity-ktx:1.2.2")
    implementation("androidx.appcompat:appcompat:1.3.0-rc01")
    implementation("androidx.appcompat:appcompat-resources:1.3.0-rc01")
    implementation("androidx.core:core-ktx:1.5.0-rc01")
    implementation("androidx.fragment:fragment-ktx:1.3.3")
    implementation("androidx.recyclerview:recyclerview:1.2.0")
    implementation("androidx.slidingpanelayout:slidingpanelayout:1.1.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0-alpha01")
    implementation("androidx.media:media:1.3.0")
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("com.takisoft.preferencex:preferencex:$preferenceFixVersion")
    implementation("com.takisoft.preferencex:preferencex-simplemenu:$preferenceFixVersion")
    implementation("com.takisoft.preferencex:preferencex-colorpicker:$preferenceFixVersion")
    implementation("com.google.android.gms:play-services-auth:19.0.0")
    implementation("com.google.firebase:firebase-core:18.0.3")
    implementation("com.google.firebase:firebase-firestore-ktx:22.1.2")
    implementation("com.google.firebase:firebase-storage-ktx:19.2.2")
    implementation("com.google.firebase:firebase-auth:20.0.4")
    implementation("com.google.firebase:firebase-crashlytics:17.4.1")
    implementation("com.google.firebase:firebase-analytics:18.0.3")
    implementation("com.afollestad.material-dialogs:core:$materialDialogsVersion")
    implementation("com.afollestad.material-dialogs:input:$materialDialogsVersion")
    implementation("com.afollestad.material-dialogs:files:$materialDialogsVersion")
    implementation("com.mikepenz:materialdrawer:$materialDrawerVersion")
    implementation("com.mikepenz:materialdrawer-iconics:$materialDrawerVersion")
    implementation("com.mikepenz:crossfader:1.6.0")
    implementation("com.mikepenz:iconics-core:$iconicVersion")
    implementation("com.mikepenz:iconics-views:$iconicVersion")
    implementation("com.mikepenz:community-material-typeface:5.8.55.0-kotlin@aar")
    implementation("com.mikepenz:itemanimators:1.1.0")
    implementation("com.github.jrvansuita:MaterialAbout:0.2.5")
    implementation("com.getkeepsafe.taptargetview:taptargetview:1.13.2")
    implementation("com.mikepenz:fastadapter:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-drag:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-swipe:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-expandable:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-ui:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-binding:$fastAdapterVersion")
    implementation("com.mikepenz:fastadapter-extensions-utils:$fastAdapterVersion")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("pub.devrel:easypermissions:3.0.0")
    implementation("it.marbat.pdfjet.lib:lib:1.0.0")
    implementation("com.github.turing-tech:MaterialScrollBar:13.3.4")
    implementation("com.leinardi.android:speed-dial:3.1.1")
    implementation("com.github.mohammadatif:Animatoo:master")
    implementation("com.github.Ferfalk:SimpleSearchView:0.2.0")
    implementation("com.github.MFlisar:changelog:1.1.7")
    implementation("com.github.stupacki:MultiFunctions:1.2.2")
    implementation("com.wada811:android-material-design-colors:3.0.0")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
}
