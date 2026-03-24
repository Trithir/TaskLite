package com.erics.tasklite

import android.app.Application
import com.erics.tasklite.data.TaskDatabaseProvider

class TaskLiteApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		TaskDatabaseProvider.getRepository(this)
	}
}
