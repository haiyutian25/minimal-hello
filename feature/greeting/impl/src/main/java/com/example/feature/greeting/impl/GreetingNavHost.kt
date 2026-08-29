package com.example.feature.greeting.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.core.navigation.rememberAppNavigator
import com.example.feature.greeting.api.GreetingNavKey
import com.example.feature.greeting.impl.screens.SplashScreen

/**
 * Navigation 3 host of the greeting feature.
 *
 * The back stack is owned by [rememberAppNavigator]; keys come from the
 * feature's public contract ([GreetingNavKey]) so the app shell never needs
 * to know about internal destinations.
 */
@Composable
fun GreetingNavHost(
    viewModel: GreetingViewModel,
    modifier: Modifier = Modifier,
) {
    val navigator = rememberAppNavigator(GreetingNavKey.Splash)
    val currentTheme by viewModel.currentTheme.collectAsState()

    NavDisplay(
        backStack = navigator.navigationState,
        modifier = modifier,
        entryProvider = entryProvider {
            entry<GreetingNavKey.Splash> {
                SplashScreen(
                    currentTheme = currentTheme,
                    onFinish = { navigator.replace(GreetingNavKey.Main) }
                )
            }
            entry<GreetingNavKey.Main> {
                MainScreen(
                    viewModel = viewModel
                )
            }
        }
    )
}
