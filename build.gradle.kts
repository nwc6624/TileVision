// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    //trick: for the ability to change versions via catalogs
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
}
