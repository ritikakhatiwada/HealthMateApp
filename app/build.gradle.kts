plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
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

// API Key Configuration
// For production: Set keys in local.properties (see local.properties.template)
// For development: Fallback values allow building but features may not work
val apiKey = localProperties.getProperty("GEMINI_API_KEY") ?: run {
    logger.warn("GEMINI_API_KEY not found in local.properties. Chatbot will not work.")
    ""
}
val hfApiKey = localProperties.getProperty("HF_API_KEY") ?: ""
val cloudinaryCloudName = localProperties.getProperty("CLOUDINARY_CLOUD_NAME") ?: run {
    logger.warn("CLOUDINARY_CLOUD_NAME not found. Image uploads will not work.")
    ""
}
val cloudinaryApiKey = localProperties.getProperty("CLOUDINARY_API_KEY") ?: ""
val cloudinaryApiSecret = localProperties.getProperty("CLOUDINARY_API_SECRET") ?: ""


configurations.all {
    resolutionStrategy {
        force("com.squareup:javapoet:1.13.0")
    }
}

android {
    namespace = "com.example.healthmate"

    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.healthmate"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // API Key Configuration
        // Keys are read from local.properties file (see local.properties.template)
        // local.properties is gitignored and should never be committed

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
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)

    // Google Sign-In
    implementation(libs.play.services.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // Cloudinary
    implementation(libs.cloudinary.android)

    // Coil for async image loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Location Services
    implementation(libs.play.services.location)

    // OpenStreetMap (OSMDroid) - No API key required
    implementation(libs.osmdroid.android)

    // Accompanist Permissions
    implementation(libs.accompanist.permissions)

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Charts (Vico)
    implementation(libs.vico.compose.m3)

    // OkHttp for networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
