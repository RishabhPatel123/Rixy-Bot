package com.rixy.bot.data.prefs

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import java.io.File
import java.io.FileOutputStream

/**
 * App-private image storage: saves generated images and picked attachments to
 * files, decodes bounded bitmaps for display, and exports to the gallery on
 * API 29+ (MediaStore needs no permission for app-created entries).
 */
class ImageStore(private val context: Context) {

    private val dir: File
        get() = File(context.filesDir, "rixy_images").apply { mkdirs() }

    /** Decodes base64 image data and persists it; returns the absolute file path. */
    fun saveBase64(base64: String, mimeType: String): String {
        val ext = when {
            mimeType.contains("jpeg") || mimeType.contains("jpg") -> "jpg"
            mimeType.contains("webp") -> "webp"
            else -> "png"
        }
        val bytes = Base64.decode(base64, Base64.NO_WRAP)
        val file = File(dir, "img_${System.currentTimeMillis()}.$ext")
        file.writeBytes(bytes)
        return file.absolutePath
    }

    /** Copies picked content into private storage; returns the absolute file path or null. */
    fun copyFromUri(uri: Uri): String? {
        val mimeType = context.contentResolver.getType(uri) ?: "image/png"
        val ext = when {
            mimeType.contains("jpeg") || mimeType.contains("jpg") -> "jpg"
            mimeType.contains("webp") -> "webp"
            else -> "png"
        }
        val file = File(dir, "att_${System.currentTimeMillis()}.$ext")
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            } ?: return null
            file.absolutePath
        }.getOrNull()
    }

    /** Reads a file into base64 for the Gemini inlineData part. */
    fun toBase64(path: String): String? = runCatching {
        Base64.encodeToString(File(path).readBytes(), Base64.NO_WRAP)
    }.getOrNull()

    fun mimeTypeFor(path: String): String = when (path.substringAfterLast('.', "").lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "webp" -> "image/webp"
        else -> "image/png"
    }

    /** Decodes a file to a Bitmap no larger than [maxDim] on its longest side. */
    fun decodeBounded(path: String, maxDim: Int = 2048): Bitmap? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (bounds.outWidth / sample > maxDim || bounds.outHeight / sample > maxDim) sample *= 2
        BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    }.getOrNull()

    /** Saves an image to the shared gallery. Returns false on unsupported Android versions. */
    fun saveToGallery(path: String, displayName: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val mimeType = mimeTypeFor(path)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.${path.substringAfterLast('.', "png")}")
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Rixy")
        }
        return runCatching {
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return false
            context.contentResolver.openOutputStream(uri)?.use { out ->
                File(path).inputStream().use { it.copyTo(out) }
            } ?: return false
            true
        }.getOrDefault(false)
    }
}
