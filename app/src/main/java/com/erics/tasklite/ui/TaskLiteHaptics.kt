package com.erics.tasklite.ui

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

internal fun performAddTaskVibration(
	context: Context,
	fallbackView: View? = null
) {
	performTaskLiteVibration(
		context = context,
		fallbackView = fallbackView,
		effect = taskActionConfirmationEffect(),
		fallbackConstant = HapticFeedbackConstants.KEYBOARD_TAP
	)
}

internal fun performMoveTaskVibration(
	context: Context,
	fallbackView: View? = null
) {
	performTaskLiteVibration(
		context = context,
		fallbackView = fallbackView,
		effect = taskActionConfirmationEffect(),
		fallbackConstant = HapticFeedbackConstants.KEYBOARD_TAP
	)
}

internal fun performEditTaskVibration(
	context: Context,
	fallbackView: View? = null
) {
	performTaskLiteVibration(
		context = context,
		fallbackView = fallbackView,
		effect = taskActionConfirmationEffect(),
		fallbackConstant = HapticFeedbackConstants.KEYBOARD_TAP
	)
}

internal fun performCompleteTaskVibration(
	context: Context,
	fallbackView: View? = null
) {
	performTaskLiteVibration(
		context = context,
		fallbackView = fallbackView,
		effect = completeTaskVibrationEffect(),
		fallbackConstant = HapticFeedbackConstants.LONG_PRESS
	)
}

internal fun performWidgetCompleteTaskVibration(context: Context) {
	val vibrator = context.getSystemService(VibratorManager::class.java)?.defaultVibrator
		?: context.getSystemService(Vibrator::class.java)

	if (vibrator == null) {
		return
	}

	runCatching {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			vibrator.vibrate(
				completeTaskVibrationEffect(),
				WIDGET_COMPLETE_VIBRATION_ATTRIBUTES
			)
		} else {
			vibrator.vibrate(completeTaskVibrationEffect())
		}
	}
}

private fun taskActionConfirmationEffect(): VibrationEffect {
	return VibrationEffect.createWaveform(
		longArrayOf(0L, 36L, 28L, 112L),
		intArrayOf(0, 180, 0, 255),
		-1
	)
}

private fun completeTaskVibrationEffect(): VibrationEffect {
	return VibrationEffect.createWaveform(
		longArrayOf(0L, 216L, 216L, 360L),
		intArrayOf(0, 255, 0, 255),
		-1
	)
}

private val WIDGET_COMPLETE_VIBRATION_ATTRIBUTES: VibrationAttributes =
	VibrationAttributes.createForUsage(VibrationAttributes.USAGE_NOTIFICATION)

private fun performTaskLiteVibration(
	context: Context,
	fallbackView: View?,
	effect: VibrationEffect,
	fallbackConstant: Int
) {
	val vibrator = context.getSystemService(VibratorManager::class.java)?.defaultVibrator
		?: context.getSystemService(Vibrator::class.java)

	if (vibrator != null) {
		runCatching {
			vibrator.vibrate(effect)
		}.onSuccess {
			return
		}
	}

	fallbackView?.performHapticFeedback(fallbackConstant)
}
