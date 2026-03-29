package io.tasklite.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.tasklite.MainActivity

private const val WIDGET_OPEN_DATA_URI = "tasklite://widget/open"
private const val WIDGET_ADD_DATA_URI = "tasklite://widget/add"

fun createOpenTaskLiteIntent(context: Context): Intent {
	return Intent(context, MainActivity::class.java).apply {
		action = Intent.ACTION_VIEW
		data = Uri.parse(WIDGET_OPEN_DATA_URI)
		flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
	}
}

fun createAddTaskLiteIntent(context: Context): Intent {
	return Intent(context, MainActivity::class.java).apply {
		action = Intent.ACTION_VIEW
		data = Uri.parse(WIDGET_ADD_DATA_URI)
		flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
		TaskLiteLaunchStateStore.markAddIntent(this)
	}
}
