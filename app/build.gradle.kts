plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.taller2movil"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.taller2movil"
        minSdk = 26
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.camara.core)
    implementation(libs.androidx.camara.camera2)
    implementation(libs.androidx.camara.lifecycle)
    implementation(libs.androidx.camara.view)
    implementation(libs.mapa.compose)
    implementation(libs.servicios.mapa)
    implementation(libs.acompanante.permisos)
    implementation(libs.servicios.ubicacion)
    implementation(libs.coil.compose)
    implementation(libs.androidx.interfaz.exif)
    debugImplementation(libs.androidx.ui.tooling)
}