package io.tasklite

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import io.tasklite.data.TaskDatabaseProvider
import io.tasklite.notification.TaskLiteNotificationManager
import io.tasklite.ui.TaskLiteApp
import io.tasklite.widget.TaskLiteLaunchStateStore
import io.tasklite.ui.theme.TaskLiteTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
	private var pendingNotificationEnable = false

	private val notificationPermissionLauncher = registerForActivityResult(
		ActivityResultContracts.RequestPermission()
	) { isGranted ->
		lifecycleScope.launch {
			TaskLiteNotificationManager.setEnabled(
				context = applicationContext,
				enabled = isGranted && pendingNotificationEnable
			)
		}
		pendingNotificationEnable = false
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		TaskLiteLaunchStateStore.update(intent)

		setContent {
			val repository = remember {
				TaskDatabaseProvider.getRepository(applicationContext)
			}
			val notificationEnabled by TaskLiteNotificationManager.observeEnabled(applicationContext)
				.collectAsStateWithLifecycle(initialValue = true)
			val coroutineScope = rememberCoroutineScope()

			TaskLiteTheme {
				TaskLiteApp(
					repository = repository,
					notificationEnabled = notificationEnabled,
					onNotificationToggleRequested = { enabled ->
						coroutineScope.launch {
							if (enabled && shouldRequestNotificationPermission()) {
								pendingNotificationEnable = true
								notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
							} else {
								TaskLiteNotificationManager.setEnabled(applicationContext, enabled)
							}
						}
					}
				)
			}
		}
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		setIntent(intent)
		TaskLiteLaunchStateStore.update(intent)
	}

	private fun shouldRequestNotificationPermission(): Boolean {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
			return false
		}

		return ContextCompat.checkSelfPermission(
			this,
			Manifest.permission.POST_NOTIFICATIONS
		) != PackageManager.PERMISSION_GRANTED
	}
}
