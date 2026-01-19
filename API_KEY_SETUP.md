# API Key Setup Instructions

## ⚠️ IMPORTANT: Secure API Key Storage

Your Gemini API key is currently stored in `app/build.gradle.kts`. For better security, especially when committing to version control, consider the following options:

## Option 1: Using local.properties (Recommended for Development)

1. Open `local.properties` file in the project root
2. Add your API key:
   ```
   GEMINI_API_KEY=AIzaSyAzpCosYSFJbODADaO297SI7reAClX-yjU
   ```

3. Update `app/build.gradle.kts` to read from local.properties:
   ```kotlin
   val localProperties = java.util.Properties()
   val localPropertiesFile = rootProject.file("local.properties")
   if (localPropertiesFile.exists()) {
       localProperties.load(java.io.FileInputStream(localPropertiesFile))
   }
   
   defaultConfig {
       // ... other config
       buildConfigField(
           "String", 
           "GEMINI_API_KEY", 
           "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\""
       )
   }
   ```

## Option 2: Using Environment Variables (Recommended for CI/CD)

Set environment variable:
```bash
export GEMINI_API_KEY=AIzaSyAzpCosYSFJbODADaO297SI7reAClX-yjU
```

Then in `build.gradle.kts`:
```kotlin
buildConfigField(
    "String", 
    "GEMINI_API_KEY", 
    "\"${System.getenv("GEMINI_API_KEY") ?: ""}\""
)
```

## Option 3: Keep in BuildConfig (Current Setup)

The API key is currently in `app/build.gradle.kts` in the `defaultConfig` block. 
**Note:** This file should NOT be committed to version control if it contains your API key.

## Current API Key

Your current API key: `AIzaSyAzpCosYSFJbODADaO297SI7reAClX-yjU`

**Remember:**
- Never commit API keys to public repositories
- Add `local.properties` to `.gitignore` if using Option 1
- Rotate your API key if it's ever exposed publicly

