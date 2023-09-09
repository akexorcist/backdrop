package com.akexorcist.backdrop.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.delay

@Composable
fun rememberBackdropAppState(
    windowState: WindowState,
    applicationScope: ApplicationScope,
): BackdropAppState = remember(
    windowState,
    applicationScope,
) {
    BackdropAppState(
        windowState = windowState,
        applicationScope = applicationScope,
    )
}

class BackdropAppState(
    private val windowState: WindowState,
    private val applicationScope: ApplicationScope,
) {
    fun isFullscreen() = windowState.placement == WindowPlacement.Fullscreen ||
            windowState.placement == WindowPlacement.Maximized

    suspend fun enterFullScreen() {
        windowState.placement = WindowPlacement.Fullscreen
        delay(1000)
        windowState.placement = WindowPlacement.Maximized
    }

    fun exitFullScreen() {
        windowState.placement = WindowPlacement.Floating
    }

    fun exitApplication() {
        applicationScope.exitApplication()
    }
}
