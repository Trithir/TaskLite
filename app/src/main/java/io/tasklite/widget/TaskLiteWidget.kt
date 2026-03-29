package io.tasklite.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

private val WidgetPillBackground = ColorProvider(Color(0xD0143127))
private val WidgetTaskTextColor = ColorProvider(Color(0xFFE6F1EC))
private val WidgetBubbleBackground = ColorProvider(Color(0xFFE4B86A))
private val WidgetBubbleTextColor = ColorProvider(Color(0xFF0B1E17))
private val WidgetBubbleOutline = ColorProvider(Color(0xFF89A498))
private val WidgetBubbleIdleBackground = WidgetPillBackground
private val WidgetBubbleSize = 26.dp
private val WidgetPillCornerRadius = 18.dp
private val WidgetOuterHorizontalPadding = 12.dp
private val WidgetOuterVerticalPadding = 8.dp
private val WidgetInnerHorizontalPadding = 12.dp
private val WidgetInnerVerticalPadding = 7.dp
private val WidgetTextSidePadding = 34.dp

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
				completeAction = actionRunCallback<CompleteCurrentTaskAction>(
					parameters = actionParametersOf(
						WidgetTaskIdParamKey to (state.currentTaskId ?: -1L)
					)
				)
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
		modifier = GlanceModifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Box(
			modifier = GlanceModifier
				.fillMaxWidth()
				.padding(
					horizontal = WidgetOuterHorizontalPadding,
					vertical = WidgetOuterVerticalPadding
				)
				.clickable(openAction)
				.background(WidgetPillBackground)
				.cornerRadius(WidgetPillCornerRadius)
				.padding(
					horizontal = WidgetInnerHorizontalPadding,
					vertical = WidgetInnerVerticalPadding
				)
		) {
			Box(
				modifier = GlanceModifier.fillMaxWidth(),
				contentAlignment = Alignment.CenterStart
			) {
				CircleBubble(
					label = "\u2713",
					action = completeAction,
					size = WidgetBubbleSize,
					filled = state.isCompleting
				)
			}

			Box(
				modifier = GlanceModifier.fillMaxWidth(),
				contentAlignment = Alignment.CenterEnd
			) {
				CircleBubble(
					label = "+",
					action = addAction,
					size = WidgetBubbleSize,
					filled = true
				)
			}

			Box(
				modifier = GlanceModifier
					.fillMaxWidth()
					.padding(horizontal = WidgetTextSidePadding),
				contentAlignment = Alignment.Center
			) {
				Text(
					text = state.currentTaskText ?: "No current task",
					maxLines = 1,
					style = TextStyle(
						fontWeight = FontWeight.Medium,
						fontSize = 14.sp,
						color = if (state.isCompleting) {
							ColorProvider(Color(0xFFB8C9C1))
						} else {
							WidgetTaskTextColor
						},
						textDecoration = if (state.isCompleting) {
							TextDecoration.LineThrough
						} else {
							TextDecoration.None
						}
					)
				)
			}
		}
	}
}

@Composable
private fun CircleBubble(
	label: String,
	action: Action,
	size: Dp,
	filled: Boolean
) {
	Box(
		modifier = GlanceModifier
			.size(size)
			.cornerRadius(size / 2)
			.background(WidgetBubbleOutline)
			.clickable(action),
		contentAlignment = Alignment.Center
	) {
		Box(
			modifier = GlanceModifier
				.size(size - 2.dp)
				.cornerRadius((size - 2.dp) / 2)
				.background(if (filled) WidgetBubbleBackground else WidgetBubbleIdleBackground),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = label,
				style = TextStyle(
					fontWeight = FontWeight.Bold,
					fontSize = 13.sp,
					color = if (filled) WidgetBubbleTextColor else WidgetTaskTextColor
				)
			)
		}
	}
}
