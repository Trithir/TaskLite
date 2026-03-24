package com.erics.tasklite.ui.expanded

import kotlin.random.Random

data class EmptyStatePrompt(
	val title: String,
	val	message: String
)

data class SoftWarningPrompt(
	val title: String,
	val message: String
)

private enum class EmptyStateTone {
	CHILL,
	SASSY,
	MOTIVATIONAL
}

private val emptyStatePrompts = mapOf(
	EmptyStateTone.CHILL to listOf(
		EmptyStatePrompt(
			title = "Nothing on deck yet.",
			message = "Add one task and let the list do its job."
		),
		EmptyStatePrompt(
			title = "Quiet little moment.",
			message = "Drop in one thing that matters and call it a start."
		)
	),
	EmptyStateTone.SASSY to listOf(
		EmptyStatePrompt(
			title = "Suspiciously organized.",
			message = "Go ahead, give future-you something useful to tap."
		),
		EmptyStatePrompt(
			title = "A blank slate? Bold.",
			message = "Put one task here before your brain invents seven."
		)
	),
	EmptyStateTone.MOTIVATIONAL to listOf(
		EmptyStatePrompt(
			title = "Fresh start energy.",
			message = "One clear task is enough to get momentum moving."
		),
		EmptyStatePrompt(
			title = "Ready when you are.",
			message = "Add the next right thing and keep it simple."
		)
	)
)

fun randomEmptyStatePrompt(random: Random = Random.Default): EmptyStatePrompt {
	val tone = EmptyStateTone.entries.random(random)
	return emptyStatePrompts.getValue(tone).random(random)
}

private val softWarningPrompts = mapOf(
	EmptyStateTone.CHILL to listOf(
		SoftWarningPrompt(
			title = "This list is getting tall.",
			message = "Might be a nice moment to clear a few easy wins."
		),
		SoftWarningPrompt(
			title = "A lot is parked here.",
			message = "You do not need to solve it all. Just pull the next thread."
		)
	),
	EmptyStateTone.SASSY to listOf(
		SoftWarningPrompt(
			title = "That is... a generous number of tasks.",
			message = "No judgment. Maybe bully a couple of tiny ones off the list."
		),
		SoftWarningPrompt(
			title = "The queue has opinions now.",
			message = "Knocking out a few small tasks could calm it down fast."
		)
	),
	EmptyStateTone.MOTIVATIONAL to listOf(
		SoftWarningPrompt(
			title = "Big list, still manageable.",
			message = "A few completions will change the whole feel of it."
		),
		SoftWarningPrompt(
			title = "You have enough here to build momentum.",
			message = "Pick a couple of clean wins and let the stack shrink."
		)
	)
)

fun randomSoftWarningPrompt(random: Random = Random.Default): SoftWarningPrompt {
	val tone = EmptyStateTone.entries.random(random)
	return softWarningPrompts.getValue(tone).random(random)
}
