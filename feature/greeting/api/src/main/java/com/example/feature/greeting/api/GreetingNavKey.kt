package com.example.feature.greeting.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Navigation contract of the greeting feature.
 *
 * Other modules only ever depend on this sealed hierarchy (never on the
 * implementation module) when they need to navigate into the feature.
 */
@Serializable
sealed interface GreetingNavKey : NavKey {

    /** Typewriter boot splash. */
    @Serializable
    data object Splash : GreetingNavKey

    /** Main canvas experience (sidebar + 4-tab scaffold). */
    @Serializable
    data object Main : GreetingNavKey
}
