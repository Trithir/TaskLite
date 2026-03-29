package io.tasklite.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.widgetCompletionDataStore by preferencesDataStore(
	name = "tasklite_widget_completion"
)

data class PendingWidgetCompletionState(
	val taskId: Long? = null,
	val taskText: String? = null,
	val visualUntilMillis: Long = 0L
) {
	fun isActive(nowMillis: Long): Boolean {
		return !taskText.isNullOrBlank() && visualUntilMillis > nowMillis
	}
}

object TaskLiteWidgetCompletionStore {
	private val KEY_TASK_ID = longPreferencesKey("task_id")
	private val KEY_TASK_TEXT = stringPreferencesKey("task_text")
	private val KEY_VISUAL_UNTIL = longPreferencesKey("visual_until")

	fun observePendingCompletion(context: Context): Flow<PendingWidgetCompletionState> {
		return context.applicationContext.widgetCompletionDataStore.data.map { preferences ->
			PendingWidgetCompletionState(
				taskId = preferences[KEY_TASK_ID],
				taskText = preferences[KEY_TASK_TEXT],
				visualUntilMillis = preferences[KEY_VISUAL_UNTIL] ?: 0L
			)
		}
	}

	suspend fun setPendingCompletion(
		context: Context,
		taskId: Long,
		taskText: String,
		visualUntilMillis: Long
	) {
		context.applicationContext.widgetCompletionDataStore.edit { preferences ->
			preferences[KEY_TASK_ID] = taskId
			preferences[KEY_TASK_TEXT] = taskText
			preferences[KEY_VISUAL_UNTIL] = visualUntilMillis
		}
	}

	suspend fun clearPendingCompletion(context: Context) {
		context.applicationContext.widgetCompletionDataStore.edit { preferences ->
			preferences.remove(KEY_TASK_ID)
			preferences.remove(KEY_TASK_TEXT)
			preferences.remove(KEY_VISUAL_UNTIL)
		}
	}
}
