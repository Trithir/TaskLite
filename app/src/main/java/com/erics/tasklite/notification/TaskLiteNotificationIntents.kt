package com.erics.tasklite.notification

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.erics.tasklite.MainActivity

private const val NOTIFICATION_OPEN_DATA_URI = "tasklite://notification/open"

fun createOpenTaskLiteNotificationIntent(context: Context): Intent {
	return Intent(context, MainActivity::class.java).apply {
		action = Intent.ACTION_VIEW
		data = Uri.parse(NOTIFICATION_OPEN_DATA_URI)
		flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
	}
}
