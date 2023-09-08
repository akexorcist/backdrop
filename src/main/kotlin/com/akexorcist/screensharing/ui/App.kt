@file:Suppress("FunctionName")

package com.akexorcist.screensharing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import com.akexorcist.screensharing.di.AppModule
import com.akexorcist.screensharing.ui.main.MainRoute
import com.akexorcist.screensharing.ui.main.MainViewModel
import com.akexorcist.screensharing.ui.theme.defaultColors
import com.github.eduramiba.webcamcapture.drivers.NativeDriver
import com.github.sarxos.webcam.Webcam
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.get

fun main() = application {
    Webcam.setDriver(NativeDriver())
    startKoin { modules(AppModule.modules) }
    Window(
        title = "Screen Sharing",
        state = rememberWindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = DpSize(1280.dp, 720.dp),
        ),
        undecorated = true,
        onCloseRequest = ::exitApplication,
    ) {
        MaterialTheme(colors = defaultColors) {
            WindowDraggableArea {
                App()
            }
        }
    }
}

@Composable
fun App() {
    MainRoute(mainViewModel = get(MainViewModel::class.java))
}
