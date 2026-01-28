plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}


import java.util.Properties
import java.io.FileInputStream

// Load local.properties for API key
val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

val apiKey = localProperties.getProperty("GEMINI_API_KEY") ?: "AIzaSyAzpCosYSFJbODADaO297SI7reAClX-yjU"
val hfApiKey = localProperties.getProperty("HF_API_KEY") ?: ""
val cloudinaryCloudName = localProperties.getProperty("CLOUDINARY_CLOUD_NAME") ?: ""
val cloudinaryApiKey = localProperties.getProperty("CLOUDINARY_API_KEY") ?: ""
val cloudinaryApiSecret = localProperties.getProperty("CLOUDINARY_API_SECRET") ?: ""


android {
    namespace = "com.example.healthmate"

    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.healthmate"
        minSdk = 24
        targetSdk = 35
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
        buildConfigField("String", "HF_API_KEY", "\"$hfApiKey\"")
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"$cloudinaryCloudName\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"$cloudinaryApiKey\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"$cloudinaryApiSecret\"")
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
    implementation(libs.androidx.navigation.compose)

    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    
    // Cloudinary
    implementation(libs.cloudinary.android)

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}