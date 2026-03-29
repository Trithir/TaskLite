package io.tasklite.notification

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

object TaskLiteNotificationCoordinator {
	@Volatile
	private var started = false

	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

	fun ensureStarted(context: Context) {
		if (started) {
			return
		}

		synchronized(this) {
			if (started) {
				return
			}

			started = true
			val notificationRepository = TaskLiteNotificationRepository.from(context)
			combine(
				notificationRepository.observeState(),
				TaskLiteNotificationManager.observeEnabled(context)
			) { state, enabled ->
				state to enabled
			}
				.onEach { (state, enabled) ->
					if (enabled) {
						TaskLiteNotificationManager.show(context, state.currentTaskText)
					} else {
						TaskLiteNotificationManager.hide(context)
					}
				}
				.launchIn(scope)
		}
	}
}
