@file:Suppress("FunctionName")

package com.akexorcist.backdrop.ui

import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.awt.ComposeWindow
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
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.io.File
import java.util.Properties

data class WindowInfo(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

fun main() {
    val windowInfo = loadLastWindowInfo()
    startKoin { modules(AppModule.modules) }
    application {
        val windowState = rememberWindowState(
            windowInfo = windowInfo,
        )
        val appState = rememberBackdropAppState(
            windowState = windowState,
            applicationScope = this,
        )
        LaunchedEffect(Unit) {
            Webcam.setDriver(NativeDriver())
        }
        Window(
            title = StringResource.appName,
            state = windowState,
            undecorated = true,
            onCloseRequest = ::exitApplication,
        ) {
            WindowConfigurationEffect(window = window)
            MaterialTheme(colors = defaultColors) {
                WindowDraggableArea {
                    App(appState = appState)
                }
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

@Composable
fun WindowConfigurationEffect(window: ComposeWindow) {
    DisposableEffect(Unit) {
        var lastWindowInfo = WindowInfo(
            x = window.x,
            y = window.y,
            width = window.size.width,
            height = window.size.height,
        )
        val componentListener = object : ComponentListener {
            override fun componentResized(e: ComponentEvent?) {
                val (width, height) = window.run { width to height }
                if (lastWindowInfo.width != width || lastWindowInfo.height != height) {
                    lastWindowInfo = lastWindowInfo.copy(
                        width = width,
                        height = height,
                    ).also {
                        saveNewWindowInfo(info = it)
                    }
                }
            }

            override fun componentMoved(e: ComponentEvent?) {
                val (x, y) = window.run { x to y }
                if (lastWindowInfo.x != x || lastWindowInfo.y != y) {
                    lastWindowInfo = lastWindowInfo.copy(
                        x = x,
                        y = y,
                    ).also {
                        saveNewWindowInfo(info = it)
                    }
                }
            }

            override fun componentShown(e: ComponentEvent?) = Unit

            override fun componentHidden(e: ComponentEvent?) = Unit

        }
        window.addComponentListener(componentListener)
        onDispose { window.removeComponentListener(componentListener) }
    }
}

private val windowInfoFile = File(System.getProperty("user.home"), ".window_info.properties")

private fun loadLastWindowInfo(): WindowInfo? {
    val properties = Properties()
    if (windowInfoFile.exists()) {
        properties.load(windowInfoFile.inputStream())
    }
    val x = properties.getProperty("x")?.toIntOrNull() ?: return null
    val y = properties.getProperty("y")?.toIntOrNull() ?: return null
    val width = properties.getProperty("width")?.toIntOrNull() ?: return null
    val height = properties.getProperty("height")?.toIntOrNull() ?: return null
    return WindowInfo(
        x = x,
        y = y,
        width = width,
        height = height,
    )
}

@Composable
private fun rememberWindowState(windowInfo: WindowInfo?): WindowState {
    return rememberWindowState(
        position = windowInfo?.let { info ->
            WindowPosition.Absolute(
                x = info.x.dp,
                y = info.y.dp,
            )
        } ?: WindowPosition.Aligned(Alignment.Center),
        size = windowInfo?.let { info ->
            DpSize(
                width = info.width.dp,
                height = info.height.dp,
            )
        } ?: DpSize(1280.dp, 720.dp),
    )
}

private fun saveNewWindowInfo(info: WindowInfo) {
    val properties = Properties().apply {
        setProperty("x", info.x.toString())
        setProperty("y", info.y.toString())
        setProperty("width", info.width.toString())
        setProperty("height", info.height.toString())
    }
    windowInfoFile.parentFile?.mkdirs()
    windowInfoFile.outputStream().use { properties.store(it, "Window Configuration") }
}