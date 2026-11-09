---
title: "NPinner — notification system"
status: current
summary: "Deep dive on notification lifecycle, scheduling, broadcast receivers, WorkManager workers and soft deletes."
read_when: "Debugging notification persistence, reboot recovery, or scheduled work."
---

> Context: [Main](../../context.md) > [NPinner](./context.md) > Notification System

# NPinner — Notification System

This document covers NPinner's notification lifecycle, scheduling, persistence across reboots, broadcast receivers, and WorkManager workers. For general NPinner context, see [NPinner context](./context.md).

## Overview

NPinner's core functionality is creating persistent (ongoing) notifications that survive app closure, device reboots, and app updates. The system uses a combination of:

- **System Notifications** (ongoing, non-dismissable) for pinned notifications
- **WorkManager** for scheduled and periodic tasks
- **BroadcastReceivers** for boot/update resilience
- **Soft deletes** for data integrity

## Notification Lifecycle

### 1. Create

1. User opens editor → enters title (required, max 72 chars) + description (optional, max 400 chars)
2. On save → `NotificationRepository.upsert()` generates UUID, sets `createdAt`/`updatedAt` timestamps
3. If **no schedule** → auto-pin: `isPinned = true` + `NPinnerNotificationManager.postNotification()`
4. If **scheduled** → `NPinnerNotificationScheduler.schedule()` enqueues WorkManager one-time work with computed delay

### 2. Pin / Unpin

- Toggle updates `isPinned` in Room via `NotificationRepository.updatePinStatus()`
- **Pin**: `NPinnerNotificationManager.postNotification()` creates system notification
- **Unpin**: `NPinnerNotificationManager.dismissNotification()` cancels system notification
- Notification ID is derived from UUID: `notification.id.hashCode()`

### 3. Schedule

- User sets date + time + optional repeat type (DAY/WEEK/MONTH)
- `NPinnerNotificationScheduler` enqueues a `ScheduleWorker` as one-time work
- Delay computed: `(scheduledInstant - Clock.System.now()).inWholeMilliseconds`
- When triggered: worker pins the notification + posts system notification

### 4. Recurring

After a scheduled notification triggers:
1. `ScheduleWorker` checks if `schedule.type != null` (recurring)
2. Adds 1 unit to the date: `schedule.date.plus(1, DateTimeUnit.DAY/WEEK/MONTH)`
3. Updates the notification's schedule in the database
4. Re-enqueues a new `ScheduleWorker` with the updated delay

### 5. Delete

- **Soft delete**: Sets `deletedAt` timestamp (non-null = deleted)
- Dismisses system notification via `NPinnerNotificationManager`
- Cancels any scheduled WorkManager work
- Database queries filter `deletedAt IS NULL` — soft-deleted records are invisible but preserved

### 6. Swipe-to-Dismiss (in list)

- Swipe gesture in the notification list triggers the full delete flow
- Same as delete: soft-delete + dismiss + cancel schedule

## Key Components

### `NPinnerNotificationManager`

Manages system notification posting and dismissal.

**Responsibilities:**
- Creates notification channel on first use: `NPinner_Pinned_Notifications_Channel`
  - Importance: LOW (no sound)
  - No badge, no lights
- Posts ongoing (non-dismissable) notifications
- Dismisses notifications by ID

**Notification features:**
- **Ongoing**: Cannot be swiped away by the user
- **Actions**: "Unpin" and "Delete" buttons via broadcast PendingIntents
- **Content tap**: Opens editor via deep link (`npinner://notificationEditor?id=...`)
- **ID**: `notification.id.hashCode()` (UUID string hashed to Int)

### `NPinnerNotificationMonitor`

Ensures all pinned notifications remain visible in the system notification panel.

**Method:**
```kotlin
fun ensurePinnedNotificationVisibility()
```

**How it works:**
1. Queries all notifications where `isPinned = true` and `deletedAt IS NULL`
2. Checks which system notifications are currently active
3. Re-posts any pinned notifications that are missing from the system panel

**Called by:**
- `DeviceBootReceiver` (after reboot)
- `AppUpdateReceiver` (after app update)
- `MonitorWorker` (every 30 minutes)
- `NPinnerActivity.onCreate()` (app launch)

### `NPinnerNotificationScheduler`

Enqueues and cancels WorkManager schedule requests.

**Schedule:**
```kotlin
fun schedule(notification: NPinnerNotification)
```
- Creates a `ScheduleNotificationRequest` — one-time WorkManager work
- Work name: unique per notification ID (allows cancellation)
- Delay: `(targetInstant - Clock.System.now()).inWholeMilliseconds`

**Cancel:**
```kotlin
fun cancel(notificationId: String)
```
- Cancels unique work by notification ID

### `IntentProvider` (Object)

Creates PendingIntents for notification actions:
- `getNotificationEditorPendingIntent(id)` — content tap → deep link to editor
- `getUnpinPendingIntent(id)` — "Unpin" action → broadcast
- `getDeletePendingIntent(id)` — "Delete" action → broadcast

All PendingIntents use `FLAG_IMMUTABLE` and unique request codes.

## BroadcastReceivers

All receivers implement `KoinComponent` and use `by inject()` for dependency access.

### `DeviceBootReceiver`

- **Action**: `android.intent.action.BOOT_COMPLETED`
- **Purpose**: Re-show all pinned notifications after device reboot
- **Calls**: `NPinnerNotificationMonitor.ensurePinnedNotificationVisibility()`

### `AppUpdateReceiver`

- **Action**: `android.intent.action.MY_PACKAGE_REPLACED`
- **Purpose**: Re-show all pinned notifications after app update
- **Calls**: `NPinnerNotificationMonitor.ensurePinnedNotificationVisibility()`

### `UnpinNotificationReceiver`

- **Action**: `npinner.unpin_notification`
- **Purpose**: Handle "Unpin" button tap on system notification
- **Flow**: Dismisses system notification → updates `isPinned = false` in Room

### `DeleteNotificationReceiver`

- **Action**: `npinner.delete_notification`
- **Purpose**: Handle "Delete" button tap on system notification
- **Flow**: Dismisses system notification → soft-deletes in Room → cancels scheduled work

### Manifest Registration

All receivers are registered with `android:exported="false"`:
```xml
<receiver android:name=".core.receiver.DeviceBootReceiver" android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

## WorkManager Workers

### `MonitorWorker`

**Type**: Periodic (every 30 minutes)
**Purpose**: Safety net — ensures pinned notifications stay visible even if the system kills them

**Flow:**
1. Runs every ~30 minutes
2. Calls `NPinnerNotificationMonitor.ensurePinnedNotificationVisibility()`
3. Re-posts any missing pinned notifications

**Enqueued by**: `NPinnerActivity.onCreate()` — `ExistingPeriodicWorkPolicy.KEEP` ensures only one instance runs

### `ScheduleWorker`

**Type**: One-time (with initial delay)
**Purpose**: Pins a notification at its scheduled time

**Flow:**
1. Triggered when delay expires (scheduled time reached)
2. Loads notification from database
3. Sets `isPinned = true` + `updatedAt = now`
4. Posts system notification via `NPinnerNotificationManager`
5. If recurring (`schedule.type != null`):
   - Computes next occurrence: `date + 1 DAY/WEEK/MONTH`
   - Updates schedule in database
   - Re-enqueues itself with new delay

### Koin Worker Integration

WorkManager is initialized via Koin's `workManagerFactory()` in `NPinnerApplication`:
```kotlin
startKoin {
    workManagerFactory()  // Registers KoinWorkerFactory
    modules(NPinnerModule)
}
```

Workers are registered in the Koin module:
```kotlin
worker { MonitorWorker(androidContext(), get(), get()) }
worker { ScheduleWorker(androidContext(), get(), get(), get()) }
```

The default `InitializationProvider` is removed in the manifest:
```xml
<provider
    android:name="androidx.startup.InitializationProvider"
    tools:node="remove" />
```

## Notification Persistence Strategy

NPinner uses a multi-layered approach to ensure notifications survive all scenarios:

| Event | Handler | Action |
|-------|---------|--------|
| Device reboot | `DeviceBootReceiver` | Re-posts all pinned notifications |
| App update | `AppUpdateReceiver` | Re-posts all pinned notifications |
| System kills notification | `MonitorWorker` (30 min) | Detects missing, re-posts |
| App opened | `NPinnerActivity.onCreate()` | Immediate check + enqueue periodic monitor |
| Scheduled time reached | `ScheduleWorker` | Pins + posts notification |

This ensures that as long as the notification is marked `isPinned = true` in the database, it will always eventually reappear in the system panel — even after reboots, updates, or being killed by the system.

## Schedule Data Model

### `Schedule` (embedded in `NotificationEntity`)

| Field | Type | Notes |
|-------|------|-------|
| `date` | `kotlinx.datetime.LocalDate` | Target date, stored as ISO string in Room |
| `time` | `kotlinx.datetime.LocalTime` | Target time, stored as ISO string in Room |
| `type` | `ScheduleType?` | `null` = one-time, non-null = recurring |

### `ScheduleType` (Enum)

```kotlin
enum class ScheduleType {
    DAY,    // Repeat daily
    WEEK,   // Repeat weekly
    MONTH   // Repeat monthly
}
```

### Computed Properties

- `schedule.asLocalDateTime` → combines date + time into `LocalDateTime(date, time)`
- `schedule.isFuture` → `true` if recurring (always valid) OR if datetime is in the future
- Default schedule: 10 minutes from now, repeat daily

### Date/Time Conversion for WorkManager Delay

```kotlin
val scheduledInstant = schedule.asLocalDateTime.toInstant(TimeZone.currentSystemDefault())
val delayMs = (scheduledInstant - Clock.System.now()).inWholeMilliseconds
```

Uses `kotlin.time.Clock` and `kotlin.time.Instant` (not the deprecated kotlinx-datetime versions).

## Related Context

- [NPinner Context](./context.md) — Module overview, architecture, data model, UI patterns
- [Core Modules](../../core/context.md) — Shared utilities, DI module
- [Main Context](../../context.md) — Project overview, tech stack