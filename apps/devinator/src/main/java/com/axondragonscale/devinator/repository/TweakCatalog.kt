package com.axondragonscale.devinator.repository

import android.os.BatteryManager
import android.provider.Settings
import com.axondragonscale.devinator.model.Tweak
import com.axondragonscale.devinator.model.TweakGroup
import com.axondragonscale.devinator.model.TweakIcon

/** Created by Ronak on 09/11/26 */

/** "Stay awake" reads the plug types as a bitmask; anything plugged in keeps the screen on. */
private val PLUGGED_IN = BatteryManager.BATTERY_PLUGGED_AC or
    BatteryManager.BATTERY_PLUGGED_USB or
    BatteryManager.BATTERY_PLUGGED_WIRELESS

// These are `@hide` in the SDK, so the literal is the only handle on them, exactly as
// KEY_WIRELESS_DEBUGGING does for `adb_wifi_enabled`.
private const val KEY_BACKGROUND_PROCESS_LIMIT = "background_process_limit"
private const val KEY_ANR_SHOW_BACKGROUND = "anr_show_background"

/**
 * The Tweaks page in draw order. Adding a row is one entry here plus a vector in the UI's
 * `TweakIcon.vector()`; no other file knows these keys.
 *
 * None of these belong in [RESTORABLE_FLAG_KEYS]: stock Android keeps them when developer options
 * goes off, so zeroing them behind the user's back would be a surprise the platform does not set.
 */
val TweakCatalog: List<Tweak> = listOf(
    Tweak.Scale(
        key = Settings.Global.WINDOW_ANIMATION_SCALE,
        group = TweakGroup.ANIMATION,
        title = "Window animation",
        caption = "Windows opening and closing",
        icon = TweakIcon.ANIMATION,
    ),
    Tweak.Scale(
        key = Settings.Global.TRANSITION_ANIMATION_SCALE,
        group = TweakGroup.ANIMATION,
        title = "Transition animation",
        caption = "Moving between activities",
        icon = TweakIcon.ANIMATION,
    ),
    Tweak.Scale(
        key = Settings.Global.ANIMATOR_DURATION_SCALE,
        group = TweakGroup.ANIMATION,
        title = "Animator animation",
        caption = "Animation inside app screens",
        icon = TweakIcon.ANIMATION,
    ),
    Tweak.Toggle(
        key = Settings.Global.ALWAYS_FINISH_ACTIVITIES,
        group = TweakGroup.BEHAVIOUR,
        title = "Don't keep activities",
        caption = "Destroy every screen you leave",
        icon = TweakIcon.LIFECYCLE,
    ),
    Tweak.Limit(
        key = KEY_BACKGROUND_PROCESS_LIMIT,
        group = TweakGroup.BEHAVIOUR,
        title = "Background processes",
        caption = "How many apps may stay alive",
        icon = TweakIcon.PROCESSES,
    ),
    Tweak.Toggle(
        key = KEY_ANR_SHOW_BACKGROUND,
        group = TweakGroup.BEHAVIOUR,
        title = "Show all ANRs",
        caption = "Dialog when a background app hangs",
        icon = TweakIcon.ANR,
    ),
    Tweak.Toggle(
        key = Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
        group = TweakGroup.BEHAVIOUR,
        title = "Stay awake while charging",
        caption = "Never dim while plugged in",
        icon = TweakIcon.AWAKE,
        onValue = PLUGGED_IN,
    ),
)
