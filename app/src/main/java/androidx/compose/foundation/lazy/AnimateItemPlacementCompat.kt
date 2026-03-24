package androidx.compose.foundation.lazy

import androidx.compose.ui.Modifier

/**
 * Compatibility shim for the app's current Compose baseline.
 *
 * The expanded screen still imports the legacy lazy placement API, so this keeps
 * the project compiling without changing that screen file in this slice.
 */
fun Modifier.animateItemPlacement(): Modifier {
	return this
}
