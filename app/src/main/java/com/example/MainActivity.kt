package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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

            MinimalTheme(cssVars = currentTheme) {
                GreetingNavHost(viewModel = viewModel)
            }
        }
    }
}
