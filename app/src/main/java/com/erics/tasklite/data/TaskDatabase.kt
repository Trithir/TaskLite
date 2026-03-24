package com.erics.tasklite.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
	entities = [TaskEntity::class],
	version = 1,
	exportSchema = false
)
abstract class TaskDatabase : RoomDatabase() {
	abstract fun taskDao(): TaskDao

	companion object {
		const val NAME = "tasklite.db"
	}
}
