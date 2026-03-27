package com.erics.tasklite.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
	primary = AmberDot,
	onPrimary = DeepForest,
	primaryContainer = FernHighlight,
	onPrimaryContainer = Mist,
	secondary = Sage,
	onSecondary = DeepForest,
	secondaryContainer = FernHighlight,
	onSecondaryContainer = Mist,
	background = DeepForest,
	onBackground = Mist,
	surface = MossSurface,
	onSurface = Mist,
	surfaceVariant = Pine,
	onSurfaceVariant = Sage,
	outlineVariant = Sage.copy(alpha = 0.38f)
)

private val LightColors = lightColorScheme(
	primary = AmberDot,
	onPrimary = DeepForest,
	primaryContainer = ColorTokens.LightPrimaryContainer,
	onPrimaryContainer = Bark,
	secondary = FernHighlight,
	onSecondary = Cream,
	secondaryContainer = ColorTokens.LightSecondaryContainer,
	onSecondaryContainer = Bark,
	background = Cream,
	onBackground = Bark,
	surface = ColorTokens.LightSurface,
	onSurface = Bark,
	surfaceVariant = ColorTokens.LightSecondaryContainer,
	onSurfaceVariant = ColorTokens.LightMuted,
	outlineVariant = FernHighlight.copy(alpha = 0.22f)
)

@Composable
fun TaskLiteTheme(
	darkTheme: Boolean = true,
	content: @Composable () -> Unit
) {
	val colorScheme = if (darkTheme) DarkColors else LightColors

	MaterialTheme(
		colorScheme = colorScheme,
		typography = TaskLiteTypography,
		content = content
	)
}

private object ColorTokens {
	val LightPrimaryContainer = Color(0xFFD3E6D8)
	val LightSecondaryContainer = Color(0xFFD9EADF)
	val LightSurface = Color(0xFFEAF3EE)
	val LightMuted = Bark.copy(alpha = 0.7f)
}
