package io.tasklite

import android.app.Application
import io.tasklite.data.TaskDatabaseProvider

class TaskLiteApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		TaskDatabaseProvider.getRepository(this)
	}
}
