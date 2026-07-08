/*
 * Copyright (c) 2026 Auxio Project
 * SleepTimerDialog.kt is part of Auxio.
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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.oxycblt.auxio.R
import org.oxycblt.auxio.databinding.DialogSleepTimerBinding
import org.oxycblt.auxio.playback.PlaybackViewModel
import org.oxycblt.auxio.playback.formatDurationMs
import org.oxycblt.auxio.ui.ViewBindingMaterialDialogFragment
import org.oxycblt.auxio.util.collectImmediately

/**
 * A [ViewBindingMaterialDialogFragment] that allows the user to start or stop a sleep timer that
 * pauses playback after a given duration. Durations up to an hour are picked with a radial dial,
 * anything longer can be typed into a dedicated field. The last used duration is the initial
 * choice.
 *
 * @author Alexander Capehart (OxygenCobalt)
 */
@AndroidEntryPoint
class SleepTimerDialog : ViewBindingMaterialDialogFragment<DialogSleepTimerBinding>() {
    private val playbackModel: PlaybackViewModel by activityViewModels()
    private var notificationPermissionLauncher: ActivityResultLauncher<String>? = null
    private var currentMinutes = 0
    private var updatingCustomField = false

    override fun onConfigDialog(builder: AlertDialog.Builder) {
        builder
            .setTitle(R.string.lbl_sleep_timer)
            .setPositiveButton(R.string.lbl_start) { _, _ ->
                playbackModel.startSleepTimer(currentMinutes)
            }
            .setNeutralButton(R.string.lbl_stop) { _, _ -> playbackModel.cancelSleepTimer() }
            .setNegativeButton(R.string.lbl_cancel, null)
    }

    override fun onCreateBinding(inflater: LayoutInflater) =
        DialogSleepTimerBinding.inflate(inflater)

    override fun onBindingCreated(binding: DialogSleepTimerBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)

        // Unlike the (exempt) media notification, the countdown notification requires the
        // notification permission, which the app does not request anywhere else. Ask for it
        // up-front so that it's already decided by the time the timer is started. The timer
        // itself works regardless of the outcome, so nothing needs to be done with the result.
        notificationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                // Nothing to do
            }
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            requireNotNull(notificationPermissionLauncher) {
                    "Notification permission launcher was not available"
                }
                .launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        binding.sleepTimerRing.onMinutesChanged = { minutes ->
            updateMinutes(minutes, fromRing = true)
        }
        binding.sleepTimerCustom.doAfterTextChanged { text ->
            if (updatingCustomField) return@doAfterTextChanged
            val minutes = text?.toString()?.toIntOrNull() ?: return@doAfterTextChanged
            if (minutes >= 1) {
                updateMinutes(minutes, fromRing = false)
            }
        }

        updateMinutes(playbackModel.lastSleepTimerDurationMinutes, fromRing = false)
        collectImmediately(playbackModel.sleepTimerRemainingMs, ::updateTimer)
    }

    override fun onDestroyBinding(binding: DialogSleepTimerBinding) {
        super.onDestroyBinding(binding)
        notificationPermissionLauncher = null
        binding.sleepTimerRing.onMinutesChanged = null
    }

    /**
     * Move the dialog to a new selected duration, keeping the ring, the center label, and the
     * custom duration field in sync.
     *
     * @param minutes The new duration, in minutes.
     * @param fromRing Whether the change came from the ring. Ring drags overwrite the custom
     *   field, but typing into the field must never overwrite the field itself.
     */
    private fun updateMinutes(minutes: Int, fromRing: Boolean) {
        val binding = requireBinding()
        currentMinutes = minutes
        binding.sleepTimerValue.text = minutes.toString()
        if (binding.sleepTimerRing.minutes != minutes) {
            binding.sleepTimerRing.minutes = minutes
        }
        if (fromRing || binding.sleepTimerCustom.text.isNullOrEmpty()) {
            updatingCustomField = true
            binding.sleepTimerCustom.setText(minutes.toString())
            updatingCustomField = false
        }
    }

    private fun updateTimer(remainingMs: Long?) {
        val binding = requireBinding()
        binding.sleepTimerRemaining.isVisible = remainingMs != null
        if (remainingMs != null) {
            binding.sleepTimerRemaining.text = remainingMs.formatDurationMs(true)
        }
        // The stop button only makes sense while a timer is actually running. The buttons
        // are not created until the dialog is shown, so this can be null on the initializing
        // call.
        (dialog as AlertDialog?)
            ?.getButton(AlertDialog.BUTTON_NEUTRAL)
            ?.isVisible = remainingMs != null
    }
}
