package io.tasklite.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class TaskLiteWidgetReceiver : GlanceAppWidgetReceiver() {
	override val glanceAppWidget = TaskLiteWidget()
}
