package com.example.feature.greeting.impl.fonts

/**
 * A downloadable preset font hosted on the self-hosted font server.
 */
data class PresetFont(
    val id: String,
    val displayName: String,
    val fileName: String,
    val downloadUrl: String,
    val sizeBytes: Long,
)

/**
 * Static catalog of preset fonts served by the VPS font server. These can be
 * downloaded on demand and then used like any installed font.
 */
object PresetFontCatalog {
    private const val BASE_URL = "http://106.55.13.206:5212"

    val ALL: List<PresetFont> = listOf(
        PresetFont(
            id = "preset-source-han-serif",
            displayName = "Source Han Serif SC",
            fileName = "SourceHanSerifSC-Regular.otf",
            downloadUrl = "$BASE_URL/SourceHanSerifSC-Regular.otf",
            sizeBytes = 24_543_332L,
        ),
        PresetFont(
            id = "preset-lxgw-wenkai",
            displayName = "LXGW WenKai",
            fileName = "LXGWWenKai-Regular.ttf",
            downloadUrl = "$BASE_URL/LXGWWenKai-Regular.ttf",
            sizeBytes = 25_575_676L,
        ),
        PresetFont(
            id = "preset-jetbrains-mono",
            displayName = "JetBrains Mono",
            fileName = "JetBrainsMono-Regular.ttf",
            downloadUrl = "$BASE_URL/JetBrainsMono-Regular.ttf",
            sizeBytes = 273_900L,
        ),
    )

    fun findByFileName(fileName: String): PresetFont? = ALL.firstOrNull { it.fileName == fileName }
}

/**
 * A font installed on the device (a downloaded preset or a user import).
 * [id] is the on-disk file name and uniquely identifies the font.
 */
data class InstalledFont(
    val id: String,
    val displayName: String,
    val fileName: String,
    val isPreset: Boolean,
    val sizeBytes: Long,
)
