# TaskLite Privacy Policy Draft

Last updated: 2026-03-28

TaskLite is designed to be local-first and minimal.
This policy describes the current released behavior of TaskLite.

## Overview

TaskLite lets you create, edit, reorder, complete, search, and delete tasks on your Android device.
It also includes:
- a `4x1` home screen widget
- an optional ongoing notification
- vibration feedback for task interactions

## What data TaskLite stores

TaskLite stores task data on your device.
This includes:
- task text that you enter
- task ordering
- task completion history

If you enable the optional ongoing notification, TaskLite also stores a local preference so it can remember whether that notification is turned on.

The widget reads local app data so it can show the current task on your home screen.

## What TaskLite does not do

Based on the current implementation, TaskLite does not:
- create user accounts
- require sign-in
- sync data to a server
- transmit task data off-device
- include advertising SDKs
- include analytics SDKs
- include crash reporting SDKs
- sell personal data
- share task data with third parties

## Data collection and sharing

TaskLite does not collect or share user data off-device in the current implementation.

The current app manifest does not request the Android `INTERNET` permission.
The release manifest does include some transitive AndroidX/WorkManager support permissions used by the widget stack, including `ACCESS_NETWORK_STATE`, `WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED`, and `FOREGROUND_SERVICE`, but the shipped app does not send task data to the developer or another service based on the current codebase.

## Permissions

TaskLite currently uses these user-visible permissions directly:

### `VIBRATE`

Used for tactile feedback on task interactions.

### `POST_NOTIFICATIONS`

Used only for the optional ongoing notification on Android versions that require notification permission.
If you do not enable the ongoing notification, this permission is not required for the core task and widget experience.
If enabled, the ongoing notification may display current task text on device surfaces such as the notification shade or lock screen, depending on Android version and system settings.

## Device surfaces and privacy

If you place the widget on your home screen, your current task may be visible there.
If you enable the ongoing notification, your current task text may also be visible in notification surfaces depending on your device settings and lock-screen configuration.

## Retention and deletion

Your task data stays on your device unless you remove it.

- Deleting a task removes it from the local list.
- Completing a task does not delete it; completed tasks remain in local history unless you remove them by deleting them before completion or clearing the app by uninstalling it.

## Backup

TaskLite currently disables Android backup in the app manifest with `allowBackup="false"`.

## Children

TaskLite is a general productivity app.

## Changes to this policy

If TaskLite later adds network features, sync, analytics, crash reporting, accounts, or any off-device data handling, this policy should be updated before those changes are released.

## Contact

A public support contact and hosted privacy-policy URL still need to be added before store submission.
