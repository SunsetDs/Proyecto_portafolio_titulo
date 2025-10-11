plugins {
    // 1. Android Application Plugin: Se solicita solo con el alias (libs.plugins.android.application)
    alias(libs.plugins.android.application)

    // 2. Kotlin Plugin: Correcto
    alias(libs.plugins.kotlin.android)

    // 3. Google Services Plugin: Se solicita directamente con id()
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    // NOTA: Se eliminó la línea duplicada 'id("com.android.application")'
}

android {
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    namespace = "com.example.kachate"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.kachate"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Importe la Firebase BoM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0")) // <-- VERSIÓN AQUÍ

    // Las librerías ya no necesitan un número de versión si usa la BoM
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-common-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0") // para escanear códigos de barra

    // Conversor de JSON (Gson) para Retrofit
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.0")
// CameraX Core
    implementation("androidx.camera:camera-core:1.4.0-alpha01")
    implementation("androidx.camera:camera-camera2:1.4.0-alpha01")
    implementation("androidx.camera:camera-lifecycle:1.4.0-alpha01")
// CameraX View
    implementation("androidx.camera:camera-view:1.4.0-alpha01")
// ML Kit Text Recognition
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation(libs.androidx.mediarouter)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    // ... otras dependencias de su proyecto

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


}


