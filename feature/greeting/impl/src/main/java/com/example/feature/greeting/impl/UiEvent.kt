package com.example.feature.greeting.impl

import androidx.annotation.StringRes

/**
 * One-time UI events emitted by [GreetingViewModel] and consumed exactly once
 * by the UI through a [kotlinx.coroutines.channels.Channel].
 *
 * Unlike [kotlinx.coroutines.flow.StateFlow], these events are not replayed to
 * new collectors, which makes them suitable for transient feedback such as
 * toasts (e.g. "font imported", "download failed").
 */
sealed interface UiEvent {
    /** Show a transient toast carrying a string resource. */
    data class ShowToast(@StringRes val messageRes: Int) : UiEvent
}
