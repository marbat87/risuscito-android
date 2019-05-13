import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

plugins {
    id("com.android.application")
    id("io.fabric")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}


android {

    compileSdkVersion(28)
    buildToolsVersion("28.0.3")
    defaultConfig {
        applicationId = "it.cammino.risuscito"
        minSdkVersion(16)
        targetSdkVersion(28)
        multiDexEnabled = true
        versionCode = 4408
        versionName = "4.4.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        setTargetCompatibility(JavaVersion.VERSION_1_8)
    }

    (kotlinOptions as KotlinJvmOptions).apply {
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
        exclude("META-INF/atomicfu.kotlin_module")
    }
}

val supportVersion = "1.0.0"
val preferenceFixVersion = "1.0.0"
val fastAdapterVersion = "4.0.0-rc04"
val materialDialogsVersion = "3.0.0-alpha1"
dependencies {
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.afollestad.material-dialogs:core:$materialDialogsVersion")
    implementation("com.afollestad.material-dialogs:input:$materialDialogsVersion")
    implementation("com.afollestad.material-dialogs:files:$materialDialogsVersion")
    implementation("me.zhanghai.android.materialprogressbar:library:1.6.1")
    implementation("com.mikepenz:materialdrawer:7.0.0-beta01")
    implementation("com.mikepenz:crossfader:1.6.0")
    implementation ("com.mikepenz:iconics-core:4.0.0-rc01")
    implementation("com.mikepenz:community-material-typeface:3.5.95.1-kotlin@aar")
    implementation("com.mikepenz:itemanimators:1.1.0")
    implementation("com.google.android.material:material:$supportVersion")
    implementation("androidx.legacy:legacy-support-v4:$supportVersion")
    implementation("androidx.media:media:$supportVersion")
    implementation("androidx.exifinterface:exifinterface:$supportVersion")
    implementation("androidx.cardview:cardview:$supportVersion")
    implementation("androidx.preference:preference:$supportVersion")
    implementation("com.takisoft.preferencex:preferencex:$preferenceFixVersion")
    implementation("com.takisoft.preferencex:preferencex-simplemenu:$preferenceFixVersion")
    implementation("com.takisoft.preferencex:preferencex-colorpicker:$preferenceFixVersion")
    implementation("com.google.android.gms:play-services-auth:16.0.1")
    implementation("com.google.firebase:firebase-core:16.0.9")
    implementation ("com.google.firebase:firebase-firestore-ktx:19.0.0")
    implementation ("com.google.firebase:firebase-storage:17.0.0")
    implementation ("com.google.firebase:firebase-auth:17.0.0")
    implementation("com.crashlytics.sdk.android:crashlytics:2.9.9@aar") { isTransitive = true }
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
    implementation("androidx.room:room-runtime:2.0.0")
    kapt("androidx.room:room-compiler:2.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.0.0")
    implementation("it.marbat.pdfjet.lib:lib:1.0.0")
    implementation("androidx.core:core-ktx:1.0.2")
    implementation("androidx.fragment:fragment-ktx:1.0.0")
    implementation("com.github.turing-tech:MaterialScrollBar:13.3.2")
    implementation("com.leinardi.android:speed-dial:2.0.1")
    implementation("com.github.mohammadatif:Animatoo:master")
    implementation ("com.github.Ferfalk:SimpleSearchView:0.1.3")
    implementation ("com.github.MFlisar:changelog:1.1.5")
    implementation ("com.github.zawadz88.materialpopupmenu:material-popup-menu:3.4.0")
}
