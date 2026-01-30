package com.example.healthmate.util

import com.example.healthmate.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * Cloudinary Helper for HealthMate
 *
 * Provides optimized image URLs for doctor/user avatars and medical illustrations.
 * Uses Cloudinary transformations for:
 * - Face detection cropping
 * - Automatic format selection (WebP fallback)
 * - Quality optimization
 * - Responsive sizing
 */
object CloudinaryHelper {
    private val CLOUD_NAME = BuildConfig.CLOUDINARY_CLOUD_NAME

    /**
     * Get optimized doctor/user avatar URL
     *
     * Transformations:
     * - c_fill: Fill mode for perfect square
     * - w_200,h_200: 200x200px size
     * - g_face: Face detection for smart cropping
     * - f_auto: Automatic format (WebP for supported devices, fallback to JPEG)
     * - q_80: 80% quality (optimal for photos)
     *
     * @param publicId Cloudinary public ID (e.g., "doctors/dr_john_smith")
     * @return Optimized image URL
     */
    fun getDoctorAvatar(publicId: String): String {
        return if (CLOUD_NAME.isNotEmpty()) {
            "https://res.cloudinary.com/$CLOUD_NAME/image/upload/c_fill,w_200,h_200,g_face,f_auto,q_80/$publicId.jpg"
        } else {
            // Fallback when Cloudinary not configured
            ""
        }
    }

    /**
     * Get optimized user avatar URL
     * Same as doctor avatar - circular profile photos
     *
     * @param publicId Cloudinary public ID (e.g., "users/user_12345")
     * @return Optimized image URL
     */
    fun getUserAvatar(publicId: String): String {
        return getDoctorAvatar(publicId) // Same transformations
    }

    /**
     * Get medical illustration URL
     *
     * For icons, logos, and illustrations with transparency.
     * Uses PNG format with auto-optimization.
     *
     * @param publicId Cloudinary public ID (e.g., "illustrations/empty_appointments")
     * @return Optimized PNG URL
     */
    fun getIllustration(publicId: String): String {
        return if (CLOUD_NAME.isNotEmpty()) {
            "https://res.cloudinary.com/$CLOUD_NAME/image/upload/f_auto,q_auto/$publicId.png"
        } else {
            ""
        }
    }

    /**
     * Get thumbnail for medical records (PDFs/images)
     *
     * @param publicId Cloudinary public ID
     * @param width Thumbnail width (default 300px)
     * @return Thumbnail URL
     */
    fun getMedicalRecordThumbnail(publicId: String, width: Int = 300): String {
        return if (CLOUD_NAME.isNotEmpty()) {
            "https://res.cloudinary.com/$CLOUD_NAME/image/upload/w_$width,c_limit,f_auto,q_auto/$publicId"
        } else {
            ""
        }
    }

    /**
     * Generate initials-based placeholder avatar using ui-avatars.com
     *
     * Creates a circular avatar with user initials when no photo is available.
     * Uses medical green background (#1EB980).
     *
     * @param name Full name (e.g., "Dr. Sarah Johnson")
     * @param size Avatar size in pixels (default 200)
     * @return Placeholder avatar URL
     */
    fun generateInitialsAvatar(name: String, size: Int = 200): String {
        val initials = name
            .trim()
            .split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .take(2)
            .joinToString("")

        // Medical green background, white text
        return "https://ui-avatars.com/api/?name=$initials&background=1EB980&color=fff&size=$size&bold=true"
    }

    /**
     * Upload profile image to Cloudinary
     *
     * Uploads image with face-detection crop transformation.
     * Returns secure URL on success, null on failure.
     *
     * @param file The image file to upload
     * @return Secure URL of uploaded image, or null on failure
     */
    suspend fun uploadProfileImage(file: File): String? {
        return withContext(Dispatchers.IO) {
            try {
                val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
                val apiKey = BuildConfig.CLOUDINARY_API_KEY
                val apiSecret = BuildConfig.CLOUDINARY_API_SECRET

                if (cloudName.isEmpty() || apiKey.isEmpty() || apiSecret.isEmpty()) {
                    return@withContext null
                }

                val client = OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()

                val timestamp = (System.currentTimeMillis() / 1000).toString()
                val folder = "healthmate/profiles"

                // Create signature for secure upload
                val signatureString = "folder=$folder&timestamp=$timestamp$apiSecret"
                val signature = MessageDigest.getInstance("SHA-1")
                    .digest(signatureString.toByteArray())
                    .joinToString("") { "%02x".format(it) }

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        file.name,
                        file.asRequestBody("image/*".toMediaType())
                    )
                    .addFormDataPart("api_key", apiKey)
                    .addFormDataPart("timestamp", timestamp)
                    .addFormDataPart("signature", signature)
                    .addFormDataPart("folder", folder)
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body?.string() ?: "")
                    jsonResponse.getString("secure_url")
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Check if Cloudinary is configured
     */
    fun isConfigured(): Boolean {
        return CLOUD_NAME.isNotEmpty()
    }
}
