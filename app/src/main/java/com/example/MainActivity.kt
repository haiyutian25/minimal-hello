package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.core.ui.theme.LocalContentFontFamily
import com.example.core.ui.theme.MinimalTheme
import com.example.feature.greeting.impl.GreetingNavHost
import com.example.feature.greeting.impl.GreetingViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * App shell: single Activity, theme application and navigation assembly.
 * All feature state lives in [GreetingViewModel] (MVVM).
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GreetingViewModel = hiltViewModel()
            val currentTheme by viewModel.currentTheme.collectAsState()
            val activeContentFont by viewModel.activeContentFont.collectAsState()
            val fontScale by viewModel.fontScale.collectAsState()

            // Keep the view model's system dark-mode state in sync so the
            // SYSTEM color mode follows the OS setting live.
            val isSystemDark = isSystemInDarkTheme()
            LaunchedEffect(isSystemDark) {
                viewModel.setSystemDarkMode(isSystemDark)
            }

            // Broadcast the resolved content font (system engine or an installed
            // custom font) and font scale to the whole tree. fontScale multiplies
            // every .sp text size app-wide.
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalContentFontFamily provides activeContentFont,
                LocalDensity provides Density(density = baseDensity.density, fontScale = fontScale)
            ) {
                MinimalTheme(cssVars = currentTheme) {
                    GreetingNavHost(viewModel = viewModel)
                }
            }
        }
    }
}
