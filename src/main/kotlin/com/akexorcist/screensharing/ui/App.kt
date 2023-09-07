@file:Suppress("FunctionName")

package com.akexorcist.screensharing.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
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
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

@Composable
fun App() {
    MaterialTheme(colors = defaultColors) {
        MainRoute(mainViewModel = get(MainViewModel::class.java))
    }
}
