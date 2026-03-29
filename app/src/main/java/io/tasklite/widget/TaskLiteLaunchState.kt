package io.tasklite.widget

import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class TaskLiteLaunchMode {
	OPEN,
	ADD
}

object TaskLiteLaunchStateStore {
	private const val EXTRA_LAUNCH_MODE = "io.tasklite.extra.LAUNCH_MODE"
	private const val LAUNCH_MODE_ADD = "add"

	private val _launchMode = MutableStateFlow(TaskLiteLaunchMode.OPEN)
	private val _launchNonce = MutableStateFlow(0)

	val launchMode: StateFlow<TaskLiteLaunchMode> = _launchMode
	val launchNonce: StateFlow<Int> = _launchNonce

	fun update(intent: Intent?) {
		_launchMode.value = when (intent?.getStringExtra(EXTRA_LAUNCH_MODE)) {
			LAUNCH_MODE_ADD -> TaskLiteLaunchMode.ADD
			else -> TaskLiteLaunchMode.OPEN
		}
		_launchNonce.value += 1
	}

	fun consumeAddLaunch() {
		_launchMode.value = TaskLiteLaunchMode.OPEN
	}

	fun markAddIntent(intent: Intent) {
		intent.putExtra(EXTRA_LAUNCH_MODE, LAUNCH_MODE_ADD)
	}
}
