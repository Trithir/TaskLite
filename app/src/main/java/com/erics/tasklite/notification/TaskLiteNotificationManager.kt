package com.erics.tasklite.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.erics.tasklite.R
import kotlinx.coroutines.flow.Flow

object TaskLiteNotificationManager {
	private const val CHANNEL_NAME = "Current Task"

	fun observeEnabled(context: Context): Flow<Boolean> {
		return TaskLiteNotificationSettingsStore.observeEnabled(context)
	}

	suspend fun refresh(context: Context) {
		if (!TaskLiteNotificationSettingsStore.currentEnabled(context)) {
			hide(context)
			return
		}

		val state = TaskLiteNotificationRepository.from(context).loadState()
		show(context, state.currentTaskText)
	}

	suspend fun setEnabled(context: Context, enabled: Boolean) {
		TaskLiteNotificationSettingsStore.setEnabled(context, enabled)
		if (enabled) {
			refresh(context)
		} else {
			hide(context)
		}
	}

	suspend fun toggleEnabled(context: Context): Boolean {
		val nextEnabled = !currentEnabled(context)
		setEnabled(context, nextEnabled)
		return nextEnabled
	}

	suspend fun currentEnabled(context: Context): Boolean {
		return TaskLiteNotificationSettingsStore.currentEnabled(context)
	}

	fun show(context: Context, currentTaskText: String?) {
		if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
			hide(context)
			return
		}

		ensureChannel(context)

		val notification = NotificationCompat.Builder(context, TaskLiteNotificationIds.CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_notification_tasklite)
			.setContentTitle(currentTaskText ?: "No current task")
			.setContentIntent(
				PendingIntent.getActivity(
					context,
					0,
					createOpenTaskLiteNotificationIntent(context),
					PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
				)
			)
			.setOngoing(true)
			.setOnlyAlertOnce(true)
			.setSilent(true)
			.setCategory(NotificationCompat.CATEGORY_STATUS)
			.setPriority(NotificationCompat.PRIORITY_LOW)
			.setShowWhen(false)
			.build()

		NotificationManagerCompat.from(context).notify(TaskLiteNotificationIds.NOTIFICATION_ID, notification)
	}

	fun hide(context: Context) {
		NotificationManagerCompat.from(context).cancel(TaskLiteNotificationIds.NOTIFICATION_ID)
	}

	private fun ensureChannel(context: Context) {
		val channel = NotificationChannel(
			TaskLiteNotificationIds.CHANNEL_ID,
			CHANNEL_NAME,
			NotificationManager.IMPORTANCE_LOW
		).apply {
			description = "Shows the current task"
			setShowBadge(false)
			lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
		}

		val manager = context.getSystemService(NotificationManager::class.java)
		manager.createNotificationChannel(channel)
	}
}
