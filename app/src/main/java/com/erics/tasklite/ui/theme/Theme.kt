package com.erics.tasklite.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
	primary = Sand,
	onPrimary = NightInk,
	background = NightInk,
	onBackground = Fog,
	surface = Slate,
	onSurface = Fog,
	onSurfaceVariant = Cloud
)

private val LightColors = lightColorScheme(
	primary = Moss,
	onPrimary = Snow,
	background = Snow,
	onBackground = Ash,
	surface = ColorTokens.LightSurface,
	onSurface = Ash,
	onSurfaceVariant = ColorTokens.LightMuted
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
	val LightSurface = Snow
	val LightMuted = Ash.copy(alpha = 0.7f)
}
