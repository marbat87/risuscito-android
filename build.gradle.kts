// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        jcenter()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.0")
        classpath("com.google.gms:google-services:4.3.4")
        classpath(kotlin("gradle-plugin", version = "1.4.10"))
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.3.0")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        jcenter()
        maven(url = "https://dl.bintray.com/mikepenz/maven")
        maven(url = "https://jitpack.io")
        maven(url = "http://github.com/wada811/Android-Material-Design-Colors/raw/master/repository/")
        google()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}