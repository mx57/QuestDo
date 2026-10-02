package com.example.ui.viewmodel

data class FocusTimerUiState(
    val totalTimeMinutes: Int = 25,
    val timeLeftSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val targetEndTimeMs: Long = 0L,
    val activeSound: String = "Шум дождя 🌧️",
    val isMuted: Boolean = false,
    val spotlightTaskId: Long? = null,
    val spotlightTaskTitle: String? = null,
    val showCompletedDialog: Boolean = false,
    val completedMinutes: Int = 0
) {
    val totalSeconds: Int
        get() = (totalTimeMinutes * 60).coerceAtLeast(1)

    val progress: Float
        get() = (totalSeconds - timeLeftSeconds).toFloat() / totalSeconds.toFloat()

    val formattedTime: String
        get() {
            val minutes = timeLeftSeconds / 60
            val seconds = timeLeftSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}
