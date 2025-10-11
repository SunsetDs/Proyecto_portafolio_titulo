// build.gradle.kts (NIVEL DE PROYECTO)

buildscript {
    repositories {
        google() // Añadir aquí para asegurar que el plugin de Android funcione
        mavenCentral()
    }
    dependencies {
        // Asegura la compatibilidad del plugin de Android
        // Asegúrate de que esta línea refleje la versión de tu proyecto si tienes un error aquí
        classpath("com.android.tools.build:gradle:8.4.1")
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin")
    }
}

plugins {
    // Define el plugin de Android Application (usado por el módulo app)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Añade la dependencia de la clase del plugin de Google Services
    id("com.google.gms.google-services") version "4.4.3" apply false
}
