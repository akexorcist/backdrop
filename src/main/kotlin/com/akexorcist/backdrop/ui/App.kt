@file:Suppress("FunctionName")

package com.akexorcist.backdrop.ui

import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
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
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.get

fun main() = application {
    Webcam.setDriver(NativeDriver())
    startKoin { modules(AppModule.modules) }
    val windowState = rememberWindowState(
        position = WindowPosition.Aligned(Alignment.Center),
        size = DpSize(1280.dp, 720.dp),
    )
    val appState = rememberBackdropAppState(
        windowState = windowState,
        applicationScope = this,
    )
    Window(
        title = StringResource.appName,
        state = windowState,
        undecorated = true,
        onCloseRequest = ::exitApplication,
    ) {
        MaterialTheme(colors = defaultColors) {
            WindowDraggableArea {
                App(appState = appState)
            }
        }
    }
}

@Composable
fun App(
    appState: BackdropAppState,
) {
    MainRoute(
        appState = appState,
        mainViewModel = get(MainViewModel::class.java),
    )
}
