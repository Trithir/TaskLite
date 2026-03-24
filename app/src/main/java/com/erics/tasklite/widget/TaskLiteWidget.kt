package com.erics.tasklite.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class TaskLiteWidget : GlanceAppWidget() {
	override suspend fun provideGlance(context: Context, id: GlanceId) {
		val repository = TaskLiteWidgetRepository.from(context)

		provideContent {
			val state = repository.observeState().collectAsState(
				initial = TaskLiteWidgetState()
			).value

			TaskLiteWidgetContent(
				state = state,
				openAction = actionRunCallback<OpenTaskLiteActivityAction>(),
				addAction = actionRunCallback<OpenAddTaskLiteActivityAction>(),
				completeAction = actionRunCallback<CompleteCurrentTaskAction>()
			)
		}
	}
}

@Composable
private fun TaskLiteWidgetContent(
	state: TaskLiteWidgetState,
	openAction: Action,
	addAction: Action,
	completeAction: Action
) {
	Box(
		modifier = GlanceModifier
			.fillMaxSize()
			.background(ColorProvider(Color(0xFF121212)))
			.cornerRadius(24.dp)
			.padding(horizontal = 12.dp, vertical = 10.dp)
	) {
		Box(
			modifier = GlanceModifier.fillMaxSize(),
			contentAlignment = Alignment.CenterStart
		) {
			CircleBubble(
				label = "+",
				action = addAction
			)
		}

		Box(
			modifier = GlanceModifier.fillMaxSize(),
			contentAlignment = Alignment.CenterEnd
		) {
			CircleBubble(
				label = "",
				action = completeAction
			)
		}

		Box(
			modifier = GlanceModifier
				.fillMaxSize()
				.padding(horizontal = 48.dp),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = state.currentTaskText ?: "No current task",
				maxLines = 1,
				style = TextStyle(
					fontWeight = FontWeight.Medium,
					color = ColorProvider(Color.White)
				),
				modifier = GlanceModifier.clickable(openAction)
			)
		}
	}
}

@Composable
private fun CircleBubble(
	label: String,
	action: Action
) {
	Box(
		modifier = GlanceModifier
			.size(32.dp)
			.cornerRadius(16.dp)
			.background(ColorProvider(Color(0xFFE5DDD4)))
			.clickable(action),
		contentAlignment = Alignment.Center
	) {
		Text(
			text = label,
			style = TextStyle(
				fontWeight = FontWeight.Bold,
				color = ColorProvider(Color(0xFF121212))
			)
		)
	}
}
