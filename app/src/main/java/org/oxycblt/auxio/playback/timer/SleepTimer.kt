/*
 * Copyright (c) 2026 Auxio Project
 * SleepTimer.kt is part of Auxio.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.oxycblt.auxio.playback.timer

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.oxycblt.auxio.BuildConfig
import org.oxycblt.auxio.IntegerTable
import org.oxycblt.auxio.R
import org.oxycblt.auxio.playback.formatDurationMs
import org.oxycblt.auxio.playback.service.PlaybackActions
import org.oxycblt.auxio.playback.state.PlaybackStateManager
import org.oxycblt.auxio.util.newBroadcastPendingIntent
import org.oxycblt.auxio.util.newMainPendingIntent
import timber.log.Timber as L

/**
 * A sleep timer that pauses playback after a user-specified duration. Lives for the entire process
 * so that the countdown continues even when the UI is closed while the playback service is running.
 *
 * While a timer is running, an ongoing notification with a live countdown is shown so that the user
 * can keep track of it (and cancel it) from outside the app.
 *
 * @author Alexander Capehart (OxygenCobalt)
 */
@Singleton
class SleepTimer
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val playbackManager: PlaybackStateManager,
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var countdownJob: Job? = null
    private val notificationManager = NotificationManagerCompat.from(context)
    private var notificationBuilder: NotificationCompat.Builder? = null

    private val _remainingMs = MutableStateFlow<Long?>(null)
    /** Time left until playback is paused, in milliseconds. Null if no timer is running. */
    val remainingMs: StateFlow<Long?>
        get() = _remainingMs

    init {
        // An earlier revision of this channel used IMPORTANCE_LOW, which is hidden from the
        // lock screen on many devices. Existing channels cannot have their importance raised
        // programmatically, and re-creating a channel with the same ID resurrects it's old
        // settings, so the current channel uses a different ID and the old one is removed.
        notificationManager.deleteNotificationChannel(OLD_CHANNEL_ID)
        // DEFAULT importance so that the countdown is visible on the lock screen. The
        // notification itself is posted silently, so this does not result in any sound or
        // vibration.
        val channel =
            NotificationChannelCompat.Builder(
                    CHANNEL_ID,
                    NotificationManagerCompat.IMPORTANCE_DEFAULT,
                )
                .setName(context.getString(R.string.lbl_sleep_timer))
                .setLightsEnabled(false)
                .setVibrationEnabled(false)
                .setShowBadge(false)
                .build()
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Start a new sleep timer, replacing any timer that is already running.
     *
     * @param durationMs How long to play before pausing, in milliseconds.
     */
    fun start(durationMs: Long) {
        L.d("Starting sleep timer for ${durationMs}ms")
        countdownJob?.cancel()
        countdownJob =
            scope.launch {
                val endMs = SystemClock.elapsedRealtime() + durationMs
                while (true) {
                    val remainingMs = endMs - SystemClock.elapsedRealtime()
                    if (remainingMs <= 0) break
                    _remainingMs.value = remainingMs
                    updateNotification(remainingMs)
                    // Tick on second boundaries so the countdown decrements evenly.
                    delay((remainingMs - 1) % 1000 + 1)
                }
                L.d("Sleep timer elapsed, pausing playback")
                _remainingMs.value = null
                removeNotification()
                playbackManager.playing(false)
            }
    }

    /** Cancel the currently running timer, if any. Playback is left as-is. */
    fun cancel() {
        L.d("Cancelling sleep timer")
        countdownJob?.cancel()
        countdownJob = null
        _remainingMs.value = null
        removeNotification()
    }

    // The notification is a nicety and not crucial to timer functionality, so it's fine to
    // silently do nothing when notifications are not permitted.
    @SuppressLint("MissingPermission")
    private fun updateNotification(remainingMs: Long) {
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }
        // The remaining time is the title so that it's as prominent as a notification allows.
        // Since it has to be re-posted every second anyways, a text countdown is used over a
        // chronometer. The notification is silent and only alerts once, so the updates cause
        // no sound or flickering.
        val builder =
            notificationBuilder
                ?: NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_timer_24)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setContentText(context.getString(R.string.lng_sleep_timer))
                    .setShowWhen(false)
                    .setOngoing(true)
                    .setSilent(true)
                    .setOnlyAlertOnce(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentIntent(context.newMainPendingIntent())
                    .addAction(
                        R.drawable.ic_close_24,
                        context.getString(R.string.lbl_stop),
                        context.newBroadcastPendingIntent(
                            PlaybackActions.ACTION_CANCEL_SLEEP_TIMER
                        ),
                    )
                    .also { notificationBuilder = it }
        builder.setContentTitle(remainingMs.formatDurationMs(true))
        notificationManager.notify(IntegerTable.SLEEP_TIMER_NOTIFICATION_CODE, builder.build())
    }

    private fun removeNotification() {
        notificationBuilder = null
        notificationManager.cancel(IntegerTable.SLEEP_TIMER_NOTIFICATION_CODE)
    }

    private companion object {
        val CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel.SLEEP_TIMER_COUNTDOWN"
        val OLD_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel.SLEEP_TIMER"
    }
}
