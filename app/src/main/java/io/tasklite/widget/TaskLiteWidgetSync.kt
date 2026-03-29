package io.tasklite.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

object TaskLiteWidgetSync {
	suspend fun refresh(context: Context) {
		TaskLiteWidget().updateAll(context.applicationContext)
	}
}
