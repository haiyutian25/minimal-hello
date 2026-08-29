package com.example.feature.greeting.impl.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.CssVariables

/**
 * Stateless themed search / filter input (CSS-token driven), used at the top
 * of the Tokens page. State is hoisted to the caller.
 */
@Composable
fun TokenSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    currentTheme: CssVariables,
    placeholder: String = "Filter CSS variable tokens...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder, fontSize = 12.sp, color = currentTheme.mutedForeground) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = currentTheme.mutedForeground,
                modifier = Modifier.size(16.dp)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(currentTheme.radiusSm),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = currentTheme.card,
            unfocusedContainerColor = currentTheme.card,
            focusedBorderColor = currentTheme.primary,
            unfocusedBorderColor = currentTheme.border,
            focusedTextColor = currentTheme.foreground,
            unfocusedTextColor = currentTheme.foreground
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("tokens_search_input")
    )
}
