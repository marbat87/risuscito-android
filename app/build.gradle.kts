import org.jetbrains.kotlin.config.KotlinCompilerVersion

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
        versionCode = 4376
        versionName = "4.3.2"
    }

    compileOptions {
        setSourceCompatibility(JavaVersion.VERSION_1_8)
        setTargetCompatibility(JavaVersion.VERSION_1_8)
    }

    lintOptions {
        isAbortOnError = false
        disable("PrivateResource")
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
}

val supportVersion = "1.0.0"
val preferenceFixVersion = "1.0.0"
val lifecycleVersion = "1.1.1"
val roomVersion = "1.1.1"
dependencies {
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.github.gabrielemariotti.changeloglib:changelog:2.1.0")
    implementation("com.afollestad.material-dialogs:core:2.0.0-rc7")
    implementation("com.afollestad.material-dialogs:input:2.0.0-rc7")
    implementation("com.afollestad.material-dialogs:files:2.0.0-rc7")
    implementation("me.zhanghai.android.materialprogressbar:library:1.6.1")
    implementation("com.mikepenz:materialdrawer:6.1.2")
    implementation("com.mikepenz:crossfader:1.6.0")
    implementation("com.mikepenz:community-material-typeface:2.7.94.1@aar")
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
    implementation("com.google.android.gms:play-services-drive:16.0.0")
    implementation("com.google.firebase:firebase-core:16.0.6")
    implementation("com.crashlytics.sdk.android:crashlytics:2.9.8@aar") { isTransitive = true }
    implementation("com.afollestad:material-cab:1.3.0")
    implementation("com.github.jrvansuita:MaterialAbout:0.2.3")
    implementation("com.getkeepsafe.taptargetview:taptargetview:1.12.0")
    implementation("com.mikepenz:fastadapter:3.3.1")
    implementation("com.mikepenz:fastadapter-commons:3.3.1")
    implementation("com.mikepenz:fastadapter-extensions:3.3.1")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("pub.devrel:easypermissions:2.0.1")
    implementation("androidx.room:room-runtime:2.0.0")
    kapt("androidx.room:room-compiler:2.0.0")
    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-extensions:2.0.0")
    implementation("it.marbat.pdfjet.lib:lib:1.0.0")
    implementation("androidx.core:core-ktx:1.0.1")
    implementation("androidx.fragment:fragment-ktx:1.0.0")
    implementation("com.github.turing-tech:MaterialScrollBar:13.2.5")
    implementation("com.leinardi.android:speed-dial:2.0.1")
    implementation("com.github.mohammadatif:Animatoo:master")
    implementation ("com.github.Ferfalk:SimpleSearchView:0.1.3")
}
