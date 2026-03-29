package io.tasklite.notification

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.notificationSettingsDataStore by preferencesDataStore(
	name = "tasklite_notification_settings"
)

object TaskLiteNotificationSettingsStore {
	private val KEY_ENABLED = booleanPreferencesKey("notification_enabled")

	fun observeState(context: Context): Flow<TaskLiteNotificationSettingsState> {
		return context.applicationContext.notificationSettingsDataStore.data
			.map { preferences ->
				TaskLiteNotificationSettingsState(
					enabled = preferences[KEY_ENABLED] ?: false
				)
			}
			.distinctUntilChanged()
	}

	fun observeEnabled(context: Context): Flow<Boolean> {
		return observeState(context)
			.map { it.enabled }
			.distinctUntilChanged()
	}

	suspend fun isEnabled(context: Context): Boolean {
		return currentEnabled(context)
	}

	suspend fun currentEnabled(context: Context): Boolean {
		return observeState(context).first().enabled
	}

	suspend fun setEnabled(context: Context, enabled: Boolean) {
		context.applicationContext.notificationSettingsDataStore.edit { preferences ->
			preferences[KEY_ENABLED] = enabled
		}
	}

	suspend fun toggleEnabled(context: Context): Boolean {
		val nextEnabled = !currentEnabled(context)
		setEnabled(context, nextEnabled)
		return nextEnabled
	}
}
