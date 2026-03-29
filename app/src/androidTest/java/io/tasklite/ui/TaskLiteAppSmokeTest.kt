package io.tasklite.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.tasklite.ui.expanded.ExpandedTaskScreenTestTags
import io.tasklite.ui.theme.TaskLiteTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskLiteAppSmokeTest {
	@get:Rule
	val composeRule = createAndroidComposeRule<ComponentActivity>()

	private lateinit var repository: FakeTaskRepository

	@Before
	fun setUp() {
		repository = FakeTaskRepository()

		composeRule.setContent {
			TaskLiteTheme {
				TaskLiteApp(
					repository = repository,
					notificationEnabled = false
				)
			}
		}
	}

	@Test
	fun launchWithEmptyStateShowsCoreControls() {
		composeRule.onNodeWithTag(ExpandedTaskScreenTestTags.SEARCH_INPUT).assertIsDisplayed()
		composeRule.onNodeWithTag(ExpandedTaskScreenTestTags.ADD_TASK_INPUT).assertIsDisplayed()
		composeRule.onNodeWithTag(ExpandedTaskScreenTestTags.ADD_TASK_BUTTON).assertIsDisplayed()
		composeRule.onNodeWithTag(ExpandedTaskScreenTestTags.EMPTY_STATE_CARD).assertIsDisplayed()
	}

	@Test
	fun addTaskFromComposerShowsTaskAndClearsInput() {
		composeRule.onNodeWithTag(ExpandedTaskScreenTestTags.ADD_TASK_INPUT)
			.performTextInput("Release smoke")
		composeRule.onNodeWithTag(ExpandedTaskScreenTestTags.ADD_TASK_BUTTON)
			.performClick()

		composeRule.onNodeWithText("Release smoke").assertIsDisplayed()
		composeRule.onNodeWithTag(ExpandedTaskScreenTestTags.ADD_TASK_INPUT)
			.assertTextEquals("")
	}
}
