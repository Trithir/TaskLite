package com.erics.tasklite.widget

import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class TaskLiteLaunchMode {
	OPEN,
	ADD
}

object TaskLiteLaunchStateStore {
	private const val EXTRA_LAUNCH_MODE = "com.erics.tasklite.extra.LAUNCH_MODE"
	private const val LAUNCH_MODE_ADD = "add"

	private val _launchMode = MutableStateFlow(TaskLiteLaunchMode.OPEN)

	val launchMode: StateFlow<TaskLiteLaunchMode> = _launchMode

	fun update(intent: Intent?) {
		_launchMode.value = when (intent?.getStringExtra(EXTRA_LAUNCH_MODE)) {
			LAUNCH_MODE_ADD -> TaskLiteLaunchMode.ADD
			else -> TaskLiteLaunchMode.OPEN
		}
	}

	fun consumeAddLaunch() {
		_launchMode.value = TaskLiteLaunchMode.OPEN
	}

	fun markAddIntent(intent: Intent) {
		intent.putExtra(EXTRA_LAUNCH_MODE, LAUNCH_MODE_ADD)
	}
}
