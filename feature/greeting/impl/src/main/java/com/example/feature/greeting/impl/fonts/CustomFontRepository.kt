package com.example.feature.greeting.impl.fonts

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.core.data.manager.dispatcher.DispatcherManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Manages user-installed fonts (downloaded presets + local imports) on disk,
 * exposes live download progress, and resolves font IDs to Compose [FontFamily].
 */
@Singleton
class CustomFontRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatcherManager: DispatcherManager,
    okHttpClient: OkHttpClient,
) {
    /**
     * Derived from the shared Hilt-provided client (core:network): same
     * connection pool, dispatcher and debug logging interceptor, but with the
     * longer timeouts multi-MB font downloads need.
     */
    private val downloadClient: OkHttpClient = okHttpClient.newBuilder()
        .connectTimeout(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        .readTimeout(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        .build()
    private val fontsDir: File
        get() = File(context.filesDir, DIR_NAME).apply { if (!exists()) mkdirs() }

    private val fontFamilyCache = ConcurrentHashMap<String, FontFamily>()

    /**
     * In-memory snapshot of the on-disk font file names, refreshed by every
     * [installedFonts] scan and kept current by install/delete mutations.
     * [fontFamilyFor] is on the hot read path (called from the ViewModel's
     * updateState and from LazyList item compositions, both on the main
     * thread), so membership must never be answered with filesystem I/O.
     */
    @Volatile
    private var installedIds: Set<String> = emptySet()

    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    /** fontId -> download progress in 0f..1f (present only while downloading). */
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()

    private val _installedVersion = MutableStateFlow(0)
    /** Bumped whenever the set of installed files changes, to trigger rescans. */
    val installedVersion: StateFlow<Int> = _installedVersion.asStateFlow()

    /** Scans the fonts directory and returns all installed fonts. */
    suspend fun installedFonts(): List<InstalledFont> = withContext(dispatcherManager.io) {
        val fonts = fontsDir.listFiles()
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
        installedIds = fonts.map { it.id }.toSet()
        fonts
    }

    fun isInstalled(fontId: String): Boolean = File(fontsDir, fontId).exists()

    /**
     * Resolves an installed font ID to a cached [FontFamily], or null if missing.
     * Membership is answered from the in-memory [installedIds] snapshot — no
     * disk I/O on the caller thread. Until the first scan lands after process
     * start the snapshot is empty; the [installedVersion] collection in the
     * ViewModel triggers that scan and re-derives the font, so the choice
     * self-heals within one frame sequence.
     */
    fun fontFamilyFor(fontId: String): FontFamily? {
        if (fontId.isEmpty()) return null
        if (fontId !in installedIds) return null
        return fontFamilyCache.getOrPut(fontId) { FontFamily(Font(file = File(fontsDir, fontId))) }
    }

    /**
     * Downloads a preset font to disk, streaming progress to [downloadProgress].
     * The transferred bytes are SHA-256 verified against [PresetFont.sha256];
     * a mismatching file (tampered, truncated or a stale server copy) is
     * rejected and never installed.
     */
    suspend fun downloadPreset(preset: PresetFont): Boolean = withContext(dispatcherManager.io) {
        if (isInstalled(preset.fileName)) return@withContext true
        val target = File(fontsDir, preset.fileName)
        val partial = File(fontsDir, preset.fileName + PARTIAL_SUFFIX)
        try {
            setProgress(preset.fileName, 0f)
            val request = Request.Builder().url(preset.downloadUrl).build()
            downloadClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext fail(partial, preset.fileName)
                val body = response.body ?: return@withContext fail(partial, preset.fileName)
                val total = body.contentLength().takeIf { it > 0 } ?: preset.sizeBytes
                val digest = MessageDigest.getInstance("SHA-256")
                body.byteStream().use { input ->
                    partial.outputStream().use { output ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var bytesRead: Int
                        var downloaded = 0L
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            digest.update(buffer, 0, bytesRead)
                            downloaded += bytesRead
                            setProgress(preset.fileName, (downloaded.toFloat() / total).coerceIn(0f, 0.99f))
                        }
                        output.flush()
                    }
                }
                val actualDigest = digest.digest().toHexString()
                if (!actualDigest.equals(preset.sha256, ignoreCase = true)) {
                    return@withContext fail(partial, preset.fileName)
                }
            }
            if (!partial.renameTo(target)) {
                partial.copyTo(target, overwrite = true)
                partial.delete()
            }
            installedIds = installedIds + preset.fileName
            bumpInstalled()
            // Clear the progress entry so a preset re-listed in the library (e.g.
            // after deletion) shows the download button, not a stale 100% bar.
            setProgress(preset.fileName, null)
            true
        } catch (e: Exception) {
            fail(partial, preset.fileName)
        }
    }

    /** Copies a user-picked font file into the fonts directory. Returns its ID. */
    suspend fun importFont(uri: Uri, fallbackName: String): String? = withContext(dispatcherManager.io) {
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
            installedIds = installedIds + target.name
            bumpInstalled()
            target.name
        } catch (e: Exception) {
            null
        }
    }

    /** Deletes an installed font file and evicts its cached family. */
    suspend fun deleteFont(fontId: String) = withContext(dispatcherManager.io) {
        File(fontsDir, fontId).delete()
        fontFamilyCache.remove(fontId)
        installedIds = installedIds - fontId
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
        private const val CONNECT_TIMEOUT_MS = 15_000L
        private const val READ_TIMEOUT_MS = 60_000L
        private val FONT_EXTENSIONS = setOf("ttf", "otf")
    }
}
