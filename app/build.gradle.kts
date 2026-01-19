plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}


import java.util.Properties
import java.io.FileInputStream

// Load local.properties for API key
val localPropertiesFile = rootProject.file("local.properties")
val apiKey = if (localPropertiesFile.exists()) {
    val localProperties = Properties()
    localProperties.load(FileInputStream(localPropertiesFile))
    localProperties.getProperty("GEMINI_API_KEY") 
        ?: "AIzaSyAzpCosYSFJbODADaO297SI7reAClX-yjU"
} else {
    "AIzaSyAzpCosYSFJbODADaO297SI7reAClX-yjU"
}


android {
    namespace = "com.example.healthmate"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.healthmate"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // IMPORTANT: API Key Storage
        // The API key is read from local.properties file (recommended for security)
        // If not found in local.properties, it falls back to the hardcoded value below
        // 
        // To use local.properties:
        // 1. Open local.properties in the project root
        // 2. Add: GEMINI_API_KEY=AIzaSyAzpCosYSFJbODADaO297SI7reAClX-yjU
        // 3. local.properties is already in .gitignore, so it won't be committed
        //
        // Current API key: AIzaSyAzpCosYSFJbODADaO297SI7reAClX-yjU
        

        buildConfigField("String", "GEMINI_API_KEY", "\"$apiKey\"")
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.google.generativeai)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}