package io.tasklite.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertTask(task: TaskEntity): Long

	@Update
	suspend fun updateTask(task: TaskEntity)

	@Update
	suspend fun updateTasks(tasks: List<TaskEntity>)

	@Delete
	suspend fun deleteTask(task: TaskEntity)

	@Query(
		"""
		SELECT * FROM tasks
		WHERE completedAt IS NULL
		ORDER BY sortOrder ASC, id ASC
		"""
	)
	fun getIncompleteTasksOrdered(): Flow<List<TaskEntity>>

	@Query(
		"""
		SELECT * FROM tasks
		WHERE completedAt IS NULL
		ORDER BY sortOrder ASC, id ASC
		"""
	)
	suspend fun getIncompleteTasksOrderedOnce(): List<TaskEntity>

	@Query(
		"""
		SELECT * FROM tasks
		WHERE completedAt IS NOT NULL
		ORDER BY completedAt DESC, id DESC
		"""
	)
	fun getCompletedTasksOrdered(): Flow<List<TaskEntity>>

	@Query(
		"""
		SELECT * FROM tasks
		WHERE completedAt IS NULL
		ORDER BY sortOrder ASC, id ASC
		LIMIT 1
		"""
	)
	fun getCurrentTask(): Flow<TaskEntity?>

	@Query(
		"""
		SELECT * FROM tasks
		WHERE completedAt IS NULL
		ORDER BY sortOrder ASC, id ASC
		LIMIT 1
		"""
	)
	suspend fun getCurrentTaskOnce(): TaskEntity?
}
