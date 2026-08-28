package com.example.core.ui.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/**
 * Shared clipboard helper used by all feature modules instead of
 * duplicating ClipboardManager boilerplate.
 */
fun Context.copyToClipboard(text: String, label: String = "Text") {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text))
}
