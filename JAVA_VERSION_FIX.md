# Java Version Compatibility Issue Fix

## Problem
You're encountering a build error: `java.lang.IllegalArgumentException: 25.0.1`

This happens because you have **Java 25.0.1** installed, but the Kotlin compiler in Gradle doesn't recognize this version format yet.

## Solution

You have two options:

### Option 1: Use Java 17 or 21 (Recommended)

1. **Download Java 17 or 21 LTS:**
   - Java 17: https://adoptium.net/temurin/releases/?version=17
   - Java 21: https://adoptium.net/temurin/releases/?version=21

2. **Install Java 17 or 21**

3. **Configure Android Studio to use Java 17/21:**
   - Open Android Studio
   - Go to **File → Settings → Build, Execution, Deployment → Build Tools → Gradle**
   - Under **Gradle JDK**, select Java 17 or 21
   - Click **Apply** and **OK**

4. **Or set JAVA_HOME environment variable:**
   ```powershell
   # For PowerShell (temporary):
   $env:JAVA_HOME="C:\Program Files\Java\jdk-17"
   
   # Or add to System Environment Variables permanently
   ```

### Option 2: Wait for Kotlin/Gradle Updates

Kotlin and Gradle will eventually support Java 25, but for now, Java 17 or 21 is the recommended approach for Android development.

## Verify Your Java Version

After switching, verify with:
```powershell
java -version
```

You should see Java 17 or 21, not Java 25.

## Why This Happens

The Kotlin compiler's `JavaVersion.parse()` method doesn't recognize the "25.0.1" version format. This is a known compatibility issue with newer Java versions.

## Note

This issue is **not related to the chatbot implementation**. The chatbot code is correct; this is purely a Java version compatibility issue with the build system.


