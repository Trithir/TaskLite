package io.tasklite.data

import android.content.Context
import androidx.room.Room
import io.tasklite.notification.TaskLiteNotificationCoordinator

object TaskDatabaseProvider {
	@Volatile
	private var database: TaskDatabase? = null

	fun getDatabase(context: Context): TaskDatabase {
		val applicationContext = context.applicationContext
		return database ?: synchronized(this) {
			database ?: Room.databaseBuilder(
				applicationContext,
				TaskDatabase::class.java,
				TaskDatabase.NAME
			).build().also { database = it }
		}
	}

	fun getRepository(context: Context): TaskRepository {
		val applicationContext = context.applicationContext
		val database = getDatabase(applicationContext)
		val repository = TaskRepositoryImpl(
			database = database,
			taskDao = database.taskDao(),
			appContext = applicationContext
		)
		TaskLiteNotificationCoordinator.ensureStarted(applicationContext)
		return repository
	}
}
