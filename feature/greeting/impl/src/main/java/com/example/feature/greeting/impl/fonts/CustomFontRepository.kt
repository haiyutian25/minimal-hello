package com.example.feature.greeting.impl.fonts

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

/**
 * Manages user-installed fonts (downloaded presets + local imports) on disk,
 * exposes live download progress, and resolves font IDs to Compose [FontFamily].
 */
@Singleton
class CustomFontRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val fontsDir: File
        get() = File(context.filesDir, DIR_NAME).apply { if (!exists()) mkdirs() }

    private val fontFamilyCache = ConcurrentHashMap<String, FontFamily>()

    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    /** fontId -> download progress in 0f..1f (present only while downloading). */
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()

    private val _installedVersion = MutableStateFlow(0)
    /** Bumped whenever the set of installed files changes, to trigger rescans. */
    val installedVersion: StateFlow<Int> = _installedVersion.asStateFlow()

    /** Scans the fonts directory and returns all installed fonts. */
    fun installedFonts(): List<InstalledFont> =
        fontsDir.listFiles()
            .orEmpty()
            .filter { it.isFile && it.extension.lowercase() in FONT_EXTENSIONS }
            .map { file ->
                val preset = PresetFontCatalog.findByFileName(file.name)
                InstalledFont(
                    id = file.name,
                    displayName = preset?.displayName
                        ?: file.nameWithoutExtension.removePrefix(UPLOAD_PREFIX),
                    fileName = file.name,
                    isPreset = preset != null,
                    sizeBytes = file.length(),
                )
            }
            .sortedBy { it.displayName.lowercase() }

    fun isInstalled(fontId: String): Boolean = File(fontsDir, fontId).exists()

    /** Resolves an installed font ID to a cached [FontFamily], or null if missing. */
    fun fontFamilyFor(fontId: String): FontFamily? {
        if (fontId.isEmpty()) return null
        val file = File(fontsDir, fontId)
        if (!file.exists()) return null
        return fontFamilyCache.getOrPut(fontId) { FontFamily(Font(file = file)) }
    }

    /** Downloads a preset font to disk, streaming progress to [downloadProgress]. */
    suspend fun downloadPreset(preset: PresetFont): Boolean = withContext(Dispatchers.IO) {
        if (isInstalled(preset.fileName)) return@withContext true
        val target = File(fontsDir, preset.fileName)
        val partial = File(fontsDir, preset.fileName + PARTIAL_SUFFIX)
        var connection: HttpURLConnection? = null
        try {
            setProgress(preset.fileName, 0f)
            connection = (URL(preset.downloadUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
            }
            connection.connect()
            if (connection.responseCode !in 200..299) return@withContext fail(partial, preset.fileName)
            val total = if (connection.contentLengthLong > 0) connection.contentLengthLong else preset.sizeBytes
            connection.inputStream.use { input ->
                partial.outputStream().use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int
                    var downloaded = 0L
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloaded += bytesRead
                        if (total > 0) {
                            setProgress(preset.fileName, (downloaded.toFloat() / total).coerceIn(0f, 0.99f))
                        }
                    }
                    output.flush()
                }
            }
            if (!partial.renameTo(target)) {
                partial.copyTo(target, overwrite = true)
                partial.delete()
            }
            bumpInstalled()
            // Clear the progress entry so a preset re-listed in the library (e.g.
            // after deletion) shows the download button, not a stale 100% bar.
            setProgress(preset.fileName, null)
            true
        } catch (e: Exception) {
            fail(partial, preset.fileName)
        } finally {
            connection?.disconnect()
        }
    }

    /** Copies a user-picked font file into the fonts directory. Returns its ID. */
    suspend fun importFont(uri: Uri, fallbackName: String): String? = withContext(Dispatchers.IO) {
        try {
            val originalName = queryDisplayName(uri) ?: fallbackName
            val extension = originalName.substringAfterLast('.', "").lowercase()
            if (extension !in FONT_EXTENSIONS) return@withContext null
            val base = originalName
                .substringBeforeLast('.')
                .replace(Regex("[^A-Za-z0-9_\\-\\u4e00-\\u9fa5]"), "_")
                .ifEmpty { "font" }
            var target = File(fontsDir, "$UPLOAD_PREFIX$base.$extension")
            var suffix = 1
            while (target.exists()) {
                target = File(fontsDir, "$UPLOAD_PREFIX${base}_$suffix.$extension")
                suffix++
            }
            val copied = context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext null
            if (copied <= 0L || !target.exists()) {
                target.delete()
                return@withContext null
            }
            bumpInstalled()
            target.name
        } catch (e: Exception) {
            null
        }
    }

    /** Deletes an installed font file and evicts its cached family. */
    fun deleteFont(fontId: String) {
        File(fontsDir, fontId).delete()
        fontFamilyCache.remove(fontId)
        bumpInstalled()
    }

    private fun bumpInstalled() {
        _installedVersion.update { it + 1 }
    }

    private fun setProgress(fontId: String, value: Float?) {
        _downloadProgress.update { current ->
            if (value == null) current - fontId else current + (fontId to value)
        }
    }

    private fun fail(partial: File, fontId: String): Boolean {
        partial.delete()
        setProgress(fontId, null)
        return false
    }

    private fun queryDisplayName(uri: Uri): String? =
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
            }
        } catch (e: Exception) {
            null
        }

    companion object {
        private const val DIR_NAME = "custom_fonts"
        private const val UPLOAD_PREFIX = "upload_"
        private const val PARTIAL_SUFFIX = ".part"
        private const val BUFFER_SIZE = 64 * 1024
        private const val CONNECT_TIMEOUT_MS = 15_000
        private const val READ_TIMEOUT_MS = 60_000
        private val FONT_EXTENSIONS = setOf("ttf", "otf")
    }
}
