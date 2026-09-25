package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val RoyalColorScheme =
  darkColorScheme(
    primary = RoyalGold,
    secondary = RoyalGoldDark,
    tertiary = CharcoalLight,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = NeutralText,
    onBackground = NeutralText,
    onSurface = NeutralText,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for Royal Studio
  dynamicColor: Boolean = false, // Disable dynamic colors to maintain brand identity
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = RoyalColorScheme, typography = Typography, content = content)
}
