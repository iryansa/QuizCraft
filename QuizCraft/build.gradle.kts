// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    alias(libs.plugins.google.gms.google.services) apply false
//    alias(libs.plugins.android.application) apply false
//    alias(libs.plugins.kotlin.android) apply false
//    alias(libs.plugins.google.gms.google.services) apply false
}