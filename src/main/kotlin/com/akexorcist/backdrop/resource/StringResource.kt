package com.akexorcist.backdrop.resource

object StringResource {
    val appName: String
    val menuToggleUiDisplayContentDescription: String
    val menuCloseAppContentDescription: String
    val menuEnterToFullscreenContentDescription: String
    val menuExitFromFullscreenContentDescription: String
    val labelCurrentResolution: String
    val labelResolution: String
    val labelVideo: String
    val labelAudioInput: String
    val labelAudioOutput: String

    init {
        appName = "Backdrop"
        menuToggleUiDisplayContentDescription = "Toggle UI display"
        menuCloseAppContentDescription = "Close app"
        menuEnterToFullscreenContentDescription = "Enter to fullscreen"
        menuExitFromFullscreenContentDescription = "Exit from fullscreen"
        labelCurrentResolution = "Current Resolution"
        labelResolution = "Resolution"
        labelVideo = "Video"
        labelAudioInput = "Audio Input"
        labelAudioOutput = "Audio Output"
    }
}