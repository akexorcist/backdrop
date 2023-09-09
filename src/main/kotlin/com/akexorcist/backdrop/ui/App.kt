@file:Suppress("FunctionName")

package com.akexorcist.backdrop.ui

import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.akexorcist.backdrop.di.AppModule
import com.akexorcist.backdrop.resource.StringResource
import com.akexorcist.backdrop.ui.main.MainRoute
import com.akexorcist.backdrop.ui.main.MainViewModel
import com.akexorcist.backdrop.ui.theme.defaultColors
import com.github.eduramiba.webcamcapture.drivers.NativeDriver
import com.github.sarxos.webcam.Webcam
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.get

fun main() = application {
    Webcam.setDriver(NativeDriver())
    startKoin { modules(AppModule.modules) }
    val windowState = rememberWindowState(
        position = WindowPosition.Aligned(Alignment.Center),
        size = DpSize(1280.dp, 720.dp),
    )
    val coroutineScope = rememberCoroutineScope()
    Window(
        title = StringResource.appName,
        state = windowState,
        undecorated = true,
        onCloseRequest = ::exitApplication,
    ) {
        MaterialTheme(colors = defaultColors) {
            WindowDraggableArea {
                App(
                    isFullScreen = windowState.placement == WindowPlacement.Fullscreen || windowState.placement == WindowPlacement.Maximized,
                    onMaximizeWindowClick = {
                        coroutineScope.launch {
                            windowState.placement = WindowPlacement.Fullscreen
                            delay(1000)
                            windowState.placement = WindowPlacement.Maximized
                        }
                    },
                    onMinimizeWindowClick = { windowState.placement = WindowPlacement.Floating },
                    onCloseAppClick = { exitApplication() },
                )
            }
        }
    }
}

@Composable
fun App(
    isFullScreen: Boolean,
    onMaximizeWindowClick: () -> Unit,
    onMinimizeWindowClick: () -> Unit,
    onCloseAppClick: () -> Unit,
) {
    MainRoute(
        mainViewModel = get(MainViewModel::class.java),
        isFullScreen = isFullScreen,
        onMaximizeWindowClick = onMaximizeWindowClick,
        onMinimizeWindowClick = onMinimizeWindowClick,
        onCloseAppClick = onCloseAppClick,
    )
}
