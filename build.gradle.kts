// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")
        classpath("com.google.gms:google-services:4.3.5")
        classpath(kotlin("gradle-plugin", version = "1.4.32"))
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.5.2")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven(url = "https://dl.bintray.com/mikepenz/maven")
        maven(url = "https://jitpack.io")
        maven(url = "https://github.com/wada811/Android-Material-Design-Colors/raw/master/repository/")

    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}