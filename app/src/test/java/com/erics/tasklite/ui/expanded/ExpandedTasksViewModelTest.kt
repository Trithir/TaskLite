package com.erics.tasklite.ui.expanded

import com.erics.tasklite.data.TaskEntity
import com.erics.tasklite.data.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpandedTasksViewModelTest {
	private val dispatcher = StandardTestDispatcher()

	@Before
	fun setUp() {
		Dispatchers.setMain(dispatcher)
	}

	@After
	fun tearDown() {
		Dispatchers.resetMain()
	}

	@Test
	fun submitNewTask_trimsText_andAddsAtBottomOfActiveList() = runTest(dispatcher) {
		val repository = FakeTaskRepository(
			listOf(
				TaskEntity(id = 1L, text = "Current", sortOrder = 0L),
				TaskEntity(id = 2L, text = "Later", sortOrder = 1L)
			)
		)
		val viewModel = createViewModel(repository)
		collectUiState(viewModel)
		advanceUntilIdle()

		viewModel.showAddTaskField()
		viewModel.updateNewTaskText("  New task  ")
		viewModel.submitNewTask()
		advanceUntilIdle()

		assertEquals(
			listOf("Current", "Later", "New task"),
			repository.incompleteSnapshot().map(TaskEntity::text)
		)
		assertEquals(2L, repository.incompleteSnapshot().last().sortOrder)
		assertFalse(viewModel.uiState.value.isAddTaskFieldVisible)
		assertEquals("", viewModel.uiState.value.newTaskText)
	}

	@Test
	fun completeTask_waitsForDelayBeforePromotingNextTask() = runTest(dispatcher) {
		val repository = FakeTaskRepository(
			listOf(
				TaskEntity(id = 1L, text = "Current", sortOrder = 0L),
				TaskEntity(id = 2L, text = "Next", sortOrder = 1L)
			)
		)
		val viewModel = createViewModel(repository)
		collectUiState(viewModel)
		advanceUntilIdle()

		viewModel.completeTask(1L)
		runCurrent()

		assertEquals(setOf(1L), viewModel.uiState.value.pendingCompletionTaskIds)
		assertEquals(listOf("Current", "Next"), repository.incompleteSnapshot().map(TaskEntity::text))

		advanceTimeBy(299L)
		runCurrent()

		assertEquals(listOf("Current", "Next"), repository.incompleteSnapshot().map(TaskEntity::text))
		assertEquals(setOf(1L), viewModel.uiState.value.pendingCompletionTaskIds)

		advanceTimeBy(1L)
		advanceUntilIdle()

		assertEquals(listOf("Next"), repository.incompleteSnapshot().map(TaskEntity::text))
		assertEquals(listOf("Current"), repository.completedSnapshot().map(TaskEntity::text))
		assertTrue(viewModel.uiState.value.pendingCompletionTaskIds.isEmpty())
		assertEquals("Next", viewModel.uiState.value.currentTask?.text)
		assertEquals(1, viewModel.uiState.value.completionShiftToken)
	}

	@Test
	fun uncheckCompletedTask_createsNewActiveCopy_andKeepsHistoryEntry() = runTest(dispatcher) {
		val repository = FakeTaskRepository(
			listOf(
				TaskEntity(id = 1L, text = "Current", sortOrder = 0L),
				TaskEntity(id = 2L, text = "Done thing", sortOrder = 7L, completedAt = 500L)
			)
		)
		val viewModel = createViewModel(repository)
		collectUiState(viewModel)
		advanceUntilIdle()

		viewModel.uncheckCompletedTask(2L)
		advanceUntilIdle()

		assertEquals(
			listOf("Current", "Done thing"),
			repository.incompleteSnapshot().map(TaskEntity::text)
		)
		assertEquals(1L, repository.incompleteSnapshot().last().sortOrder)
		assertEquals(listOf("Done thing"), repository.completedSnapshot().map(TaskEntity::text))
	}

	@Test
	fun deleteTask_requiresConfirmation_beforeRemovingItem() = runTest(dispatcher) {
		val repository = FakeTaskRepository(
			listOf(
				TaskEntity(id = 1L, text = "Current", sortOrder = 0L),
				TaskEntity(id = 2L, text = "Later", sortOrder = 1L)
			)
		)
		val viewModel = createViewModel(repository)
		collectUiState(viewModel)
		advanceUntilIdle()

		viewModel.requestDeleteTask(2L)
		runCurrent()
		assertEquals("Later", viewModel.uiState.value.deleteTargetTask?.text)
		assertEquals(listOf("Current", "Later"), repository.incompleteSnapshot().map(TaskEntity::text))

		viewModel.confirmDeleteTask()
		advanceUntilIdle()

		assertEquals(listOf("Current"), repository.incompleteSnapshot().map(TaskEntity::text))
		assertNull(viewModel.uiState.value.deleteTargetTask)
	}

	@Test
	fun moveActiveTask_updatesPriorityImmediately() = runTest(dispatcher) {
		val repository = FakeTaskRepository(
			listOf(
				TaskEntity(id = 1L, text = "Current", sortOrder = 0L),
				TaskEntity(id = 2L, text = "Middle", sortOrder = 1L),
				TaskEntity(id = 3L, text = "Bottom", sortOrder = 2L)
			)
		)
		val viewModel = createViewModel(repository)
		collectUiState(viewModel)
		advanceUntilIdle()

		viewModel.moveActiveTask(fromIndex = 2, toIndex = 0)
		advanceUntilIdle()

		assertEquals(
			listOf("Bottom", "Current", "Middle"),
			repository.incompleteSnapshot().map(TaskEntity::text)
		)
		assertEquals(listOf(0L, 1L, 2L), repository.incompleteSnapshot().map(TaskEntity::sortOrder))
		assertEquals("Bottom", viewModel.uiState.value.currentTask?.text)
	}

	@Test
	fun updateSearchQuery_filtersVisibleTasks_caseInsensitively() = runTest(dispatcher) {
		val repository = FakeTaskRepository(
			listOf(
				TaskEntity(id = 1L, text = "Alpha current", sortOrder = 0L),
				TaskEntity(id = 2L, text = "Bravo later", sortOrder = 1L),
				TaskEntity(id = 3L, text = "done ALPHA", sortOrder = 2L, completedAt = 100L)
			)
		)
		val viewModel = createViewModel(repository)
		collectUiState(viewModel)
		advanceUntilIdle()

		viewModel.updateSearchQuery("alpha")
		advanceUntilIdle()

		assertEquals("alpha", viewModel.uiState.value.searchQuery)
		assertEquals("Alpha current", viewModel.uiState.value.currentTask?.text)
		assertEquals(emptyList<ExpandedTaskUiModel>(), viewModel.uiState.value.futureTasks)
		assertEquals(listOf("done ALPHA"), viewModel.uiState.value.completedTasks.map(ExpandedTaskUiModel::text))
	}

	@Test
	fun updateSearchQuery_hidesNonMatchingCurrentTask_whenThereAreNoMatches() = runTest(dispatcher) {
		val repository = FakeTaskRepository(
			listOf(
				TaskEntity(id = 1L, text = "Current", sortOrder = 0L),
				TaskEntity(id = 2L, text = "Later", sortOrder = 1L)
			)
		)
		val viewModel = createViewModel(repository)
		collectUiState(viewModel)
		advanceUntilIdle()

		viewModel.updateSearchQuery("zzz")
		advanceUntilIdle()

		assertNull(viewModel.uiState.value.currentTask)
		assertTrue(viewModel.uiState.value.futureTasks.isEmpty())
		assertTrue(viewModel.uiState.value.completedTasks.isEmpty())
		assertNull(viewModel.uiState.value.currentTaskFlatIndex)
	}

	private fun createViewModel(repository: FakeTaskRepository): ExpandedTasksViewModel {
		return ExpandedTasksViewModel(repository)
	}

	private fun TestScope.collectUiState(viewModel: ExpandedTasksViewModel) {
		backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
			viewModel.uiState.collect { }
		}
	}
}

@OptIn(ExperimentalCoroutinesApi::class)
private class FakeTaskRepository(
	initialTasks: List<TaskEntity>
) : TaskRepository {
	private val tasksFlow = MutableStateFlow(initialTasks.sortedWith(taskComparator))
	private var nextId = (initialTasks.maxOfOrNull(TaskEntity::id) ?: 0L) + 1L

	override fun getCurrentTask(): Flow<TaskEntity?> = tasksFlow.map { tasks ->
		tasks
			.filter { it.completedAt == null }
			.minByOrNull(TaskEntity::sortOrder)
	}

	override fun getIncompleteTasks(): Flow<List<TaskEntity>> = tasksFlow.map { tasks ->
		tasks
			.filter { it.completedAt == null }
			.sortedBy(TaskEntity::sortOrder)
	}

	override fun getCompletedTasks(): Flow<List<TaskEntity>> = tasksFlow.map { tasks ->
		tasks
			.filter { it.completedAt != null }
			.sortedByDescending(TaskEntity::completedAt)
	}

	override suspend fun insertTask(task: TaskEntity): Long {
		val insertedTask = task.copy(id = if (task.id == 0L) nextId++ else task.id)
		tasksFlow.value = (tasksFlow.value + insertedTask).sortedWith(taskComparator)
		return insertedTask.id
	}

	override suspend fun updateTask(task: TaskEntity) {
		tasksFlow.value = tasksFlow.value
			.map { existing -> if (existing.id == task.id) task else existing }
			.sortedWith(taskComparator)
	}

	override suspend fun reorderTasks(tasks: List<TaskEntity>) {
		val replacements = tasks.associateBy(TaskEntity::id)
		tasksFlow.value = tasksFlow.value
			.map { existing -> replacements[existing.id] ?: existing }
			.sortedWith(taskComparator)
	}

	override suspend fun deleteTask(task: TaskEntity) {
		tasksFlow.value = tasksFlow.value.filterNot { it.id == task.id }
	}

	fun incompleteSnapshot(): List<TaskEntity> = tasksFlow.value
		.filter { it.completedAt == null }
		.sortedBy(TaskEntity::sortOrder)

	fun completedSnapshot(): List<TaskEntity> = tasksFlow.value
		.filter { it.completedAt != null }
		.sortedByDescending(TaskEntity::completedAt)

	companion object {
		private val taskComparator = compareBy<TaskEntity> { it.completedAt != null }
			.thenBy { it.sortOrder }
			.thenByDescending { it.completedAt ?: Long.MIN_VALUE }
			.thenBy { it.id }
	}
}
