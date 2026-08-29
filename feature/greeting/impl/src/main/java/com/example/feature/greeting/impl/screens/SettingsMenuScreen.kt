package com.example.feature.greeting.impl.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.CssVariables
import com.example.feature.greeting.impl.R
import com.example.feature.greeting.impl.components.Button

/**
 * Settings menu list, rendered inside the main Scaffold content area so the
 * global top nav bar stays visible. Current entry:
 * "Appearance & Themes" -> the appearance settings page.
 */
@Composable
fun SettingsMenuScreen(
    currentTheme: CssVariables,
    onOpenAppearance: () -> Unit,
    onOpenLanguage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.background)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .widthIn(max = 560.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.settings_menu_label),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = currentTheme.mutedForeground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Entry: Appearance & Themes -> existing appearance settings page
        Button(
            onClick = { onOpenAppearance() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(currentTheme.radiusMd),
            testTag = "settings_menu_appearance_entry"
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(currentTheme.card)
                    .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusMd))
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = null,
                    tint = currentTheme.primary,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_menu_appearance_title),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = currentTheme.foreground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.settings_menu_appearance_subtitle),
                        fontSize = 11.sp,
                        color = currentTheme.mutedForeground
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = currentTheme.mutedForeground,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Entry: Language -> language selection page
        Button(
            onClick = { onOpenLanguage() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(currentTheme.radiusMd),
            testTag = "settings_menu_language_entry"
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(currentTheme.card)
                    .border(1.dp, currentTheme.border, RoundedCornerShape(currentTheme.radiusMd))
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = null,
                    tint = currentTheme.primary,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.language_title),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = currentTheme.foreground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.language_menu_subtitle),
                        fontSize = 11.sp,
                        color = currentTheme.mutedForeground
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = currentTheme.mutedForeground,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
