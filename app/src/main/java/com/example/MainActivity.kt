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
            val typographyChoice by viewModel.typographyChoice.collectAsState()

            // Keep the view model's system dark-mode state in sync so the
            // SYSTEM color mode follows the OS setting live.
            val isSystemDark = isSystemInDarkTheme()
            LaunchedEffect(isSystemDark) {
                viewModel.setSystemDarkMode(isSystemDark)
            }

            // Broadcast the user's chosen content font to the whole tree.
            CompositionLocalProvider(LocalContentFontFamily provides typographyChoice.font) {
                MinimalTheme(cssVars = currentTheme) {
                    GreetingNavHost(viewModel = viewModel)
                }
            }
        }
    }
}
